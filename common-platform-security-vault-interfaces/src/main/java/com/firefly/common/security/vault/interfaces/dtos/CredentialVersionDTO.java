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

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for CredentialVersion entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credential Version information for version history")
public class CredentialVersionDTO {
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Version ID")
    private UUID id;
    
    @NotNull(message = "Credential ID is required")
    @Schema(description = "Credential ID this version belongs to")
    private UUID credentialId;
    
    @Schema(description = "Version number", example = "1")
    private Integer versionNumber;
    
    @Schema(description = "Encrypted value", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String encryptedValue;
    
    @Schema(description = "Encryption algorithm", example = "AES-256-GCM")
    private String encryptionAlgorithm;
    
    @Schema(description = "Encryption key ID")
    private String encryptionKeyId;
    
    @Schema(description = "Encryption IV")
    private String encryptionIv;
    
    @Schema(description = "Valid from timestamp")
    private LocalDateTime validFrom;
    
    @Schema(description = "Valid until timestamp")
    private LocalDateTime validUntil;
    
    @Schema(description = "Whether this is the current version")
    private Boolean isCurrent;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Created by user ID")
    private UUID createdBy;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Reason for rotation", example = "Scheduled rotation")
    private String rotationReason;
    
    @Schema(description = "Additional metadata")
    private String metadata;
}

