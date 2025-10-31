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


package com.firefly.common.security.vault.core.adapters;

import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.model.*;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AWS KMS adapter for Key Management
 * 
 * This adapter integrates with AWS Key Management Service (KMS) for
 * enterprise-grade key management in AWS environments.
 * 
 * Features:
 * - Integration with AWS KMS
 * - Envelope encryption support
 * - Automatic key rotation
 * - CloudTrail audit logging
 * - Multi-region support
 * 
 * Prerequisites:
 * - AWS SDK KMS dependency in classpath
 * - AWS credentials configured (IAM role, environment variables, or AWS CLI)
 * - KMS key ARN configured
 * 
 * @see <a href="https://docs.aws.amazon.com/kms/latest/developerguide/">AWS KMS Documentation</a>
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "firefly.security.vault.encryption",
    name = "provider",
    havingValue = "AWS_KMS"
)
@ConditionalOnClass(name = "software.amazon.awssdk.services.kms.KmsAsyncClient")
public class AwsKmsKeyManagementAdapter implements KeyManagementPort {

    private final SecurityVaultProperties properties;
    private final KmsAsyncClient kmsClient;

    public AwsKmsKeyManagementAdapter(SecurityVaultProperties properties) {
        this.properties = properties;
        this.kmsClient = initializeKmsClient();
        log.info("AWS KMS Key Management Adapter initialized - Region: {}",
            properties.getEncryption().getAwsKms().getRegion());
    }

    @PreDestroy
    public void destroy() {
        if (kmsClient != null) {
            log.info("Closing AWS KMS client...");
            kmsClient.close();
        }
    }

