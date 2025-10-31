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

import com.firefly.common.security.vault.interfaces.dtos.EncryptionKeyDTO;
import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Service interface for managing encryption keys
 */
public interface EncryptionKeyService {

    /**
     * Get an encryption key by ID
     * @param id Encryption Key ID
     * @return Encryption Key DTO
     */
    Mono<EncryptionKeyDTO> getById(UUID id);

    /**
     * Filter encryption keys based on criteria
     * @param filterRequest Filter criteria
     * @return Paginated list of encryption keys
     */
    Mono<PaginationResponse<EncryptionKeyDTO>> filter(FilterRequest<EncryptionKeyDTO> filterRequest);

    /**
     * Create a new encryption key
     * @param encryptionKeyDTO Encryption Key DTO
     * @return Created encryption key DTO
     */
    Mono<EncryptionKeyDTO> create(EncryptionKeyDTO encryptionKeyDTO);

    /**
     * Update an existing encryption key
     * @param id Encryption Key ID
     * @param encryptionKeyDTO Encryption Key DTO
     * @return Updated encryption key DTO
     */
    Mono<EncryptionKeyDTO> update(UUID id, EncryptionKeyDTO encryptionKeyDTO);

    /**
     * Delete an encryption key
     * @param id Encryption Key ID
     * @return Void
     */
    Mono<Void> delete(UUID id);
}

