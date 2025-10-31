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
import com.google.cloud.kms.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Google Cloud KMS adapter for Key Management
 * 
 * This adapter integrates with Google Cloud Key Management Service (KMS) for
 * enterprise-grade key management in GCP environments.
 * 
 * Features:
 * - Integration with Google Cloud KMS
 * - Envelope encryption support
 * - Automatic key rotation
 * - Cloud Audit Logs integration
 * - Multi-region support
 * - Hardware Security Module (HSM) support
 * 
 * Configuration:
 * <pre>
 * firefly:
 *   security:
 *     vault:
 *       kms:
 *         provider: GOOGLE_CLOUD_KMS
 *         google-cloud:
 *           project-id: my-gcp-project
 *           location-id: global
 *           key-ring-id: my-keyring
 *           key-id: my-key
 *           credentials-path: /path/to/service-account.json
 * </pre>
 * 
 * @author Firefly Security Team
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "firefly.security.vault.kms",
    name = "provider",
    havingValue = "GOOGLE_CLOUD_KMS"
)
@ConditionalOnClass(name = "com.google.cloud.kms.v1.KeyManagementServiceClient")
public class GoogleCloudKmsKeyManagementAdapter implements KeyManagementPort {

    private final SecurityVaultProperties properties;
    private final KeyManagementServiceClient kmsClient;
    private final String keyName;
    private final Map<String, Integer> keyVersions = new HashMap<>();

    public GoogleCloudKmsKeyManagementAdapter(SecurityVaultProperties properties) throws IOException {
        this.properties = properties;
        
        // Initialize Google Cloud KMS client
        this.kmsClient = KeyManagementServiceClient.create();
        
        // Build the key name
        String projectId = properties.getEncryption().getGoogleCloudKms().getProjectId();
        String locationId = properties.getEncryption().getGoogleCloudKms().getLocationId();
        String keyRingId = properties.getEncryption().getGoogleCloudKms().getKeyRingId();
        String cryptoKeyId = properties.getEncryption().getGoogleCloudKms().getKeyId();
        
        this.keyName = CryptoKeyName.of(projectId, locationId, keyRingId, cryptoKeyId).toString();
        
        log.info("Google Cloud KMS Adapter initialized successfully");
        log.info("   Project: {}", projectId);
        log.info("   Location: {}", locationId);
        log.info("   Key Ring: {}", keyRingId);
        log.info("   Crypto Key: {}", cryptoKeyId);
    }

