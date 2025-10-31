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


package com.firefly.common.security.vault.core.services.audit;

import com.firefly.common.security.vault.models.entities.CredentialAccessLog;
import com.firefly.common.security.vault.models.repositories.CredentialAccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for auditing all credential access and operations
 * 
 * Audit Features:
 * - Log all credential access attempts (successful and failed)
 * - Track decryption operations
 * - Record user, service, IP, and timestamp
 * - Measure access duration for performance monitoring
 * - Support compliance and security investigations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialAuditService {

    private final CredentialAccessLogRepository accessLogRepository;

    /**
     * Log a successful credential access
     */
    public Mono<Void> logAccess(UUID credentialId, String accessedBy, String accessedByService, 
                                String accessIp, String accessType, String accessReason) {
        return logAccess(credentialId, accessedBy, accessedByService, accessIp, 
                        accessType, accessReason, "SUCCESS", null, true, null);
    }

    /**
     * Log a failed credential access attempt
     */
    public Mono<Void> logFailedAccess(UUID credentialId, String accessedBy, String accessedByService,
                                      String accessIp, String accessType, String errorMessage) {
        return logAccess(credentialId, accessedBy, accessedByService, accessIp,
                        accessType, null, "FAILED", errorMessage, false, null);
    }

    /**
     * Log a denied credential access attempt (permission issue)
     */
    public Mono<Void> logDeniedAccess(UUID credentialId, String accessedBy, String accessedByService,
                                      String accessIp, String accessType, String denyReason) {
        return logAccess(credentialId, accessedBy, accessedByService, accessIp,
                        accessType, null, "DENIED", denyReason, false, null);
    }

    /**
     * Comprehensive access logging
     */
    private Mono<Void> logAccess(UUID credentialId, String accessedBy, String accessedByService,
                                 String accessIp, String accessType, String accessReason,
                                 String accessResult, String errorMessage, Boolean decryptionSuccessful,
                                 Integer durationMs) {
        
        return Mono.fromCallable(() -> {
            CredentialAccessLog accessLog = CredentialAccessLog.builder()
                .id(UUID.randomUUID())
                .credentialId(credentialId)
                .accessType(accessType)
                .accessedBy(accessedBy)
                .accessedByService(accessedByService)
                .accessIp(accessIp)
                .accessResult(accessResult)
                .accessReason(accessReason)
                .decryptionSuccessful(decryptionSuccessful)
                .errorMessage(errorMessage)
                .accessDurationMs(durationMs)
                .accessedAt(LocalDateTime.now())
                .build();
            
            return accessLog;
        })
        .flatMap(accessLogRepository::save)
        .doOnSuccess(saved -> log.info("Audit log created: credentialId={}, type={}, result={}, user={}",
                                      credentialId, accessType, accessResult, accessedBy))
        .doOnError(error -> log.error("Failed to create audit log for credentialId={}: {}", 
                                     credentialId, error.getMessage()))
        .then();
    }

    /**
     * Log credential creation
     */
    public Mono<Void> logCredentialCreation(UUID credentialId, String createdBy, String credentialCode) {
        log.info("Credential created: id={}, code={}, by={}", credentialId, credentialCode, createdBy);
        return logAccess(credentialId, createdBy, null, null, "CREATE", 
                        "Credential created", "SUCCESS", null, null, null);
    }

    /**
     * Log credential update
     */
    public Mono<Void> logCredentialUpdate(UUID credentialId, String updatedBy, String updateReason) {
        log.info("Credential updated: id={}, by={}, reason={}", credentialId, updatedBy, updateReason);
        return logAccess(credentialId, updatedBy, null, null, "UPDATE",
                        updateReason, "SUCCESS", null, null, null);
    }

    /**
     * Log credential rotation
     */
    public Mono<Void> logCredentialRotation(UUID credentialId, String rotatedBy, String rotationReason) {
        log.info("Credential rotated: id={}, by={}, reason={}", credentialId, rotatedBy, rotationReason);
        return logAccess(credentialId, rotatedBy, null, null, "ROTATE",
                        rotationReason, "SUCCESS", null, null, null);
    }

    /**
     * Log credential deletion
     */
    public Mono<Void> logCredentialDeletion(UUID credentialId, String deletedBy, String deletionReason) {
        log.warn("Credential deleted: id={}, by={}, reason={}", credentialId, deletedBy, deletionReason);
        return logAccess(credentialId, deletedBy, null, null, "DELETE",
                        deletionReason, "SUCCESS", null, null, null);
    }

    /**
     * Log credential decryption with timing
     */
    public Mono<Void> logDecryption(UUID credentialId, String accessedBy, String accessedByService,
                                    String accessIp, boolean success, long durationMs) {
        return logAccess(credentialId, accessedBy, accessedByService, accessIp, "DECRYPT",
                        "Credential decrypted", success ? "SUCCESS" : "FAILED",
                        success ? null : "Decryption failed", success, (int) durationMs);
    }
}
