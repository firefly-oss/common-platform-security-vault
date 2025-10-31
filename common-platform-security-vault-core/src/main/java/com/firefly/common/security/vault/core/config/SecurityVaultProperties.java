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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Security Vault
 */
@Data
@Component
@ConfigurationProperties(prefix = "firefly.security.vault")
public class SecurityVaultProperties {

    /**
     * Encryption configuration
     */
    private EncryptionConfig encryption = new EncryptionConfig();

    /**
     * Audit configuration
     */
    private AuditConfig audit = new AuditConfig();

    /**
     * Rotation configuration
     */
    private RotationConfig rotation = new RotationConfig();

    /**
     * Access control configuration
     */
    private AccessControlConfig accessControl = new AccessControlConfig();

    @Data
    public static class EncryptionConfig {
        /**
         * Default encryption algorithm
         */
        private String algorithm = "AES-256-GCM";

        /**
         * Hexagonal Architecture: Provider selection
         * Options: IN_MEMORY, AWS_KMS, AZURE_KEY_VAULT, HASHICORP_VAULT, GOOGLE_CLOUD_KMS
         */
        private String provider = "IN_MEMORY";

        /**
         * Legacy: Default KMS provider (deprecated, use 'provider' instead)
         */
        @Deprecated
        private String kmsProvider = "IN_MEMORY";

        /**
         * Master encryption key ID
         */
        private String masterKeyId = "default-master-key";

        /**
         * Enable envelope encryption (encrypt data keys with master key)
         */
        private boolean envelopeEncryption = false;

        /**
         * AWS KMS specific configuration
         */
        private AwsKmsConfig awsKms = new AwsKmsConfig();

        /**
         * Azure Key Vault configuration
         */
        private AzureKeyVaultConfig azureKeyVault = new AzureKeyVaultConfig();

        /**
         * HashiCorp Vault configuration
         */
        private HashiCorpVaultConfig hashicorpVault = new HashiCorpVaultConfig();

        /**
         * Google Cloud KMS configuration
         */
        private GoogleCloudKmsConfig googleCloudKms = new GoogleCloudKmsConfig();
    }

    @Data
    public static class AwsKmsConfig {
        private String region = "us-east-1";
        private String keyArn;
        private String endpoint; // For LocalStack or custom endpoints
    }

    @Data
    public static class AzureKeyVaultConfig {
        private String vaultUrl;
        private String keyName;
        private String tenantId;
        private String clientId;
        private String clientSecret;
    }

    @Data
    public static class HashiCorpVaultConfig {
        private String address;
        private String token;
        private String transitPath = "transit";
        private String keyName;
        private String namespace; // For Vault Enterprise
        private boolean tlsEnabled = true;
        private String tlsCertPath;
    }

    @Data
    public static class GoogleCloudKmsConfig {
        private String projectId;
        private String locationId = "global";
        private String keyRingId;
        private String keyId;
        private String credentialsPath; // Path to service account JSON
    }

    @Data
    public static class AuditConfig {
        /**
         * Enable comprehensive audit logging
         */
        private boolean enabled = true;

        /**
         * Log all access attempts (including denied)
         */
        private boolean logAllAttempts = true;

        /**
         * Log decryption operations
         */
        private boolean logDecryptions = true;

        /**
         * Include performance metrics in audit logs
         */
        private boolean includePerformanceMetrics = true;

        /**
         * Retention period for audit logs (days)
         */
        private int retentionDays = 90;
    }

    @Data
    public static class RotationConfig {
        /**
         * Enable automatic rotation
         */
        private boolean autoRotationEnabled = false;

        /**
         * Default rotation interval (days)
         */
        private int defaultRotationDays = 90;

        /**
         * Warning period before expiration (days)
         */
        private int warningBeforeDays = 7;

        /**
         * Enable automatic rotation for expired credentials
         */
        private boolean rotateExpired = true;

        /**
         * Maximum versions to keep per credential
         */
        private int maxVersionsToKeep = 10;
    }

    @Data
    public static class AccessControlConfig {
        /**
         * Enable strict access control
         */
        private boolean strictMode = true;

        /**
         * Require approval for sensitive credentials
         */
        private boolean requireApprovalForSensitive = true;

        /**
         * Enable IP whitelisting
         */
        private boolean enableIpWhitelist = true;

        /**
         * Enable service whitelisting
         */
        private boolean enableServiceWhitelist = true;

        /**
         * Maximum failed access attempts before lockout
         */
        private int maxFailedAttempts = 5;

        /**
         * Lockout duration (minutes)
         */
        private int lockoutDurationMinutes = 30;

        /**
         * Enable rate limiting
         */
        private boolean enableRateLimiting = true;

        /**
         * Rate limit: requests per minute
         */
        private int rateLimitPerMinute = 100;
    }
}
