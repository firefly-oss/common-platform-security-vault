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

import com.firefly.common.security.vault.core.mappers.CredentialVersionMapper;
import com.firefly.common.security.vault.core.services.CredentialVersionService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialVersionDTO;
import com.firefly.common.security.vault.models.repositories.CredentialVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialVersionServiceImpl implements CredentialVersionService {

    private final CredentialVersionRepository credentialVersionRepository;
    private final CredentialVersionMapper credentialVersionMapper;

    @Override
    public Mono<CredentialVersionDTO> getById(UUID id) {
        return credentialVersionRepository.findById(id)
            .map(credentialVersionMapper::toDTO);
    }

    @Override
    public Flux<CredentialVersionDTO> getVersionsByCredentialId(UUID credentialId) {
        return credentialVersionRepository.findByCredentialIdOrderByVersionNumberDesc(credentialId)
            .map(credentialVersionMapper::toDTO);
    }

    @Override
    public Mono<CredentialVersionDTO> getCurrentVersion(UUID credentialId) {
        return credentialVersionRepository.findByCredentialIdAndIsCurrent(credentialId, true)
            .map(credentialVersionMapper::toDTO);
    }

    @Override
    public Mono<CredentialVersionDTO> create(CredentialVersionDTO credentialVersionDTO) {
        return Mono.fromCallable(() -> credentialVersionMapper.toEntity(credentialVersionDTO))
            .flatMap(entity -> {
                if (entity.getId() == null) entity.setId(UUID.randomUUID());
                if (entity.getIsCurrent() == null) entity.setIsCurrent(false);
                if (entity.getValidFrom() == null) entity.setValidFrom(LocalDateTime.now());
                entity.setCreatedAt(LocalDateTime.now());
                return credentialVersionRepository.save(entity);
            })
            .map(credentialVersionMapper::toDTO);
    }
}

