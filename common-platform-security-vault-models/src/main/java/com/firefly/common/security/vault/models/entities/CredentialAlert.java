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
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a credential alert
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credential_alerts")
public class CredentialAlert {

    @Id
    private UUID id;

    @Column("credential_id")
    private UUID credentialId;

    @Column("alert_type")
    private String alertType;

    @Column("severity")
    private String severity;

    @Column("alert_message")
    private String alertMessage;

    @Column("alert_details")
    private String alertDetails;

    @Column("triggered_at")
    private LocalDateTime triggeredAt;

    @Column("acknowledged")
    private Boolean acknowledged;

    @Column("acknowledged_by")
    private UUID acknowledgedBy;

    @Column("acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column("resolved")
    private Boolean resolved;

    @Column("resolved_by")
    private UUID resolvedBy;

    @Column("resolved_at")
    private LocalDateTime resolvedAt;

    @Column("resolution_notes")
    private String resolutionNotes;

    @Column("notification_sent")
    private Boolean notificationSent;

    @Column("notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column("metadata")
    private String metadata;
}
