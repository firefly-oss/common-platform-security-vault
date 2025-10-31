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

package com.firefly.common.security.vault.core.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates Security Vault configuration at startup
 * 
 * Performs comprehensive validation of:
 * - Provider configuration
 * - Provider-specific settings
 * - Security settings
 * - Performance settings
 * 
 * Fails fast if critical configuration is missing or invalid
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityVaultConfigurationValidator {

    private final SecurityVaultProperties properties;

    @PostConstruct
    public void validate() {
        log.info("🔍 Validating Security Vault configuration...");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate basic configuration
        validateBasicConfiguration(errors, warnings);
        
        // Validate provider-specific configuration
        String provider = properties.getEncryption().getProvider();
        switch (provider.toUpperCase()) {
            case "IN_MEMORY" -> validateInMemoryConfiguration(warnings);
            case "AWS_KMS" -> validateAwsKmsConfiguration(errors, warnings);
            case "AZURE_KEY_VAULT" -> validateAzureKeyVaultConfiguration(errors, warnings);
            case "HASHICORP_VAULT" -> validateHashiCorpVaultConfiguration(errors, warnings);
            case "GOOGLE_CLOUD_KMS" -> validateGoogleCloudKmsConfiguration(errors, warnings);
            default -> errors.add("Unknown provider: " + provider);
        }

        // Validate security settings
        validateSecuritySettings(warnings);

        // Report results
        reportValidationResults(errors, warnings);
    }

    private void validateBasicConfiguration(List<String> errors, List<String> warnings) {
        // Master Key ID
        if (properties.getEncryption().getMasterKeyId() == null || 
            properties.getEncryption().getMasterKeyId().isEmpty()) {
            errors.add("Master Key ID is required (firefly.security.vault.encryption.master-key-id)");
        }

        // Provider
        if (properties.getEncryption().getProvider() == null || 
            properties.getEncryption().getProvider().isEmpty()) {
            errors.add("Provider is required (firefly.security.vault.encryption.provider)");
        }

        // Envelope encryption
        if (properties.getEncryption().isEnvelopeEncryption()) {
            log.info("Envelope encryption is ENABLED");
        } else {
            warnings.add("Envelope encryption is DISABLED - Consider enabling for better security");
        }
    }

    private void validateInMemoryConfiguration(List<String> warnings) {
        warnings.add("WARNING: IN_MEMORY provider is active - NOT SUITABLE FOR PRODUCTION");
        warnings.add("WARNING: Keys are stored in memory and will be lost on restart");
        warnings.add("WARNING: Use AWS_KMS, AZURE_KEY_VAULT, or HASHICORP_VAULT for production");
    }

    private void validateAwsKmsConfiguration(List<String> errors, List<String> warnings) {
        var config = properties.getEncryption().getAwsKms();
        
        if (config.getRegion() == null || config.getRegion().isEmpty()) {
            errors.add("AWS KMS region is required (firefly.security.vault.encryption.aws-kms.region)");
        }
        
        if (config.getKeyArn() == null || config.getKeyArn().isEmpty()) {
            errors.add("AWS KMS key ARN is required (firefly.security.vault.encryption.aws-kms.key-arn)");
        }
        
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            warnings.add("Custom AWS KMS endpoint configured: " + config.getEndpoint());
            warnings.add("This should only be used for testing (e.g., LocalStack)");
        }

        log.info("AWS KMS provider configured - Region: {}", config.getRegion());
    }

    private void validateAzureKeyVaultConfiguration(List<String> errors, List<String> warnings) {
        var config = properties.getEncryption().getAzureKeyVault();
        
        if (config.getVaultUrl() == null || config.getVaultUrl().isEmpty()) {
            errors.add("Azure Key Vault URL is required (firefly.security.vault.encryption.azure-key-vault.vault-url)");
        }
        
        if (config.getKeyName() == null || config.getKeyName().isEmpty()) {
            errors.add("Azure Key Vault key name is required (firefly.security.vault.encryption.azure-key-vault.key-name)");
        }
        
        if (config.getTenantId() == null || config.getTenantId().isEmpty()) {
            errors.add("Azure tenant ID is required (firefly.security.vault.encryption.azure-key-vault.tenant-id)");
        }
        
        if (config.getClientId() == null || config.getClientId().isEmpty()) {
            errors.add("Azure client ID is required (firefly.security.vault.encryption.azure-key-vault.client-id)");
        }
        
        if (config.getClientSecret() == null || config.getClientSecret().isEmpty()) {
            errors.add("Azure client secret is required (firefly.security.vault.encryption.azure-key-vault.client-secret)");
        }

        log.info("Azure Key Vault provider configured - Vault: {}", maskUrl(config.getVaultUrl()));
    }

    private void validateHashiCorpVaultConfiguration(List<String> errors, List<String> warnings) {
        var config = properties.getEncryption().getHashicorpVault();
        
        if (config.getAddress() == null || config.getAddress().isEmpty()) {
            errors.add("HashiCorp Vault address is required (firefly.security.vault.encryption.hashicorp-vault.address)");
        }
        
        if (config.getToken() == null || config.getToken().isEmpty()) {
            errors.add("HashiCorp Vault token is required (firefly.security.vault.encryption.hashicorp-vault.token)");
        }
        
        if (config.getKeyName() == null || config.getKeyName().isEmpty()) {
            errors.add("HashiCorp Vault key name is required (firefly.security.vault.encryption.hashicorp-vault.key-name)");
        }
        
        if (config.getTransitPath() == null || config.getTransitPath().isEmpty()) {
            warnings.add("Transit path not configured, using default: transit");
        }

        if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
            log.info("HashiCorp Vault namespace configured: {}", config.getNamespace());
        }

        log.info("HashiCorp Vault provider configured - Address: {}", maskUrl(config.getAddress()));
    }

    private void validateGoogleCloudKmsConfiguration(List<String> errors, List<String> warnings) {
        var config = properties.getEncryption().getGoogleCloudKms();
        
        if (config.getProjectId() == null || config.getProjectId().isEmpty()) {
            errors.add("Google Cloud project ID is required (firefly.security.vault.encryption.google-cloud-kms.project-id)");
        }
        
        if (config.getKeyRingId() == null || config.getKeyRingId().isEmpty()) {
            errors.add("Google Cloud key ring ID is required (firefly.security.vault.encryption.google-cloud-kms.key-ring-id)");
        }
        
        if (config.getKeyId() == null || config.getKeyId().isEmpty()) {
            errors.add("Google Cloud key ID is required (firefly.security.vault.encryption.google-cloud-kms.key-id)");
        }
        
        if (config.getCredentialsPath() == null || config.getCredentialsPath().isEmpty()) {
            warnings.add("Google Cloud credentials path not configured - will use default credentials");
        }

        log.info("Google Cloud KMS provider configured - Project: {}", config.getProjectId());
    }

    private void validateSecuritySettings(List<String> warnings) {
        // Audit logging
        if (!properties.getAudit().isEnabled()) {
            warnings.add("Audit logging is DISABLED - Consider enabling for production");
        }
    }

    private void reportValidationResults(List<String> errors, List<String> warnings) {
        // Report warnings
        if (!warnings.isEmpty()) {
            log.warn("Configuration warnings:");
            warnings.forEach(warning -> log.warn("  - {}", warning));
        }

        // Report errors and fail if any
        if (!errors.isEmpty()) {
            log.error("Configuration validation FAILED:");
            errors.forEach(error -> log.error("  - {}", error));
            throw new IllegalStateException(
                "Security Vault configuration is invalid. Found " + errors.size() + " error(s). " +
                "Please check the logs and fix the configuration."
            );
        }

        // Success
        log.info("Security Vault configuration validation PASSED");
        log.info("Configuration summary:");
        log.info("  - Provider: {}", properties.getEncryption().getProvider());
        log.info("  - Master Key ID: {}", maskKeyId(properties.getEncryption().getMasterKeyId()));
        log.info("  - Envelope Encryption: {}", properties.getEncryption().isEnvelopeEncryption());
        log.info("  - Audit Logging: {}", properties.getAudit().isEnabled());
    }

    private String maskUrl(String url) {
        if (url == null) {
            return "***";
        }
        try {
            String domain = url.replaceAll("https?://", "").split("/")[0];
            return "https://" + domain.substring(0, Math.min(10, domain.length())) + "***";
        } catch (Exception e) {
            return "***";
        }
    }

    private String maskKeyId(String keyId) {
        if (keyId == null || keyId.length() <= 8) {
            return "***";
        }
        return keyId.substring(0, 4) + "***" + keyId.substring(keyId.length() - 4);
    }
}

