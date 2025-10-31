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
 * Entity representing a secure credential in the vault.
 *
 * <p>This entity manages encrypted storage of credentials for providers,
 * integrations, and external services in the Firefly core banking system,
 * providing enterprise-grade security with encryption, rotation, and audit trails.</p>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Encrypted storage using AES-256-GCM or similar algorithms</li>
 *   <li>Automatic credential rotation and expiration management</li>
 *   <li>Comprehensive audit trail for all access</li>
 *   <li>Multi-tenant and multi-environment support</li>
 *   <li>Access control and sharing mechanisms</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("credentials")
public class Credential {

    /**
     * Unique identifier for the credential (Primary Key)
     */
    @Id
    private UUID id;

    /**
     * Unique code for the credential
     */
    @Column("code")
    private String code;

    /**
     * Human-readable name for the credential
     */
    @Column("name")
    private String name;

    /**
     * Description of what this credential is used for
     */
    @Column("description")
    private String description;

    /**
     * Reference to the credential type (Foreign Key to credential_types)
     */
    @Column("credential_type_id")
    private UUID credentialTypeId;

    /**
     * Reference to the credential status (Foreign Key to credential_statuses)
     */
    @Column("credential_status_id")
    private UUID credentialStatusId;

    /**
     * Reference to the environment type (Foreign Key to environment_types)
     */
    @Column("environment_type_id")
    private UUID environmentTypeId;

    // ========================================
    // External Entity References
    // ========================================

    /**
     * Reference to the provider using this credential
     */
    @Column("provider_id")
    private UUID providerId;

    /**
     * Reference to the integration using this credential
     */
    @Column("integration_id")
    private UUID integrationId;

    /**
     * Reference to the service using this credential
     */
    @Column("service_id")
    private UUID serviceId;

    /**
     * Reference to the tenant owning this credential
     */
    @Column("tenant_id")
    private UUID tenantId;

    // ========================================
    // Encrypted Data
    // ========================================

    /**
     * Encrypted credential value (stored as encrypted JSON)
     */
    @Column("encrypted_value")
    private String encryptedValue;

    /**
     * Encryption algorithm used (e.g., AES-256-GCM)
     */
    @Column("encryption_algorithm")
    private String encryptionAlgorithm;

    /**
     * ID of the encryption key used
     */
    @Column("encryption_key_id")
    private String encryptionKeyId;

    /**
     * Initialization vector for encryption
     */
    @Column("encryption_iv")
    private String encryptionIv;

    // ========================================
    // Credential Metadata
    // ========================================

    /**
     * Owner of the credential
     */
    @Column("credential_owner")
    private String credentialOwner;

    /**
     * Contact email for credential-related matters
     */
    @Column("credential_contact_email")
    private String credentialContactEmail;

    // ========================================
    // Rotation and Expiration
    // ========================================

    /**
     * Timestamp when the credential expires
     */
    @Column("expires_at")
    private LocalDateTime expiresAt;

    /**
     * Number of days before expiration to trigger rotation
     */
    @Column("rotate_before_days")
    private Integer rotateBeforeDays;

    /**
     * Timestamp of last rotation
     */
    @Column("last_rotated_at")
    private LocalDateTime lastRotatedAt;

    /**
     * Indicates if automatic rotation is enabled
     */
    @Column("rotation_enabled")
    private Boolean rotationEnabled;

    /**
     * Automatic rotation interval in days
     */
    @Column("auto_rotation_days")
    private Integer autoRotationDays;

    // ========================================
    // Usage Tracking
    // ========================================

    /**
     * Timestamp of last credential use
     */
    @Column("last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Total number of times the credential has been accessed
     */
    @Column("usage_count")
    private Long usageCount;

    /**
     * Identity of last accessor
     */
    @Column("last_accessed_by")
    private String lastAccessedBy;

    // ========================================
    // Access Control
    // ========================================

    /**
     * Access scope: INTERNAL, EXTERNAL, SHARED
     */
    @Column("access_scope")
    private String accessScope;

    /**
     * Comma-separated list of allowed services
     */
    @Column("allowed_services")
    private String allowedServices;

    /**
     * Comma-separated list of allowed IP addresses
     */
    @Column("allowed_ips")
    private String allowedIps;

    /**
     * Comma-separated list of allowed environments
     */
    @Column("allowed_environments")
    private String allowedEnvironments;

    // ========================================
    // Security Flags
    // ========================================

    /**
     * Indicates if this is a sensitive credential
     */
    @Column("is_sensitive")
    private Boolean isSensitive;

    /**
     * Indicates if approval is required for access
     */
    @Column("require_approval_for_access")
    private Boolean requireApprovalForAccess;

    /**
     * Indicates if all access should be audited
     */
    @Column("audit_all_access")
    private Boolean auditAllAccess;

    /**
     * Indicates if credential should be masked in logs
     */
    @Column("mask_in_logs")
    private Boolean maskInLogs;

    // ========================================
    // Backup and Recovery
    // ========================================

    /**
     * Indicates if backup is enabled
     */
    @Column("backup_enabled")
    private Boolean backupEnabled;

    /**
     * Location where backup is stored
     */
    @Column("backup_location")
    private String backupLocation;

    // ========================================
    // Organization
    // ========================================

    /**
     * Comma-separated tags for organization
     */
    @Column("tags")
    private String tags;

    /**
     * Additional metadata in JSON format
     */
    @Column("metadata")
    private String metadata;

    // ========================================
    // System Fields
    // ========================================

    /**
     * Indicates if this credential is active
     */
    @Column("active")
    private Boolean active;

    /**
     * Version number for optimistic locking
     */
    @Version
    @Column("version")
    private Long version;

    /**
     * Timestamp when credential was created
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when credential was last updated
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * User who created this credential
     */
    @Column("created_by")
    private UUID createdBy;

    /**
     * User who last updated this credential
     */
    @Column("updated_by")
    private UUID updatedBy;
}