    @Override
    public Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String encryptionContext) {
        return Mono.fromCallable(() -> {
            try {
                String effectiveKeyName = buildKeyName(keyId);
                
                // Build encryption request
                EncryptRequest.Builder requestBuilder = EncryptRequest.newBuilder()
                    .setName(effectiveKeyName)
                    .setPlaintext(ByteString.copyFrom(plaintext));
                
                // Add additional authenticated data if provided
                if (encryptionContext != null && !encryptionContext.isEmpty()) {
                    requestBuilder.setAdditionalAuthenticatedData(
                        ByteString.copyFrom(encryptionContext, StandardCharsets.UTF_8)
                    );
                }
                
                EncryptRequest request = requestBuilder.build();
                EncryptResponse response = kmsClient.encrypt(request);
                
                byte[] ciphertext = response.getCiphertext().toByteArray();
                
                log.debug("Encrypted data using Google Cloud KMS key: {}", effectiveKeyName);
                
                return new EncryptionResult(
                    ciphertext,
                    keyId != null ? keyId : "default",
                    "GOOGLE_SYMMETRIC_ENCRYPTION",
                    buildMetadata(encryptionContext)
                );
            } catch (Exception e) {
                log.error("Encryption failed with Google Cloud KMS", e);
                throw new RuntimeException("Google Cloud KMS encryption failed", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String encryptionContext) {
        return Mono.fromCallable(() -> {
            try {
                String effectiveKeyName = buildKeyName(keyId);
                
                // Build decryption request
                DecryptRequest.Builder requestBuilder = DecryptRequest.newBuilder()
                    .setName(effectiveKeyName)
                    .setCiphertext(ByteString.copyFrom(ciphertext));
                
                // Add additional authenticated data if provided
                if (encryptionContext != null && !encryptionContext.isEmpty()) {
                    requestBuilder.setAdditionalAuthenticatedData(
                        ByteString.copyFrom(encryptionContext, StandardCharsets.UTF_8)
                    );
                }
                
                DecryptRequest request = requestBuilder.build();
                DecryptResponse response = kmsClient.decrypt(request);
                
                byte[] plaintext = response.getPlaintext().toByteArray();
                
                log.debug("Decrypted data using Google Cloud KMS key: {}", effectiveKeyName);
                
                return plaintext;
            } catch (Exception e) {
                log.error("Decryption failed with Google Cloud KMS for keyId: {}", keyId, e);
                throw new RuntimeException("Google Cloud KMS decryption failed", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DataKey> generateDataKey(String keyId, String encryptionContext) {
        return Mono.fromCallable(() -> {
            try {
                // Generate a random 256-bit data key
                byte[] plaintextKey = new byte[32]; // 256 bits
                new java.security.SecureRandom().nextBytes(plaintextKey);
                
                // Encrypt the data key using Google Cloud KMS
                String effectiveKeyName = buildKeyName(keyId);
                
                EncryptRequest.Builder requestBuilder = EncryptRequest.newBuilder()
                    .setName(effectiveKeyName)
                    .setPlaintext(ByteString.copyFrom(plaintextKey));
                
                if (encryptionContext != null && !encryptionContext.isEmpty()) {
                    requestBuilder.setAdditionalAuthenticatedData(
                        ByteString.copyFrom(encryptionContext, StandardCharsets.UTF_8)
                    );
                }
                
                EncryptRequest request = requestBuilder.build();
                EncryptResponse response = kmsClient.encrypt(request);
                
                byte[] encryptedKey = response.getCiphertext().toByteArray();
                
                log.debug("Generated data key using Google Cloud KMS key: {}", effectiveKeyName);
                
                return new DataKey(
                    plaintextKey,
                    encryptedKey,
                    keyId != null ? keyId : "default"
                );
            } catch (Exception e) {
                log.error("Data key generation failed with Google Cloud KMS", e);
                throw new RuntimeException("Google Cloud KMS data key generation failed", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<KeyRotationResult> rotateKey(String keyId) {
        return Mono.fromCallable(() -> {
            try {
                String effectiveKeyName = buildKeyName(keyId);
                
                // Get the crypto key
                CryptoKey cryptoKey = kmsClient.getCryptoKey(effectiveKeyName);
                
                // Check if automatic rotation is enabled
                if (cryptoKey.hasRotationPeriod()) {
                    log.info("Automatic key rotation is enabled for key: {}", effectiveKeyName);
                    
                    // Increment version counter
                    int newVersion = keyVersions.getOrDefault(keyId, 1) + 1;
                    keyVersions.put(keyId, newVersion);
                    
                    return new KeyRotationResult(
                        true,
                        String.valueOf(newVersion),
                        "Key rotation scheduled (automatic rotation enabled)"
                    );
                } else {
                    log.warn("Automatic key rotation is not enabled for key: {}", effectiveKeyName);
                    return new KeyRotationResult(
                        false,
                        null,
                        "Automatic key rotation is not enabled. Enable it in Google Cloud Console."
                    );
                }
            } catch (Exception e) {
                log.error("Key rotation failed for keyId: {}", keyId, e);
                return new KeyRotationResult(
                    false,
                    null,
                    "Key rotation failed: " + e.getMessage()
                );
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> validateKey(String keyId) {
        return Mono.fromCallable(() -> {
            try {
                String effectiveKeyName = buildKeyName(keyId);
                
                // Get the crypto key to validate it exists and is enabled
                CryptoKey cryptoKey = kmsClient.getCryptoKey(effectiveKeyName);

                // Check if the primary version is enabled
                boolean isValid = cryptoKey.hasPrimary() &&
                    cryptoKey.getPrimary().getState() == CryptoKeyVersion.CryptoKeyVersionState.ENABLED;
                
                if (isValid) {
                    log.debug("Key validation successful for: {}", effectiveKeyName);
                } else {
                    log.warn("Key is not enabled: {}", effectiveKeyName);
                }
                
                return isValid;
            } catch (Exception e) {
                log.error("Key validation failed for keyId: {}", keyId, e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE_CLOUD_KMS;
    }

    /**
     * Build the full key name for Google Cloud KMS
     */
    private String buildKeyName(String keyId) {
        if (keyId == null || keyId.equals("default")) {
            return this.keyName;
        }
        
        // If keyId is a full resource name, use it directly
        if (keyId.startsWith("projects/")) {
            return keyId;
        }
        
        // Otherwise, use it as a crypto key ID with the configured project/location/keyring
        String projectId = properties.getEncryption().getGoogleCloudKms().getProjectId();
        String locationId = properties.getEncryption().getGoogleCloudKms().getLocationId();
        String keyRingId = properties.getEncryption().getGoogleCloudKms().getKeyRingId();
        
        return CryptoKeyName.of(projectId, locationId, keyRingId, keyId).toString();
    }

    /**
     * Build metadata map from encryption context
     */
    private String buildMetadata(String encryptionContext) {
        if (encryptionContext == null || encryptionContext.isEmpty()) {
            return "{}";
        }
        return "{\"context\":\"" + Base64.getEncoder().encodeToString(
            encryptionContext.getBytes(StandardCharsets.UTF_8)) + "\"}";
    }

    @PreDestroy
    public void cleanup() {
        if (kmsClient != null) {
            kmsClient.close();
            log.info("Google Cloud KMS client closed successfully");
        }
    }
}

