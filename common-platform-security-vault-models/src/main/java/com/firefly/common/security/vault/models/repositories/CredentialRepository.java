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

import com.firefly.common.security.vault.models.entities.Credential;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for Credential entity
 */
@Repository
public interface CredentialRepository extends R2dbcRepository<Credential, UUID> {
    
    /**
     * Count credentials by active status
     */
    Mono<Long> countByActive(Boolean active);
    
    /**
     * Count expired credentials
     */
    Mono<Long> countByExpiresAtBefore(LocalDateTime dateTime);
    
    /**
     * Find credentials by tenant
     */
    Flux<Credential> findByTenantId(UUID tenantId);
    
    /**
     * Find credentials by code
     */
    Mono<Credential> findByCode(String code);
    
    /**
     * Find active credentials by tenant
     */
    Flux<Credential> findByTenantIdAndActive(UUID tenantId, Boolean active);
    
    /**
     * Find expiring credentials
     */
    @Query("SELECT * FROM credentials WHERE expires_at BETWEEN :startDate AND :endDate AND active = true")
    Flux<Credential> findExpiringCredentials(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find credentials requiring rotation
     */
    @Query("SELECT * FROM credentials WHERE rotation_enabled = true AND " +
           "(last_rotated_at IS NULL OR last_rotated_at < :rotationDate) AND active = true")
    Flux<Credential> findCredentialsRequiringRotation(LocalDateTime rotationDate);
}

