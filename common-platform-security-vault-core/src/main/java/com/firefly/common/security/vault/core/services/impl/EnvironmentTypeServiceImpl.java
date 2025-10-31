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
import com.firefly.common.security.vault.core.mappers.EnvironmentTypeMapper;
import com.firefly.common.security.vault.core.services.EnvironmentTypeService;
import com.firefly.common.security.vault.interfaces.dtos.EnvironmentTypeDTO;
import com.firefly.common.security.vault.models.repositories.EnvironmentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentTypeServiceImpl implements EnvironmentTypeService {

    private final EnvironmentTypeRepository environmentTypeRepository;
    private final EnvironmentTypeMapper environmentTypeMapper;

    @Override
    public Mono<EnvironmentTypeDTO> getById(UUID id) {
        return environmentTypeRepository.findById(id)
            .map(environmentTypeMapper::toDTO);
    }

    @Override
    public Mono<PaginationResponse<EnvironmentTypeDTO>> filter(FilterRequest<EnvironmentTypeDTO> filterRequest) {
        return environmentTypeRepository.findByActive(true)
            .map(environmentTypeMapper::toDTO)
            .collectList()
            .map(list -> PaginationResponse.<EnvironmentTypeDTO>builder()
                .content(list)
                .totalElements((long) list.size())
                .totalPages(1)
                .currentPage(0)
                .build());
    }

    @Override
    public Mono<EnvironmentTypeDTO> create(EnvironmentTypeDTO environmentTypeDTO) {
        return Mono.fromCallable(() -> environmentTypeMapper.toEntity(environmentTypeDTO))
            .flatMap(entity -> {
                if (entity.getId() == null) entity.setId(UUID.randomUUID());
                if (entity.getActive() == null) entity.setActive(true);
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                return environmentTypeRepository.save(entity);
            })
            .map(environmentTypeMapper::toDTO);
    }

    @Override
    public Mono<EnvironmentTypeDTO> update(UUID id, EnvironmentTypeDTO environmentTypeDTO) {
        return environmentTypeRepository.findById(id)
            .flatMap(existing -> {
                existing.setCode(environmentTypeDTO.getCode());
                existing.setName(environmentTypeDTO.getName());
                existing.setDescription(environmentTypeDTO.getDescription());
                if (environmentTypeDTO.getActive() != null) existing.setActive(environmentTypeDTO.getActive());
                existing.setUpdatedAt(LocalDateTime.now());
                return environmentTypeRepository.save(existing);
            })
            .map(environmentTypeMapper::toDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return environmentTypeRepository.findById(id)
            .flatMap(entity -> {
                entity.setActive(false);
                entity.setUpdatedAt(LocalDateTime.now());
                return environmentTypeRepository.save(entity);
            })
            .then();
    }
}