    @Override
    public Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String context) {
        log.debug("Encrypting data with AWS KMS key: {}", maskKeyArn(keyId));

        Map<String, String> encryptionContext = context != null ?
            Map.of("context", context) : Map.of();

        EncryptRequest request = EncryptRequest.builder()
            .keyId(getEffectiveKeyId(keyId))
            .plaintext(SdkBytes.fromByteArray(plaintext))
            .encryptionContext(encryptionContext)
            .build();

        return Mono.fromFuture(kmsClient.encrypt(request))
            .map(response -> {
                log.debug("Successfully encrypted data with AWS KMS");
                return new EncryptionResult(
                    response.ciphertextBlob().asByteArray(),
                    response.keyId(),
                    "AWS_KMS_AES_256",
                    buildMetadata(context, response.keyId())
                );
            })
            .doOnError(error -> log.error("AWS KMS encryption failed: {}", error.getMessage()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String context) {
        log.debug("Decrypting data with AWS KMS");

        Map<String, String> encryptionContext = context != null ?
            Map.of("context", context) : Map.of();

        DecryptRequest.Builder requestBuilder = DecryptRequest.builder()
            .ciphertextBlob(SdkBytes.fromByteArray(ciphertext))
            .encryptionContext(encryptionContext);

        // KeyId is optional for decrypt (KMS can determine it from ciphertext)
        if (keyId != null && !keyId.isEmpty()) {
            requestBuilder.keyId(getEffectiveKeyId(keyId));
        }

        return Mono.fromFuture(kmsClient.decrypt(requestBuilder.build()))
            .map(response -> {
                log.debug("Successfully decrypted data with AWS KMS");
                return response.plaintext().asByteArray();
            })
            .doOnError(error -> log.error("AWS KMS decryption failed: {}", error.getMessage()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DataKey> generateDataKey(String keyId, String keySpec) {
        log.debug("Generating data key with AWS KMS master key: {}", maskKeyArn(keyId));

        // Map keySpec to AWS DataKeySpec (default to AES_256)
        DataKeySpec awsKeySpec = DataKeySpec.AES_256;
        if ("AES_128".equalsIgnoreCase(keySpec)) {
            awsKeySpec = DataKeySpec.AES_128;
        }

        GenerateDataKeyRequest request = GenerateDataKeyRequest.builder()
            .keyId(getEffectiveKeyId(keyId))
            .keySpec(awsKeySpec)
            .build();

        return Mono.fromFuture(kmsClient.generateDataKey(request))
            .map(response -> {
                log.debug("Successfully generated data key with AWS KMS");
                return new DataKey(
                    response.plaintext().asByteArray(),
                    response.ciphertextBlob().asByteArray(),
                    response.keyId()
                );
            })
            .doOnError(error -> log.error("AWS KMS data key generation failed: {}", error.getMessage()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<KeyRotationResult> rotateKey(String keyId) {
        log.info("Enabling automatic key rotation for AWS KMS key: {}", maskKeyArn(keyId));

        EnableKeyRotationRequest request = EnableKeyRotationRequest.builder()
            .keyId(getEffectiveKeyId(keyId))
            .build();

        return Mono.fromFuture(kmsClient.enableKeyRotation(request))
            .map(response -> {
                log.info("Successfully enabled automatic key rotation for AWS KMS key");
                return new KeyRotationResult(
                    true,
                    "automatic-rotation-enabled",
                    "AWS KMS automatic key rotation enabled (365 days)"
                );
            })
            .onErrorResume(e -> {
                log.error("Failed to enable key rotation for {}: {}", maskKeyArn(keyId), e.getMessage());
                return Mono.just(new KeyRotationResult(
                    false,
                    null,
                    "Key rotation failed: " + e.getMessage()
                ));
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> validateKey(String keyId) {
        log.debug("Validating AWS KMS key: {}", maskKeyArn(keyId));

        DescribeKeyRequest request = DescribeKeyRequest.builder()
            .keyId(getEffectiveKeyId(keyId))
            .build();

        return Mono.fromFuture(kmsClient.describeKey(request))
            .map(response -> {
                KeyMetadata metadata = response.keyMetadata();
                boolean isValid = metadata.enabled() &&
                                 metadata.keyState() == KeyState.ENABLED;

                log.debug("Key {} validation result: {} (state: {})",
                    maskKeyArn(keyId), isValid, metadata.keyState());
                return isValid;
            })
            .onErrorResume(e -> {
                log.error("Key validation failed for {}: {}", maskKeyArn(keyId), e.getMessage());
                return Mono.just(false);
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.AWS_KMS;
    }

    /**
     * Initialize AWS KMS client
     *
     * Creates KmsAsyncClient with:
     * - Configured region
     * - Custom endpoint if specified (for LocalStack, etc.)
     * - Async Netty HTTP client for non-blocking operations
     */
    private KmsAsyncClient initializeKmsClient() {
        log.info("Initializing AWS KMS client...");

        SecurityVaultProperties.AwsKmsConfig config = properties.getEncryption().getAwsKms();

        // Validate configuration
        if (config.getRegion() == null || config.getRegion().isEmpty()) {
            throw new IllegalStateException(
                "AWS KMS region is required. Set firefly.security.vault.encryption.aws-kms.region"
            );
        }

        if (config.getKeyArn() == null || config.getKeyArn().isEmpty()) {
            throw new IllegalStateException(
                "AWS KMS key ARN is required. Set firefly.security.vault.encryption.aws-kms.key-arn"
            );
        }

        log.info("AWS KMS Configuration:");
        log.info("  Region: {}", config.getRegion());
        log.info("  Key ARN: {}", maskKeyArn(config.getKeyArn()));
        log.info("  Custom Endpoint: {}", config.getEndpoint() != null ? config.getEndpoint() : "default");

        // Build KMS client
        var builder = KmsAsyncClient.builder()
            .region(Region.of(config.getRegion()));

        // Custom endpoint (for LocalStack or testing)
        if (config.getEndpoint() != null && !config.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(config.getEndpoint()));
        }

        KmsAsyncClient client = builder.build();

        log.info("AWS KMS client initialized successfully");

        return client;
    }

    /**
     * Get effective key ID (use configured key ARN if keyId is null)
     */
    private String getEffectiveKeyId(String keyId) {
        if (keyId != null && !keyId.isEmpty()) {
            return keyId;
        }
        return properties.getEncryption().getAwsKms().getKeyArn();
    }

    /**
     * Mask key ARN for logging (show only last 8 characters)
     */
    private String maskKeyArn(String keyArn) {
        if (keyArn == null || keyArn.length() <= 8) {
            return "***";
        }
        return "***" + keyArn.substring(keyArn.length() - 8);
    }

    /**
     * Build metadata string from encryption context and key ID
     */
    private String buildMetadata(String context, String responseKeyId) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("provider", "AWS_KMS");
        metadata.put("region", properties.getEncryption().getAwsKms().getRegion());
        metadata.put("keyId", maskKeyArn(responseKeyId));
        if (context != null) {
            metadata.put("context", context);
        }
        return metadata.toString();
    }
}

