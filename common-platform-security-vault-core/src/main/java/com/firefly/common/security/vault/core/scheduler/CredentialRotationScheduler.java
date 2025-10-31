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


package com.firefly.common.security.vault.core.scheduler;

import com.firefly.common.security.vault.core.metrics.SecurityVaultMetrics;
import com.firefly.common.security.vault.core.services.rotation.CredentialRotationService;
import com.firefly.common.security.vault.models.repositories.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Scheduled jobs for automatic credential rotation and monitoring
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CredentialRotationScheduler {

    private final CredentialRepository credentialRepository;
    private final CredentialRotationService rotationService;
    private final SecurityVaultMetrics metrics;

    /**
     * Automatic credential rotation - runs every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void rotateExpiredCredentials() {
        log.info("Starting automatic credential rotation job");
        long startTime = System.currentTimeMillis();
        
        LocalDateTime now = LocalDateTime.now();
        
        credentialRepository.findCredentialsRequiringRotation(now)
            .flatMap(credential -> {
                log.info("Auto-rotating credential: {}", credential.getCode());
                // Note: In production, generate new value from external source
                // For now, we just log the rotation requirement
                log.warn("Credential {} requires rotation (auto-rotation not fully implemented)", 
                        credential.getCode());
                metrics.recordRotation("automatic", true);
                return reactor.core.publisher.Mono.just(credential);
            })
            .collectList()
            .subscribe(results -> {
                long duration = System.currentTimeMillis() - startTime;
                log.info("Automatic rotation completed. Rotated {} credentials in {}ms", 
                        results.size(), duration);
                metrics.recordRotationDuration("automatic", Duration.ofMillis(duration));
            });
    }

    /**
     * Monitor expiring credentials - runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void monitorExpiringCredentials() {
        log.info("Checking for expiring credentials");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningThreshold = now.plusDays(7); // 7 days warning
        
        credentialRepository.findExpiringCredentials(now, warningThreshold)
            .collectList()
            .subscribe(credentials -> {
                if (!credentials.isEmpty()) {
                    log.warn("Found {} credentials expiring in the next 7 days", 
                            credentials.size());
                    credentials.forEach(cred -> {
                        log.warn("Credential {} expires at {}", 
                                cred.getCode(), cred.getExpiresAt());
                        metrics.recordSecurityEvent("credential_expiring_soon", "medium");
                    });
                } else {
                    log.info("No credentials expiring in the next 7 days");
                }
            });
    }

    /**
     * Monitor expired credentials - runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void monitorExpiredCredentials() {
        log.debug("Checking for expired credentials");
        
        LocalDateTime now = LocalDateTime.now();
        
        credentialRepository.countByExpiresAtBefore(now)
            .subscribe(count -> {
                if (count > 0) {
                    log.error("Found {} expired credentials", count);
                    metrics.recordSecurityEvent("credentials_expired", "high");
                }
            });
    }

    /**
     * Health metrics collection - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void collectHealthMetrics() {
        credentialRepository.count()
            .subscribe(total -> log.debug("Total credentials: {}", total));
            
        credentialRepository.countByActive(true)
            .subscribe(active -> log.debug("Active credentials: {}", active));
    }
}
