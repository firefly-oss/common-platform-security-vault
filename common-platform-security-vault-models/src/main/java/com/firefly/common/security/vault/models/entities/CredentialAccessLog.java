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
 * Entity representing an audit log entry for credential access
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credential_access_logs")
public class CredentialAccessLog {

    @Id
    private UUID id;

    @Column("credential_id")
    private UUID credentialId;

    @Column("access_type")
    private String accessType;

    @Column("accessed_by")
    private String accessedBy;

    @Column("accessed_by_user_id")
    private UUID accessedByUserId;

    @Column("accessed_by_service")
    private String accessedByService;

    @Column("access_ip")
    private String accessIp;

    @Column("access_location")
    private String accessLocation;

    @Column("access_result")
    private String accessResult;

    @Column("access_reason")
    private String accessReason;

    @Column("credential_version_id")
    private UUID credentialVersionId;

    @Column("decryption_successful")
    private Boolean decryptionSuccessful;

    @Column("error_message")
    private String errorMessage;

    @Column("access_duration_ms")
    private Integer accessDurationMs;

    @Column("metadata")
    private String metadata;

    @Column("accessed_at")
    private LocalDateTime accessedAt;
}
