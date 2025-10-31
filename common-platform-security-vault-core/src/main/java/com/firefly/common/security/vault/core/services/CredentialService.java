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

import com.firefly.common.security.vault.interfaces.dtos.CredentialDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing credentials
 */
public interface CredentialService {

    /**
     * Get a credential by ID
     * @param id Credential ID
     * @return Credential DTO
     */
    Mono<CredentialDTO> getById(UUID id);

    /**
     * Filter credentials based on criteria
     * @param filterRequest Filter criteria
     * @return Paginated list of credentials
     */
    Mono<PaginationResponse<CredentialDTO>> filter(FilterRequest<CredentialDTO> filterRequest);

    /**
     * Create a new credential
     * @param credentialDTO Credential DTO
     * @return Created credential DTO
     */
    Mono<CredentialDTO> create(CredentialDTO credentialDTO);

    /**
     * Update an existing credential
     * @param id Credential ID
     * @param credentialDTO Credential DTO
     * @return Updated credential DTO
     */
    Mono<CredentialDTO> update(UUID id, CredentialDTO credentialDTO);

    /**
     * Delete a credential
     * @param id Credential ID
     * @return Void
     */
    Mono<Void> delete(UUID id);
}

