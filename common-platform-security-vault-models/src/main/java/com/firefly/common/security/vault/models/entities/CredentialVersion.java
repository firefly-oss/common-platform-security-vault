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
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a credential version (for version history and rollback)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credential_versions")
public class CredentialVersion {

    @Id
    private UUID id;

    @Column("credential_id")
    private UUID credentialId;

    @Column("version_number")
    private Integer versionNumber;

    @Column("encrypted_value")
    private String encryptedValue;

    @Column("encryption_algorithm")
    private String encryptionAlgorithm;

    @Column("encryption_key_id")
    private String encryptionKeyId;

    @Column("encryption_iv")
    private String encryptionIv;

    @Column("valid_from")
    private LocalDateTime validFrom;

    @Column("valid_until")
    private LocalDateTime validUntil;

    @Column("is_current")
    private Boolean isCurrent;

    @Column("created_by")
    private UUID createdBy;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("rotation_reason")
    private String rotationReason;

    @Column("metadata")
    private String metadata;
}
