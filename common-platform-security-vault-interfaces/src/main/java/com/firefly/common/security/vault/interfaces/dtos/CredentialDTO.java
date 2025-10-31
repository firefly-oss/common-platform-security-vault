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


package com.firefly.common.security.vault.interfaces.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Credential entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credential information")
public class CredentialDTO {
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Credential ID")
    private UUID id;
    
    @NotBlank(message = "Code is required")
    @Size(min = 2, max = 100, message = "Code must be between 2 and 100 characters")
    @Schema(description = "Unique code for the credential", example = "STRIPE_API_KEY_PROD")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    @Schema(description = "Name of the credential", example = "Stripe Production API Key")
    private String name;
    
    @Schema(description = "Description of the credential", example = "API key for Stripe payment gateway in production")
    private String description;
    
    @NotNull(message = "Credential type ID is required")
    @Schema(description = "Credential type ID")
    private UUID credentialTypeId;
    
    @NotNull(message = "Credential status ID is required")
    @Schema(description = "Credential status ID")
    private UUID credentialStatusId;
    
    @NotNull(message = "Environment type ID is required")
    @Schema(description = "Environment type ID")
    private UUID environmentTypeId;
    
    @Schema(description = "Provider ID")
    private UUID providerId;
    
    @Schema(description = "Integration ID")
    private UUID integrationId;
    
    @Schema(description = "Service ID")
    private UUID serviceId;
    
    @Schema(description = "Tenant ID")
    private UUID tenantId;
    
    @NotBlank(message = "Encrypted value is required")
    @Schema(description = "Encrypted credential value", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String encryptedValue;
    
    @Schema(description = "Encryption algorithm", example = "AES-256-GCM")
    private String encryptionAlgorithm;
    
    @Schema(description = "Encryption key ID")
    private String encryptionKeyId;
    
    @Schema(description = "Encryption IV")
    private String encryptionIv;
    
    @Schema(description = "Credential owner")
    private String credentialOwner;
    
    @Schema(description = "Credential contact email")
    private String credentialContactEmail;
    
    @Schema(description = "Expiration timestamp")
    private LocalDateTime expiresAt;
    
    @Schema(description = "Rotate before days")
    private Integer rotateBeforeDays;
    
    @Schema(description = "Last rotated timestamp")
    private LocalDateTime lastRotatedAt;
    
    @Schema(description = "Rotation enabled")
    private Boolean rotationEnabled;
    
    @Schema(description = "Auto rotation days")
    private Integer autoRotationDays;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Last used timestamp")
    private LocalDateTime lastUsedAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Usage count")
    private Long usageCount;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Last accessed by")
    private String lastAccessedBy;
    
    @Schema(description = "Access scope", example = "INTERNAL")
    private String accessScope;
    
    @Schema(description = "Allowed services")
    private String allowedServices;
    
    @Schema(description = "Allowed IPs")
    private String allowedIps;
    
    @Schema(description = "Allowed environments")
    private String allowedEnvironments;
    
    @Schema(description = "Is sensitive")
    private Boolean isSensitive;
    
    @Schema(description = "Require approval for access")
    private Boolean requireApprovalForAccess;
    
    @Schema(description = "Audit all access")
    private Boolean auditAllAccess;
    
    @Schema(description = "Mask in logs")
    private Boolean maskInLogs;
    
    @Schema(description = "Backup enabled")
    private Boolean backupEnabled;
    
    @Schema(description = "Backup location")
    private String backupLocation;
    
    @Schema(description = "Tags")
    private String tags;
    
    @Schema(description = "Metadata")
    private String metadata;
    
    @Schema(description = "Whether the credential is active", defaultValue = "true")
    private Boolean active;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Version for optimistic locking")
    private Long version;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Created by user ID")
    private UUID createdBy;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Updated by user ID")
    private UUID updatedBy;
}
