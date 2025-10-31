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


package com.firefly.common.security.vault.models.repositories;

import com.firefly.common.security.vault.models.entities.EncryptionKey;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for EncryptionKey entity
 */
@Repository
public interface EncryptionKeyRepository extends R2dbcRepository<EncryptionKey, UUID> {
    
    /**
     * Find encryption key by key ID
     */
    Mono<EncryptionKey> findByKeyId(String keyId);
    
    /**
     * Find all active encryption keys
     */
    Flux<EncryptionKey> findByActive(Boolean active);
    
    /**
     * Find master keys
     */
    Flux<EncryptionKey> findByIsMasterKeyAndActive(Boolean isMasterKey, Boolean active);
    
    /**
     * Find keys by provider
     */
    Flux<EncryptionKey> findByKeyProviderAndActive(String keyProvider, Boolean active);
    
    /**
     * Find expiring keys
     */
    @Query("SELECT * FROM encryption_keys WHERE expires_at BETWEEN :startDate AND :endDate AND active = true")
    Flux<EncryptionKey> findExpiringKeys(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find keys requiring rotation
     */
    @Query("SELECT * FROM encryption_keys WHERE rotation_schedule_days IS NOT NULL AND " +
           "(rotated_at IS NULL OR rotated_at < :rotationDate) AND active = true")
    Flux<EncryptionKey> findKeysRequiringRotation(LocalDateTime rotationDate);
}

