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


package com.firefly.common.security.vault.core.services;

import com.firefly.common.security.vault.interfaces.dtos.EnvironmentTypeDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing environment types
 */
public interface EnvironmentTypeService {

    /**
     * Get an environment type by ID
     * @param id Environment Type ID
     * @return Environment Type DTO
     */
    Mono<EnvironmentTypeDTO> getById(UUID id);

    /**
     * Filter environment types based on criteria
     * @param filterRequest Filter criteria
     * @return Paginated list of environment types
     */
    Mono<PaginationResponse<EnvironmentTypeDTO>> filter(FilterRequest<EnvironmentTypeDTO> filterRequest);

    /**
     * Create a new environment type
     * @param environmentTypeDTO Environment Type DTO
     * @return Created environment type DTO
     */
    Mono<EnvironmentTypeDTO> create(EnvironmentTypeDTO environmentTypeDTO);

    /**
     * Update an existing environment type
     * @param id Environment Type ID
     * @param environmentTypeDTO Environment Type DTO
     * @return Updated environment type DTO
     */
    Mono<EnvironmentTypeDTO> update(UUID id, EnvironmentTypeDTO environmentTypeDTO);

    /**
     * Delete an environment type
     * @param id Environment Type ID
     * @return Void
     */
    Mono<Void> delete(UUID id);
}

