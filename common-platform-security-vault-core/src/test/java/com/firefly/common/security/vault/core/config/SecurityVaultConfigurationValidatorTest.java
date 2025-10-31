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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SecurityVaultConfigurationValidator
 */
@ExtendWith(MockitoExtension.class)
class SecurityVaultConfigurationValidatorTest {

    @Mock(lenient = true)
    private SecurityVaultProperties properties;

    @Mock(lenient = true)
    private SecurityVaultProperties.EncryptionConfig encryptionConfig;

    @Mock(lenient = true)
    private SecurityVaultProperties.AuditConfig auditConfig;

    @Mock(lenient = true)
    private SecurityVaultProperties.AwsKmsConfig awsKmsConfig;

    @Mock(lenient = true)
    private SecurityVaultProperties.AzureKeyVaultConfig azureKeyVaultConfig;

    @Mock(lenient = true)
    private SecurityVaultProperties.HashiCorpVaultConfig hashiCorpVaultConfig;

    @Mock(lenient = true)
    private SecurityVaultProperties.GoogleCloudKmsConfig googleCloudKmsConfig;

    private SecurityVaultConfigurationValidator validator;

    @BeforeEach
    void setUp() {
        when(properties.getEncryption()).thenReturn(encryptionConfig);
        when(properties.getAudit()).thenReturn(auditConfig);
        when(encryptionConfig.getAwsKms()).thenReturn(awsKmsConfig);
        when(encryptionConfig.getAzureKeyVault()).thenReturn(azureKeyVaultConfig);
        when(encryptionConfig.getHashicorpVault()).thenReturn(hashiCorpVaultConfig);
        when(encryptionConfig.getGoogleCloudKms()).thenReturn(googleCloudKmsConfig);
    }

    @Test
    void shouldPassValidationForInMemoryProvider() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("IN_MEMORY");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    void shouldPassValidationForAwsKmsProvider() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("AWS_KMS");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(awsKmsConfig.getRegion()).thenReturn("us-east-1");
        when(awsKmsConfig.getKeyArn()).thenReturn("arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    void shouldPassValidationForAzureKeyVaultProvider() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("AZURE_KEY_VAULT");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(azureKeyVaultConfig.getVaultUrl()).thenReturn("https://my-vault.vault.azure.net");
        when(azureKeyVaultConfig.getKeyName()).thenReturn("my-key");
        when(azureKeyVaultConfig.getTenantId()).thenReturn("tenant-id");
        when(azureKeyVaultConfig.getClientId()).thenReturn("client-id");
        when(azureKeyVaultConfig.getClientSecret()).thenReturn("client-secret");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    void shouldPassValidationForHashiCorpVaultProvider() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("HASHICORP_VAULT");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(hashiCorpVaultConfig.getAddress()).thenReturn("https://vault.example.com");
        when(hashiCorpVaultConfig.getToken()).thenReturn("vault-token");
        when(hashiCorpVaultConfig.getKeyName()).thenReturn("my-key");
        when(hashiCorpVaultConfig.getTransitPath()).thenReturn("transit");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    void shouldPassValidationForGoogleCloudKmsProvider() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("GOOGLE_CLOUD_KMS");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(googleCloudKmsConfig.getProjectId()).thenReturn("my-project");
        when(googleCloudKmsConfig.getLocationId()).thenReturn("global");
        when(googleCloudKmsConfig.getKeyRingId()).thenReturn("my-keyring");
        when(googleCloudKmsConfig.getKeyId()).thenReturn("my-key");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertDoesNotThrow(() -> validator.validate());
    }

    @Test
    void shouldFailWhenMasterKeyIdIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("IN_MEMORY");
        when(encryptionConfig.getMasterKeyId()).thenReturn(null);
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenProviderIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenProviderIsUnknown() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("UNKNOWN_PROVIDER");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenAwsKmsRegionIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("AWS_KMS");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(awsKmsConfig.getRegion()).thenReturn(null);
        when(awsKmsConfig.getKeyArn()).thenReturn("arn:aws:kms:us-east-1:123456789012:key/12345678");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenAzureVaultUrlIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("AZURE_KEY_VAULT");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(azureKeyVaultConfig.getVaultUrl()).thenReturn(null);
        when(azureKeyVaultConfig.getKeyName()).thenReturn("my-key");
        when(azureKeyVaultConfig.getTenantId()).thenReturn("tenant-id");
        when(azureKeyVaultConfig.getClientId()).thenReturn("client-id");
        when(azureKeyVaultConfig.getClientSecret()).thenReturn("client-secret");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenHashiCorpVaultAddressIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("HASHICORP_VAULT");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(hashiCorpVaultConfig.getAddress()).thenReturn(null);
        when(hashiCorpVaultConfig.getToken()).thenReturn("vault-token");
        when(hashiCorpVaultConfig.getKeyName()).thenReturn("my-key");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }

    @Test
    void shouldFailWhenGoogleCloudProjectIdIsMissing() {
        // Given
        when(encryptionConfig.getProvider()).thenReturn("GOOGLE_CLOUD_KMS");
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-key");
        when(encryptionConfig.isEnvelopeEncryption()).thenReturn(true);
        when(auditConfig.isEnabled()).thenReturn(true);
        when(googleCloudKmsConfig.getProjectId()).thenReturn(null);
        when(googleCloudKmsConfig.getLocationId()).thenReturn("global");
        when(googleCloudKmsConfig.getKeyRingId()).thenReturn("my-keyring");
        when(googleCloudKmsConfig.getKeyId()).thenReturn("my-key");

        validator = new SecurityVaultConfigurationValidator(properties);

        // When & Then
        assertThatThrownBy(() -> validator.validate())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Security Vault configuration is invalid");
    }
}

