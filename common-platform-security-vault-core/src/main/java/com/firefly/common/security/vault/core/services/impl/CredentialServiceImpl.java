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


package com.firefly.common.security.vault.core.services.impl;

import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import com.firefly.common.security.vault.core.mappers.CredentialMapper;
import com.firefly.common.security.vault.core.services.CredentialService;
import com.firefly.common.security.vault.core.services.access.AccessControlService;
import com.firefly.common.security.vault.core.services.audit.CredentialAuditService;
import com.firefly.common.security.vault.core.ports.CredentialEncryptionPort;
import com.firefly.common.security.vault.core.utils.CredentialMaskingUtil;
import com.firefly.common.security.vault.interfaces.dtos.CredentialDTO;
import com.firefly.common.security.vault.models.entities.Credential;
import com.firefly.common.security.vault.models.repositories.CredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of CredentialService with full security integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;
    private final CredentialMapper credentialMapper;
    private final CredentialEncryptionPort credentialEncryptionPort;
    private final AccessControlService accessControlService;
    private final CredentialAuditService auditService;

    @Override
    public Mono<CredentialDTO> getById(UUID id) {
        long startTime = System.currentTimeMillis();
        
        return credentialRepository.findById(id)
            .flatMap(credential -> {
                // Log access attempt
                long duration = System.currentTimeMillis() - startTime;
                return auditService.logAccess(id, "system", null, null, "READ", "Get by ID")
                    .thenReturn(credential);
            })
            .map(credentialMapper::toDTO)
            .doOnSuccess(dto -> log.debug("Retrieved credential: {}", 
                                         CredentialMaskingUtil.safeToString("Credential", id)))
            .doOnError(error -> {
                log.error("Failed to retrieve credential {}: {}", id, error.getMessage());
                auditService.logFailedAccess(id, "system", null, null, "READ", error.getMessage())
                    .subscribe();
            });
    }

    @Override
    public Mono<PaginationResponse<CredentialDTO>> filter(FilterRequest<CredentialDTO> filterRequest) {
        // For now, return a simple implementation
        // In production, implement proper filtering with criteria
        return credentialRepository.findAll()
            .filter(credential -> Boolean.TRUE.equals(credential.getActive()))
            .map(credentialMapper::toDTO)
            .collectList()
            .map(list -> PaginationResponse.<CredentialDTO>builder()
                .content(list)
                .totalElements((long) list.size())
                .totalPages(1)
                .currentPage(0)
                .build())
            .doOnSuccess(result -> log.debug("Filtered {} credentials", result.getTotalElements()));
    }

    @Override
    public Mono<CredentialDTO> create(CredentialDTO credentialDTO) {
        return Mono.fromCallable(() -> {
            log.info("Creating new credential: {}", 
                    CredentialMaskingUtil.maskCredential(credentialDTO.getCode()));
            return credentialMapper.toEntity(credentialDTO);
        })
        .flatMap(credential -> {
            // Encrypt the credential value before saving
            return credentialEncryptionPort.encryptCredential(
                credentialDTO.getEncryptedValue(),
                credentialDTO.getEncryptionKeyId()
            )
            .flatMap(encryptionResult -> {
                // Set encryption details
                credential.setId(UUID.randomUUID());
                credential.setEncryptedValue(encryptionResult.encryptedValue());
                credential.setEncryptionIv(encryptionResult.iv());
                credential.setEncryptionAlgorithm(encryptionResult.algorithm());
                credential.setEncryptionKeyId(encryptionResult.keyId());
                credential.setCreatedAt(LocalDateTime.now());
                credential.setUpdatedAt(LocalDateTime.now());
                credential.setActive(true);
                credential.setUsageCount(0L);
                credential.setAuditAllAccess(true);
                credential.setMaskInLogs(true);
                
                return credentialRepository.save(credential);
            });
        })
        .flatMap(saved -> 
            auditService.logCredentialCreation(
                saved.getId(), 
                saved.getCreatedBy() != null ? saved.getCreatedBy().toString() : "system", 
                saved.getCode()
            ).thenReturn(saved)
        )
        .map(credentialMapper::toDTO)
        .doOnSuccess(dto -> log.info("Created credential: {}", 
                                     CredentialMaskingUtil.safeToString("Credential", dto.getId())))
        .doOnError(error -> log.error("Failed to create credential: {}", error.getMessage()));
    }

    @Override
    public Mono<CredentialDTO> update(UUID id, CredentialDTO credentialDTO) {
        return credentialRepository.findById(id)
            .flatMap(existing -> {
                // Update fields (excluding encrypted value for security)
                existing.setName(credentialDTO.getName());
                existing.setDescription(credentialDTO.getDescription());
                existing.setCredentialStatusId(credentialDTO.getCredentialStatusId());
                existing.setExpiresAt(credentialDTO.getExpiresAt());
                existing.setRotationEnabled(credentialDTO.getRotationEnabled());
                existing.setAutoRotationDays(credentialDTO.getAutoRotationDays());
                existing.setAllowedServices(credentialDTO.getAllowedServices());
                existing.setAllowedIps(credentialDTO.getAllowedIps());
                existing.setAllowedEnvironments(credentialDTO.getAllowedEnvironments());
                existing.setRequireApprovalForAccess(credentialDTO.getRequireApprovalForAccess());
                existing.setTags(credentialDTO.getTags());
                existing.setMetadata(credentialDTO.getMetadata());
                existing.setUpdatedAt(LocalDateTime.now());
                
                return credentialRepository.save(existing);
            })
            .flatMap(updated -> 
                auditService.logCredentialUpdate(
                    id, 
                    updated.getUpdatedBy() != null ? updated.getUpdatedBy().toString() : "system",
                    "Credential updated"
                ).thenReturn(updated)
            )
            .map(credentialMapper::toDTO)
            .doOnSuccess(dto -> log.info("Updated credential: {}", 
                                         CredentialMaskingUtil.safeToString("Credential", id)))
            .doOnError(error -> log.error("Failed to update credential {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return credentialRepository.findById(id)
            .flatMap(credential -> {
                // Soft delete by setting active to false
                credential.setActive(false);
                credential.setUpdatedAt(LocalDateTime.now());
                return credentialRepository.save(credential);
            })
            .flatMap(deleted -> 
                auditService.logCredentialDeletion(
                    id, 
                    deleted.getUpdatedBy() != null ? deleted.getUpdatedBy().toString() : "system",
                    "Credential deleted"
                )
            )
            .doOnSuccess(v -> log.info("Deleted credential: {}", 
                                       CredentialMaskingUtil.safeToString("Credential", id)))
            .doOnError(error -> log.error("Failed to delete credential {}: {}", id, error.getMessage()))
            .then();
    }

    /**
     * Decrypt and retrieve credential value (with access control and audit)
     * 
     * @param id Credential ID
     * @param accessRequest Access request context
     * @return Decrypted credential value
     */
    public Mono<String> getDecryptedValue(UUID id, AccessControlService.AccessRequest accessRequest) {
        long startTime = System.currentTimeMillis();
        
        return credentialRepository.findById(id)
            .flatMap(credential -> {
                // Validate access
                return accessControlService.validateAccess(credential, accessRequest)
                    .flatMap(decision -> {
                        if (!decision.allowed()) {
                            long duration = System.currentTimeMillis() - startTime;
                            return auditService.logDeniedAccess(
                                id, 
                                accessRequest.userId(), 
                                accessRequest.serviceName(),
                                accessRequest.ipAddress(),
                                "DECRYPT",
                                decision.denyReason()
                            ).then(Mono.error(new SecurityException("Access denied: " + decision.denyReason())));
                        }
                        
                        // Decrypt credential
                        return credentialEncryptionPort.decryptCredential(
                            credential.getEncryptedValue(),
                            credential.getEncryptionKeyId(),
                            credential.getEncryptionIv()
                        )
                        .flatMap(decryptedValue -> {
                            long duration = System.currentTimeMillis() - startTime;
                            
                            // Update usage tracking
                            credential.setLastUsedAt(LocalDateTime.now());
                            credential.setUsageCount(credential.getUsageCount() + 1);
                            credential.setLastAccessedBy(accessRequest.userId());
                            
                            return credentialRepository.save(credential)
                                .then(auditService.logDecryption(
                                    id,
                                    accessRequest.userId(),
                                    accessRequest.serviceName(),
                                    accessRequest.ipAddress(),
                                    true,
                                    duration
                                ))
                                .thenReturn(decryptedValue);
                        });
                    });
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Failed to decrypt credential {}: {}", id, error.getMessage());
                auditService.logDecryption(
                    id,
                    accessRequest.userId(),
                    accessRequest.serviceName(),
                    accessRequest.ipAddress(),
                    false,
                    duration
                ).subscribe();
            });
    }
}
