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
import com.firefly.common.security.vault.core.mappers.CredentialStatusMapper;
import com.firefly.common.security.vault.core.services.CredentialStatusService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialStatusDTO;
import com.firefly.common.security.vault.models.repositories.CredentialStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialStatusServiceImpl implements CredentialStatusService {

    private final CredentialStatusRepository credentialStatusRepository;
    private final CredentialStatusMapper credentialStatusMapper;

    @Override
    public Mono<CredentialStatusDTO> getById(UUID id) {
        return credentialStatusRepository.findById(id)
            .map(credentialStatusMapper::toDTO);
    }

    @Override
    public Mono<PaginationResponse<CredentialStatusDTO>> filter(FilterRequest<CredentialStatusDTO> filterRequest) {
        return credentialStatusRepository.findByActive(true)
            .map(credentialStatusMapper::toDTO)
            .collectList()
            .map(list -> PaginationResponse.<CredentialStatusDTO>builder()
                .content(list)
                .totalElements((long) list.size())
                .totalPages(1)
                .currentPage(0)
                .build());
    }

    @Override
    public Mono<CredentialStatusDTO> create(CredentialStatusDTO credentialStatusDTO) {
        return Mono.fromCallable(() -> credentialStatusMapper.toEntity(credentialStatusDTO))
            .flatMap(entity -> {
                if (entity.getId() == null) entity.setId(UUID.randomUUID());
                if (entity.getActive() == null) entity.setActive(true);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                return credentialStatusRepository.save(entity);
            })
            .map(credentialStatusMapper::toDTO);
    }

    @Override
    public Mono<CredentialStatusDTO> update(UUID id, CredentialStatusDTO credentialStatusDTO) {
        return credentialStatusRepository.findById(id)
            .flatMap(existing -> {
                existing.setCode(credentialStatusDTO.getCode());
                existing.setName(credentialStatusDTO.getName());
                existing.setDescription(credentialStatusDTO.getDescription());
                if (credentialStatusDTO.getActive() != null) existing.setActive(credentialStatusDTO.getActive());
                existing.setUpdatedAt(LocalDateTime.now());
                return credentialStatusRepository.save(existing);
            })
            .map(credentialStatusMapper::toDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return credentialStatusRepository.findById(id)
            .flatMap(entity -> {
                entity.setActive(false);
                entity.setUpdatedAt(LocalDateTime.now());
                return credentialStatusRepository.save(entity);
            })
            .then();
    }
}

