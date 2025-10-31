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
 * Entity representing credential sharing/access grants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credential_shares")
public class CredentialShare {

    @Id
    private UUID id;

    @Column("credential_id")
    private UUID credentialId;

    @Column("shared_with_tenant_id")
    private UUID sharedWithTenantId;

    @Column("shared_with_service")
    private String sharedWithService;

    @Column("shared_with_user_id")
    private UUID sharedWithUserId;

    @Column("share_type")
    private String shareType;

    @Column("access_level")
    private String accessLevel;

    @Column("valid_from")
    private LocalDateTime validFrom;

    @Column("valid_until")
    private LocalDateTime validUntil;

    @Column("max_access_count")
    private Integer maxAccessCount;

    @Column("current_access_count")
    private Integer currentAccessCount;

    @Column("allowed_operations")
    private String allowedOperations;

    @Column("shared_by")
    private UUID sharedBy;

    @Column("approval_required")
    private Boolean approvalRequired;

    @Column("approval_status")
    private String approvalStatus;

    @Column("approved_by")
    private UUID approvedBy;

    @Column("approved_at")
    private LocalDateTime approvedAt;

    @Column("share_reason")
    private String shareReason;

    @Column("metadata")
    private String metadata;

    @Column("active")
    private Boolean active;

    @Version
    @Column("version")
    private Long version;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
