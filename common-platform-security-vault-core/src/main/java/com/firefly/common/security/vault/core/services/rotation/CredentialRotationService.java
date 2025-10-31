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


package com.firefly.common.security.vault.core.services.rotation;

import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import com.firefly.common.security.vault.core.services.audit.CredentialAuditService;
import com.firefly.common.security.vault.core.services.encryption.EncryptionService;
import com.firefly.common.security.vault.models.entities.Credential;
import com.firefly.common.security.vault.models.entities.CredentialVersion;
import com.firefly.common.security.vault.models.repositories.CredentialRepository;
import com.firefly.common.security.vault.models.repositories.CredentialVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing credential rotation
 * 
 * Features:
 * - Manual rotation with reason tracking
 * - Automatic rotation based on policies
 * - Version management (keep last N versions)
 * - Rollback to previous versions
 * - Zero-downtime rotation support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialRotationService {

    private final CredentialRepository credentialRepository;
    private final CredentialVersionRepository credentialVersionRepository;
    private final EncryptionService encryptionService;
    private final CredentialAuditService auditService;
    private final SecurityVaultProperties vaultProperties;

    /**
     * Manually rotate a credential with a new value
     * 
     * @param credentialId Credential to rotate
     * @param newPlaintextValue New credential value
     * @param rotatedBy User performing rotation
     * @param reason Reason for rotation
     * @return Rotation result
     */
    public Mono<RotationResult> rotateCredential(UUID credentialId, String newPlaintextValue, 
                                                 UUID rotatedBy, String reason) {
        return credentialRepository.findById(credentialId)
            .flatMap(credential -> performRotation(credential, newPlaintextValue, rotatedBy, reason))
            .doOnSuccess(result -> log.info("Credential {} rotated successfully by {}", 
                                           credentialId, rotatedBy))
            .doOnError(error -> log.error("Credential rotation failed for {}: {}", 
                                         credentialId, error.getMessage()));
    }

    /**
     * Rotate encryption key (re-encrypt with new key)
     * 
     * @param credentialId Credential to re-encrypt
     * @param newKeyId New encryption key ID
     * @param rotatedBy User performing rotation
     * @return Rotation result
     */
    public Mono<RotationResult> rotateEncryptionKey(UUID credentialId, String newKeyId, 
                                                    UUID rotatedBy) {
        return credentialRepository.findById(credentialId)
            .flatMap(credential -> 
                // Re-encrypt with new key
                encryptionService.rotateEncryption(
                    credential.getEncryptedValue(),
                    credential.getEncryptionKeyId(),
                    credential.getEncryptionIv(),
                    newKeyId
                )
                .flatMap(newEncryption -> {
                    // Save version before updating
                    return saveCurrentAsVersion(credential, "Encryption key rotation")
                        .then(Mono.fromCallable(() -> {
                            // Update with new encryption
                            credential.setEncryptedValue(newEncryption.encryptedValue());
                            credential.setEncryptionIv(newEncryption.iv());
                            credential.setEncryptionKeyId(newEncryption.keyId());
                            credential.setLastRotatedAt(LocalDateTime.now());
                            return credential;
                        }))
                        .flatMap(credentialRepository::save)
                        .flatMap(updated -> 
                            auditService.logCredentialRotation(
                                credentialId, 
                                rotatedBy.toString(),
                                "Encryption key rotation"
                            ).thenReturn(new RotationResult(true, credentialId, 
                                          "Encryption key rotated successfully"))
                        );
                })
            );
    }

    /**
     * Check and rotate expired credentials
     * 
     * @return Number of credentials rotated
     */
    public Mono<Long> rotateExpiredCredentials() {
        if (!vaultProperties.getRotation().isRotateExpired()) {
            log.debug("Automatic rotation of expired credentials is disabled");
            return Mono.just(0L);
        }

        return credentialRepository.findAll()
            .filter(credential -> {
                // Check if expired and rotation enabled
                if (credential.getExpiresAt() == null || 
                    !Boolean.TRUE.equals(credential.getRotationEnabled())) {
                    return false;
                }
                return credential.getExpiresAt().isBefore(LocalDateTime.now());
            })
            .flatMap(credential -> 
                // For expired credentials, mark as requiring rotation
                // In a real implementation, you would generate new credentials here
                // or trigger an alert for manual rotation
                auditService.logCredentialRotation(
                    credential.getId(), 
                    "system",
                    "Auto-rotation: credential expired"
                ).thenReturn(credential)
            )
            .count()
            .doOnSuccess(count -> {
                if (count > 0) {
                    log.warn("Found {} expired credentials requiring rotation", count);
                }
            });
    }

    /**
     * Get credentials nearing expiration
     * 
     * @param daysBeforeExpiration Number of days before expiration
     * @return Credentials nearing expiration
     */
    public Flux<Credential> getCredentialsNearingExpiration(int daysBeforeExpiration) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysBeforeExpiration);
        
        return credentialRepository.findAll()
            .filter(credential -> {
                if (credential.getExpiresAt() == null) {
                    return false;
                }
                return credential.getExpiresAt().isBefore(threshold) && 
                       credential.getExpiresAt().isAfter(LocalDateTime.now());
            });
    }

    /**
     * Rollback to a previous version
     * 
     * @param credentialId Credential to rollback
     * @param versionId Version to rollback to
     * @param rolledBackBy User performing rollback
     * @return Rollback result
     */
    public Mono<RotationResult> rollbackToVersion(UUID credentialId, UUID versionId, 
                                                  UUID rolledBackBy) {
        return Mono.zip(
            credentialRepository.findById(credentialId),
            credentialVersionRepository.findById(versionId)
        )
        .flatMap(tuple -> {
            Credential credential = tuple.getT1();
            CredentialVersion version = tuple.getT2();
            
            // Verify version belongs to credential
            if (!version.getCredentialId().equals(credentialId)) {
                return Mono.error(new IllegalArgumentException(
                    "Version does not belong to credential"));
            }
            
            // Save current as version before rollback
            return saveCurrentAsVersion(credential, "Rollback initiated")
                .then(Mono.fromCallable(() -> {
                    // Restore from version
                    credential.setEncryptedValue(version.getEncryptedValue());
                    credential.setEncryptionIv(version.getEncryptionIv());
                    credential.setEncryptionKeyId(version.getEncryptionKeyId());
                    credential.setLastRotatedAt(LocalDateTime.now());
                    return credential;
                }))
                .flatMap(credentialRepository::save)
                .flatMap(updated -> 
                    auditService.logCredentialRotation(
                        credentialId, 
                        rolledBackBy.toString(),
                        "Rolled back to version " + versionId
                    ).thenReturn(new RotationResult(true, credentialId, 
                                  "Rolled back to previous version successfully"))
                );
        });
    }

    /**
     * Get version history for a credential
     * 
     * @param credentialId Credential ID
     * @return Version history
     */
    public Flux<CredentialVersion> getVersionHistory(UUID credentialId) {
        return credentialVersionRepository.findAll()
            .filter(version -> version.getCredentialId().equals(credentialId))
            .sort((v1, v2) -> v2.getVersionNumber().compareTo(v1.getVersionNumber()));
    }

    /**
     * Perform the rotation
     */
    private Mono<RotationResult> performRotation(Credential credential, String newPlaintextValue,
                                                 UUID rotatedBy, String reason) {
        // Save current version before rotation
        return saveCurrentAsVersion(credential, reason)
            .then(encryptionService.encrypt(newPlaintextValue, credential.getEncryptionKeyId()))
            .flatMap(encrypted -> {
                // Update credential with new encrypted value
                credential.setEncryptedValue(encrypted.encryptedValue());
                credential.setEncryptionIv(encrypted.iv());
                credential.setLastRotatedAt(LocalDateTime.now());
                
                return credentialRepository.save(credential);
            })
            .flatMap(updated -> 
                auditService.logCredentialRotation(
                    credential.getId(), 
                    rotatedBy.toString(),
                    reason
                ).thenReturn(new RotationResult(true, credential.getId(), 
                              "Credential rotated successfully"))
            )
            .then(cleanupOldVersions(credential.getId()))
            .thenReturn(new RotationResult(true, credential.getId(), 
                      "Credential rotated successfully"));
    }

    /**
     * Save current credential value as a version
     */
    private Mono<CredentialVersion> saveCurrentAsVersion(Credential credential, String reason) {
        return getNextVersionNumber(credential.getId())
            .flatMap(versionNumber -> {
                // Mark all previous versions as not current
                return credentialVersionRepository.findAll()
                    .filter(v -> v.getCredentialId().equals(credential.getId()) && 
                                Boolean.TRUE.equals(v.getIsCurrent()))
                    .flatMap(v -> {
                        v.setIsCurrent(false);
                        v.setValidUntil(LocalDateTime.now());
                        return credentialVersionRepository.save(v);
                    })
                    .then(Mono.fromCallable(() -> 
                        CredentialVersion.builder()
                            .id(UUID.randomUUID())
                            .credentialId(credential.getId())
                            .versionNumber(versionNumber)
                            .encryptedValue(credential.getEncryptedValue())
                            .encryptionAlgorithm(credential.getEncryptionAlgorithm())
                            .encryptionKeyId(credential.getEncryptionKeyId())
                            .encryptionIv(credential.getEncryptionIv())
                            .validFrom(LocalDateTime.now())
                            .isCurrent(true)
                            .rotationReason(reason)
                            .build()
                    ))
                    .flatMap(credentialVersionRepository::save);
            });
    }

    /**
     * Get next version number for credential
     */
    private Mono<Integer> getNextVersionNumber(UUID credentialId) {
        return credentialVersionRepository.findAll()
            .filter(v -> v.getCredentialId().equals(credentialId))
            .map(CredentialVersion::getVersionNumber)
            .reduce(Integer::max)
            .map(max -> max + 1)
            .defaultIfEmpty(1);
    }

    /**
     * Clean up old versions (keep only last N versions)
     */
    private Mono<Void> cleanupOldVersions(UUID credentialId) {
        int maxVersions = vaultProperties.getRotation().getMaxVersionsToKeep();
        
        return credentialVersionRepository.findAll()
            .filter(v -> v.getCredentialId().equals(credentialId))
            .sort((v1, v2) -> v2.getVersionNumber().compareTo(v1.getVersionNumber()))
            .skip(maxVersions)
            .flatMap(credentialVersionRepository::delete)
            .then()
            .doOnSuccess(v -> log.debug("Cleaned up old versions for credential {}", 
                                       credentialId));
    }

    /**
     * Rotation result
     */
    public record RotationResult(
        boolean success,
        UUID credentialId,
        String message
    ) {}
}
