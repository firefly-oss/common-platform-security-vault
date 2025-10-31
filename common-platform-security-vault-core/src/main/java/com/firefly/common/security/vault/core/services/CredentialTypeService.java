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

import com.firefly.common.security.vault.interfaces.dtos.CredentialTypeDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing credential types
 */
public interface CredentialTypeService {

    /**
     * Get a credential type by ID
     * @param id Credential Type ID
     * @return Credential Type DTO
     */
    Mono<CredentialTypeDTO> getById(UUID id);

    /**
     * Filter credential types based on criteria
     * @param filterRequest Filter criteria
     * @return Paginated list of credential types
     */
    Mono<PaginationResponse<CredentialTypeDTO>> filter(FilterRequest<CredentialTypeDTO> filterRequest);

    /**
     * Create a new credential type
     * @param credentialTypeDTO Credential Type DTO
     * @return Created credential type DTO
     */
    Mono<CredentialTypeDTO> create(CredentialTypeDTO credentialTypeDTO);

    /**
     * Update an existing credential type
     * @param id Credential Type ID
     * @param credentialTypeDTO Credential Type DTO
     * @return Updated credential type DTO
     */
    Mono<CredentialTypeDTO> update(UUID id, CredentialTypeDTO credentialTypeDTO);

    /**
     * Delete a credential type
     * @param id Credential Type ID
     * @return Void
     */
    Mono<Void> delete(UUID id);
}

