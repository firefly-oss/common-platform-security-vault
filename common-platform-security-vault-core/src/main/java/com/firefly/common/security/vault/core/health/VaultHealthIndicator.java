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

import com.firefly.common.security.vault.core.services.encryption.EncryptionService;
import com.firefly.common.security.vault.models.repositories.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Custom health indicator for Security Vault
 * Checks:
 * - Database connectivity
 * - Encryption service availability
 * - KMS connectivity (if configured)
 * - Active credentials count
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VaultHealthIndicator implements ReactiveHealthIndicator {

    private final CredentialRepository credentialRepository;
    private final EncryptionService encryptionService;

    @Override
    public Mono<Health> health() {
        return Mono.zip(
            checkDatabase(),
            checkEncryptionService(),
            checkCredentialStats()
        )
        .map(tuple -> {
            boolean dbHealthy = tuple.getT1();
            boolean encryptionHealthy = tuple.getT2();
            CredentialStats stats = tuple.getT3();
            
            if (dbHealthy && encryptionHealthy) {
                return Health.up()
                    .withDetail("database", "connected")
                    .withDetail("encryption", "operational")
                    .withDetail("activeCredentials", stats.activeCount)
                    .withDetail("expiredCredentials", stats.expiredCount)
                    .withDetail("totalCredentials", stats.totalCount)
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
            } else {
                Health.Builder builder = Health.down();
                if (!dbHealthy) {
                    builder.withDetail("database", "disconnected");
                }
                if (!encryptionHealthy) {
                    builder.withDetail("encryption", "unavailable");
                }
                return builder.build();
            }
        })
        .onErrorResume(e -> {
            log.error("Health check failed: {}", e.getMessage());
            return Mono.just(Health.down()
                .withDetail("error", e.getMessage())
                .build());
        });
    }

    /**
     * Check database connectivity
     */
    private Mono<Boolean> checkDatabase() {
        return credentialRepository.count()
            .map(count -> true)
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(false);
    }

    /**
     * Check encryption service
     */
    private Mono<Boolean> checkEncryptionService() {
        // Test encryption/decryption with a dummy value
        String testValue = "health-check";
        return encryptionService.encrypt(testValue, "health-check-key")
            .flatMap(result -> encryptionService.decrypt(
                result.encryptedValue(), 
                result.keyId(), 
                result.iv()
            ))
            .map(decrypted -> decrypted.equals(testValue))
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(false);
    }

    /**
     * Get credential statistics
     */
    private Mono<CredentialStats> checkCredentialStats() {
        LocalDateTime now = LocalDateTime.now();
        
        return Mono.zip(
            credentialRepository.count(),
            credentialRepository.countByActive(true),
            credentialRepository.countByExpiresAtBefore(now)
        )
        .map(tuple -> new CredentialStats(
            tuple.getT1(), // total
            tuple.getT2(), // active
            tuple.getT3()  // expired
        ))
        .onErrorReturn(new CredentialStats(0L, 0L, 0L));
    }

    /**
     * Internal stats holder
     */
    private record CredentialStats(Long totalCount, Long activeCount, Long expiredCount) {}
}
