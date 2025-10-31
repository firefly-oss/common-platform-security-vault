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
import com.firefly.common.security.vault.core.mappers.EncryptionKeyMapper;
import com.firefly.common.security.vault.core.services.EncryptionKeyService;
import com.firefly.common.security.vault.interfaces.dtos.EncryptionKeyDTO;
import com.firefly.common.security.vault.models.repositories.EncryptionKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionKeyServiceImpl implements EncryptionKeyService {

    private final EncryptionKeyRepository encryptionKeyRepository;
    private final EncryptionKeyMapper encryptionKeyMapper;

    @Override
    public Mono<EncryptionKeyDTO> getById(UUID id) {
        return encryptionKeyRepository.findById(id)
            .map(encryptionKeyMapper::toDTO);
    }

    @Override
    public Mono<PaginationResponse<EncryptionKeyDTO>> filter(FilterRequest<EncryptionKeyDTO> filterRequest) {
        return encryptionKeyRepository.findByActive(true)
            .map(encryptionKeyMapper::toDTO)
            .collectList()
            .map(list -> PaginationResponse.<EncryptionKeyDTO>builder()
                .content(list)
                .totalElements((long) list.size())
                .totalPages(1)
                .currentPage(0)
                .build());
    }

    @Override
    public Mono<EncryptionKeyDTO> create(EncryptionKeyDTO encryptionKeyDTO) {
        return Mono.fromCallable(() -> encryptionKeyMapper.toEntity(encryptionKeyDTO))
            .flatMap(entity -> {
                if (entity.getId() == null) entity.setId(UUID.randomUUID());
                if (entity.getActive() == null) entity.setActive(true);
                if (entity.getIsMasterKey() == null) entity.setIsMasterKey(false);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                return encryptionKeyRepository.save(entity);
            })
            .map(encryptionKeyMapper::toDTO);
    }

    @Override
    public Mono<EncryptionKeyDTO> update(UUID id, EncryptionKeyDTO encryptionKeyDTO) {
        return encryptionKeyRepository.findById(id)
            .flatMap(existing -> {
                existing.setKeyName(encryptionKeyDTO.getKeyName());
                existing.setKeyType(encryptionKeyDTO.getKeyType());
                existing.setKeyAlgorithm(encryptionKeyDTO.getKeyAlgorithm());
                existing.setKeyProvider(encryptionKeyDTO.getKeyProvider());
                existing.setKeyLocation(encryptionKeyDTO.getKeyLocation());
                existing.setKeyStatus(encryptionKeyDTO.getKeyStatus());
                existing.setKeyPurpose(encryptionKeyDTO.getKeyPurpose());
                existing.setExpiresAt(encryptionKeyDTO.getExpiresAt());
                existing.setRotationScheduleDays(encryptionKeyDTO.getRotationScheduleDays());
                existing.setMetadata(encryptionKeyDTO.getMetadata());
                if (encryptionKeyDTO.getActive() != null) existing.setActive(encryptionKeyDTO.getActive());
                existing.setUpdatedAt(LocalDateTime.now());
                return encryptionKeyRepository.save(existing);
            })
            .map(encryptionKeyMapper::toDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return encryptionKeyRepository.findById(id)
            .flatMap(entity -> {
                entity.setActive(false);
                entity.setUpdatedAt(LocalDateTime.now());
                return encryptionKeyRepository.save(entity);
            })
            .then();
    }
}

