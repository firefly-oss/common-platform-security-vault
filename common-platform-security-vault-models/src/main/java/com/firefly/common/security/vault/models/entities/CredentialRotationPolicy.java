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
 * Entity representing a credential rotation policy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credential_rotation_policies")
public class CredentialRotationPolicy {

    @Id
    private UUID id;

    @Column("policy_name")
    private String policyName;

    @Column("description")
    private String description;

    @Column("credential_type_id")
    private UUID credentialTypeId;

    @Column("environment_type_id")
    private UUID environmentTypeId;

    @Column("rotation_interval_days")
    private Integer rotationIntervalDays;

    @Column("rotation_warning_days")
    private Integer rotationWarningDays;

    @Column("auto_rotation_enabled")
    private Boolean autoRotationEnabled;

    @Column("require_manual_approval")
    private Boolean requireManualApproval;

    @Column("notify_before_days")
    private Integer notifyBeforeDays;

    @Column("notification_recipients")
    private String notificationRecipients;

    @Column("policy_enforcement")
    private String policyEnforcement;

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

    @Column("created_by")
    private UUID createdBy;
}
