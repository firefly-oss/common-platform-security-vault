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


package com.firefly.common.security.vault.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing encryption key metadata
 * Note: The actual key material is stored in KMS, not in the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("encryption_keys")
public class EncryptionKey {

    @Id
    private UUID id;

    @Column("key_id")
    private String keyId;

    @Column("key_name")
    private String keyName;

    @Column("key_type")
    private String keyType;

    @Column("key_algorithm")
    private String keyAlgorithm;

    @Column("key_provider")
    private String keyProvider;

    @Column("key_location")
    private String keyLocation;

    @Column("key_status")
    private String keyStatus;

    @Column("is_master_key")
    private Boolean isMasterKey;

    @Column("key_purpose")
    private String keyPurpose;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("rotated_at")
    private LocalDateTime rotatedAt;

    @Column("rotation_schedule_days")
    private Integer rotationScheduleDays;

    @Column("metadata")
    private String metadata;

    @Column("active")
    private Boolean active;

    @Version
    @Column("version")
    private Long version;

    @Column("created_by")
    private UUID createdBy;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
