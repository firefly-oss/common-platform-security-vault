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
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for EncryptionKey entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Encryption Key metadata information")
public class EncryptionKeyDTO {
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Encryption key ID")
    private UUID id;
    
    @NotBlank(message = "Key ID is required")
    @Size(min = 2, max = 100, message = "Key ID must be between 2 and 100 characters")
    @Schema(description = "Unique key identifier", example = "master-key-prod-2025")
    private String keyId;
    
    @NotBlank(message = "Key name is required")
    @Size(min = 2, max = 255, message = "Key name must be between 2 and 255 characters")
    @Schema(description = "Name of the encryption key", example = "Production Master Key 2025")
    private String keyName;
    
    @Schema(description = "Type of key", example = "SYMMETRIC")
    private String keyType;
    
    @Schema(description = "Encryption algorithm", example = "AES-256-GCM")
    private String keyAlgorithm;
    
    @Schema(description = "KMS provider", example = "AWS_KMS")
    private String keyProvider;
    
    @Schema(description = "Key location/ARN", example = "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012")
    private String keyLocation;
    
    @Schema(description = "Key status", example = "ACTIVE")
    private String keyStatus;
    
    @Schema(description = "Whether this is a master key", defaultValue = "false")
    private Boolean isMasterKey;
    
    @Schema(description = "Purpose of the key", example = "CREDENTIAL_ENCRYPTION")
    private String keyPurpose;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Expiration timestamp")
    private LocalDateTime expiresAt;
    
    @Schema(description = "Last rotation timestamp")
    private LocalDateTime rotatedAt;
    
    @Schema(description = "Rotation schedule in days")
    private Integer rotationScheduleDays;
    
    @Schema(description = "Additional metadata")
    private String metadata;
    
    @Schema(description = "Whether the key is active", defaultValue = "true")
    private Boolean active;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Version for optimistic locking")
    private Long version;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Created by user ID")
    private UUID createdBy;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}

