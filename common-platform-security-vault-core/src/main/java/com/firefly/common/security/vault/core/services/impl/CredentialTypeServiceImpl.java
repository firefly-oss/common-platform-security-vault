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
import com.firefly.common.security.vault.core.mappers.CredentialTypeMapper;
import com.firefly.common.security.vault.core.services.CredentialTypeService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialTypeDTO;
import com.firefly.common.security.vault.models.entities.CredentialType;
import com.firefly.common.security.vault.models.repositories.CredentialTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of CredentialTypeService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialTypeServiceImpl implements CredentialTypeService {

    private final CredentialTypeRepository credentialTypeRepository;
    private final CredentialTypeMapper credentialTypeMapper;

    @Override
    public Mono<CredentialTypeDTO> getById(UUID id) {
        log.debug("Getting credential type by ID: {}", id);
        return credentialTypeRepository.findById(id)
            .map(credentialTypeMapper::toDTO)
            .doOnSuccess(dto -> log.debug("Retrieved credential type: {}", dto.getCode()))
            .doOnError(error -> log.error("Failed to retrieve credential type {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<PaginationResponse<CredentialTypeDTO>> filter(FilterRequest<CredentialTypeDTO> filterRequest) {
        log.debug("Filtering credential types with request: {}", filterRequest);
        
        // For now, return all active credential types
        // TODO: Implement proper filtering based on FilterRequest criteria
        return credentialTypeRepository.findByActive(true)
            .map(credentialTypeMapper::toDTO)
            .collectList()
            .map(list -> PaginationResponse.<CredentialTypeDTO>builder()
                .content(list)
                .totalElements((long) list.size())
                .totalPages(1)
                .currentPage(0)
                .build())
            .doOnSuccess(response -> log.debug("Filtered {} credential types", response.getTotalElements()));
    }

    @Override
    public Mono<CredentialTypeDTO> create(CredentialTypeDTO credentialTypeDTO) {
        log.info("Creating new credential type: {}", credentialTypeDTO.getCode());
        
        return Mono.fromCallable(() -> credentialTypeMapper.toEntity(credentialTypeDTO))
            .flatMap(credentialType -> {
                if (credentialType.getId() == null) {
                    credentialType.setId(UUID.randomUUID());
                }
                if (credentialType.getActive() == null) {
                    credentialType.setActive(true);
                }
                credentialType.setCreatedAt(LocalDateTime.now());
                credentialType.setUpdatedAt(LocalDateTime.now());
                return credentialTypeRepository.save(credentialType);
            })
            .map(credentialTypeMapper::toDTO)
            .doOnSuccess(dto -> log.info("Created credential type: {}", dto.getCode()))
            .doOnError(error -> log.error("Failed to create credential type: {}", error.getMessage()));
    }

    @Override
    public Mono<CredentialTypeDTO> update(UUID id, CredentialTypeDTO credentialTypeDTO) {
        log.info("Updating credential type: {}", id);
        
        return credentialTypeRepository.findById(id)
            .flatMap(existing -> {
                existing.setCode(credentialTypeDTO.getCode());
                existing.setName(credentialTypeDTO.getName());
                existing.setDescription(credentialTypeDTO.getDescription());
                existing.setCategory(credentialTypeDTO.getCategory());
                if (credentialTypeDTO.getActive() != null) {
                    existing.setActive(credentialTypeDTO.getActive());
                }
                existing.setUpdatedAt(LocalDateTime.now());
                return credentialTypeRepository.save(existing);
            })
            .map(credentialTypeMapper::toDTO)
            .doOnSuccess(dto -> log.info("Updated credential type: {}", dto.getCode()))
            .doOnError(error -> log.error("Failed to update credential type {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        log.info("Deleting credential type: {}", id);
        
        return credentialTypeRepository.findById(id)
            .flatMap(credentialType -> {
                // Soft delete by setting active to false
                credentialType.setActive(false);
                credentialType.setUpdatedAt(LocalDateTime.now());
                return credentialTypeRepository.save(credentialType);
            })
            .doOnSuccess(v -> log.info("Deleted credential type: {}", id))
            .doOnError(error -> log.error("Failed to delete credential type {}: {}", id, error.getMessage()))
            .then();
    }
}

