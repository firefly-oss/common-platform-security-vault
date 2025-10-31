/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firefly.common.security.vault.core.health;

import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Health indicator for Key Management Service
 * 
 * Checks:
 * - Provider availability
 * - Master key accessibility
 * - Response time
 * - Provider type
 * 
 * Health Status:
 * - UP: Provider is accessible and master key is valid
 * - DOWN: Provider is not accessible or master key is invalid
 * - UNKNOWN: Unable to determine health status
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeyManagementHealthIndicator implements ReactiveHealthIndicator {

    private final KeyManagementPort keyManagementPort;
    private final SecurityVaultProperties properties;

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public Mono<Health> health() {
        Instant start = Instant.now();
        
        String masterKeyId = properties.getEncryption().getMasterKeyId();
        KeyManagementPort.ProviderType providerType = keyManagementPort.getProviderType();

        log.debug("Performing health check for Key Management - Provider: {}, Key: {}", 
            providerType, masterKeyId);

        return keyManagementPort.validateKey(masterKeyId)
            .timeout(HEALTH_CHECK_TIMEOUT)
            .map(isValid -> {
                Duration responseTime = Duration.between(start, Instant.now());
                
                if (isValid) {
                    log.debug("Key Management health check PASSED - Response time: {}ms", 
                        responseTime.toMillis());
                    
                    return Health.up()
                        .withDetail("provider", providerType.name())
                        .withDetail("masterKeyId", maskKeyId(masterKeyId))
                        .withDetail("keyValid", true)
                        .withDetail("responseTimeMs", responseTime.toMillis())
                        .withDetail("timestamp", Instant.now().toString())
                        .build();
                } else {
                    log.warn("Key Management health check FAILED - Master key is invalid");
                    
                    return Health.down()
                        .withDetail("provider", providerType.name())
                        .withDetail("masterKeyId", maskKeyId(masterKeyId))
                        .withDetail("keyValid", false)
                        .withDetail("reason", "Master key validation failed")
                        .withDetail("responseTimeMs", responseTime.toMillis())
                        .withDetail("timestamp", Instant.now().toString())
                        .build();
                }
            })
            .onErrorResume(error -> {
                Duration responseTime = Duration.between(start, Instant.now());
                
                log.error("Key Management health check ERROR: {}", error.getMessage());
                
                return Mono.just(Health.down()
                    .withDetail("provider", providerType.name())
                    .withDetail("masterKeyId", maskKeyId(masterKeyId))
                    .withDetail("error", error.getClass().getSimpleName())
                    .withDetail("message", error.getMessage())
                    .withDetail("responseTimeMs", responseTime.toMillis())
                    .withDetail("timestamp", Instant.now().toString())
                    .build());
            });
    }

    /**
     * Mask key ID for security in health check output
     */
    private String maskKeyId(String keyId) {
        if (keyId == null || keyId.length() <= 8) {
            return "***";
        }
        return keyId.substring(0, 4) + "***" + keyId.substring(keyId.length() - 4);
    }
}

