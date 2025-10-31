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

import com.firefly.common.security.vault.interfaces.dtos.CredentialStatusDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing credential statuses
 */
public interface CredentialStatusService {

    /**
     * Get a credential status by ID
     * @param id Credential Status ID
     * @return Credential Status DTO
     */
    Mono<CredentialStatusDTO> getById(UUID id);

    /**
     * Filter credential statuses based on criteria
     * @param filterRequest Filter criteria
     * @return Paginated list of credential statuses
     */
    Mono<PaginationResponse<CredentialStatusDTO>> filter(FilterRequest<CredentialStatusDTO> filterRequest);

    /**
     * Create a new credential status
     * @param credentialStatusDTO Credential Status DTO
     * @return Created credential status DTO
     */
    Mono<CredentialStatusDTO> create(CredentialStatusDTO credentialStatusDTO);

    /**
     * Update an existing credential status
     * @param id Credential Status ID
     * @param credentialStatusDTO Credential Status DTO
     * @return Updated credential status DTO
     */
    Mono<CredentialStatusDTO> update(UUID id, CredentialStatusDTO credentialStatusDTO);

    /**
     * Delete a credential status
     * @param id Credential Status ID
     * @return Void
     */
    Mono<Void> delete(UUID id);
}

