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

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * HashiCorp Vault adapter for Key Management
 * 
 * This adapter integrates with HashiCorp Vault Transit Engine for
 * enterprise-grade key management in on-premise or hybrid environments.
 * 
 * Features:
 * - Integration with Vault Transit Engine
 * - Encryption as a Service
 * - Automatic key rotation
 * - Key versioning
 * - Convergent encryption support
 * - Namespace support (Vault Enterprise)
 * 
 * Prerequisites:
 * - HashiCorp Vault Java Driver dependency in classpath
 * - Vault server accessible
 * - Transit engine enabled
 * - Valid Vault token
 * 
 * @see <a href="https://www.vaultproject.io/docs/secrets/transit">Vault Transit Engine</a>
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "firefly.security.vault.encryption",
    name = "provider",
    havingValue = "HASHICORP_VAULT"
)
@ConditionalOnClass(name = "com.bettercloud.vault.Vault")
public class HashiCorpVaultKeyManagementAdapter implements KeyManagementPort {

    private final SecurityVaultProperties properties;
    private final Vault vault;
    private final String transitPath;
    private final String keyName;

    public HashiCorpVaultKeyManagementAdapter(SecurityVaultProperties properties) {
        this.properties = properties;
        this.transitPath = properties.getEncryption().getHashicorpVault().getTransitPath();
        this.keyName = properties.getEncryption().getHashicorpVault().getKeyName();
        this.vault = initializeVaultClient();
        log.info("HashiCorp Vault Key Management Adapter initialized - Address: {}",
            maskAddress(properties.getEncryption().getHashicorpVault().getAddress()));
    }

    @Override
    public Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String context) {
        log.debug("Encrypting data with HashiCorp Vault Transit");
        
        return Mono.fromCallable(() -> {
            String effectiveKeyName = keyId != null ? keyId : this.keyName;
            String path = String.format("%s/encrypt/%s", transitPath, effectiveKeyName);
            
            // Encode plaintext to Base64 (required by Vault)
            String base64Plaintext = Base64.getEncoder().encodeToString(plaintext);
            
            // Prepare request
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("plaintext", base64Plaintext);
            if (context != null) {
                requestData.put("context", Base64.getEncoder().encodeToString(context.getBytes(StandardCharsets.UTF_8)));
            }
            
            // Call Vault
            LogicalResponse response = vault.logical().write(path, requestData);
            
            if (response.getRestResponse().getStatus() != 200) {
                throw new RuntimeException("Vault encryption failed: " + response.getRestResponse().getStatus());
            }
            
            String ciphertext = response.getData().get("ciphertext");
            
            log.debug("Successfully encrypted data with HashiCorp Vault");
            return new EncryptionResult(
                ciphertext.getBytes(StandardCharsets.UTF_8),
                effectiveKeyName,
                "VAULT_TRANSIT_AES256_GCM96",
                buildMetadata(context, effectiveKeyName)
            );
        })
        .doOnError(error -> log.error("HashiCorp Vault encryption failed: {}", error.getMessage()))
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String context) {
        log.debug("Decrypting data with HashiCorp Vault Transit");
        
        return Mono.fromCallable(() -> {
            String effectiveKeyName = keyId != null ? keyId : this.keyName;
            String path = String.format("%s/decrypt/%s", transitPath, effectiveKeyName);
            
            // Ciphertext should be in Vault format (vault:v1:...)
            String ciphertextStr = new String(ciphertext, StandardCharsets.UTF_8);
            
            // Prepare request
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ciphertext", ciphertextStr);
            if (context != null) {
                requestData.put("context", Base64.getEncoder().encodeToString(context.getBytes(StandardCharsets.UTF_8)));
            }
            
            // Call Vault
            LogicalResponse response = vault.logical().write(path, requestData);
            
            if (response.getRestResponse().getStatus() != 200) {
                throw new RuntimeException("Vault decryption failed: " + response.getRestResponse().getStatus());
            }
            
            String base64Plaintext = response.getData().get("plaintext");
            byte[] plaintext = Base64.getDecoder().decode(base64Plaintext);
            
            log.debug("Successfully decrypted data with HashiCorp Vault");
            return plaintext;
        })
        .doOnError(error -> log.error("HashiCorp Vault decryption failed: {}", error.getMessage()))
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DataKey> generateDataKey(String keyId, String context) {
        log.debug("Generating data key with HashiCorp Vault Transit");
        
        return Mono.fromCallable(() -> {
            String effectiveKeyName = keyId != null ? keyId : this.keyName;
            String path = String.format("%s/datakey/plaintext/%s", transitPath, effectiveKeyName);
            
            // Prepare request
            Map<String, Object> requestData = new HashMap<>();
            if (context != null) {
                requestData.put("context", Base64.getEncoder().encodeToString(context.getBytes(StandardCharsets.UTF_8)));
            }
            
            // Call Vault
            LogicalResponse response = vault.logical().write(path, requestData);
            
            if (response.getRestResponse().getStatus() != 200) {
                throw new RuntimeException("Vault data key generation failed: " + response.getRestResponse().getStatus());
            }
            
            String base64Plaintext = response.getData().get("plaintext");
            String ciphertext = response.getData().get("ciphertext");
            
            byte[] plaintextKey = Base64.getDecoder().decode(base64Plaintext);
            byte[] encryptedKey = ciphertext.getBytes(StandardCharsets.UTF_8);
            
            log.debug("Successfully generated data key with HashiCorp Vault");
            return new DataKey(
                plaintextKey,
                encryptedKey,
                effectiveKeyName
            );
        })
        .doOnError(error -> log.error("HashiCorp Vault data key generation failed: {}", error.getMessage()))
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<KeyRotationResult> rotateKey(String keyId) {
        log.info("Rotating key in HashiCorp Vault Transit");
        
        return Mono.fromCallable(() -> {
            String effectiveKeyName = keyId != null ? keyId : this.keyName;
            String path = String.format("%s/keys/%s/rotate", transitPath, effectiveKeyName);
            
            try {
                LogicalResponse response = vault.logical().write(path, null);
                
                if (response.getRestResponse().getStatus() == 204 || response.getRestResponse().getStatus() == 200) {
                    log.info("Successfully rotated key in HashiCorp Vault");
                    return new KeyRotationResult(
                        true,
                        "vault-rotated",
                        "HashiCorp Vault key rotated successfully. New version created."
                    );
                } else {
                    throw new RuntimeException("Vault key rotation failed: " + response.getRestResponse().getStatus());
                }
            } catch (VaultException e) {
                log.error("Failed to rotate key {}: {}", effectiveKeyName, e.getMessage());
                return new KeyRotationResult(
                    false,
                    null,
                    "Key rotation failed: " + e.getMessage()
                );
            }
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> validateKey(String keyId) {
        log.debug("Validating HashiCorp Vault key");
        
        return Mono.fromCallable(() -> {
            String effectiveKeyName = keyId != null ? keyId : this.keyName;
            String path = String.format("%s/keys/%s", transitPath, effectiveKeyName);
            
            try {
                LogicalResponse response = vault.logical().read(path);
                
                if (response.getRestResponse().getStatus() == 200) {
                    Map<String, String> data = response.getData();
                    boolean deletionAllowed = Boolean.parseBoolean(data.getOrDefault("deletion_allowed", "false"));
                    
                    // Key exists and is not deleted
                    boolean isValid = !deletionAllowed || data.containsKey("keys");
                    
                    log.debug("Key {} validation result: {}", effectiveKeyName, isValid);
                    return isValid;
                } else {
                    log.warn("Key validation returned status: {}", response.getRestResponse().getStatus());
                    return false;
                }
            } catch (VaultException e) {
                log.error("Key validation failed for {}: {}", effectiveKeyName, e.getMessage());
                return false;
            }
        })
        .onErrorResume(e -> {
            log.error("Key validation failed: {}", e.getMessage());
            return Mono.just(false);
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.HASHICORP_VAULT;
    }

    /**
     * Initialize HashiCorp Vault client
     * 
     * Creates Vault client with:
     * - Vault address
     * - Authentication token
     * - Namespace (for Vault Enterprise)
     * - TLS configuration
     */
    private Vault initializeVaultClient() {
        log.info("Initializing HashiCorp Vault client...");
        
        SecurityVaultProperties.HashiCorpVaultConfig config = properties.getEncryption().getHashicorpVault();
        
        // Validate configuration
        if (config.getAddress() == null || config.getAddress().isEmpty()) {
            throw new IllegalStateException(
                "Vault address is required. Set firefly.security.vault.encryption.hashicorp-vault.address"
            );
        }
        
        if (config.getToken() == null || config.getToken().isEmpty()) {
            throw new IllegalStateException(
                "Vault token is required. Set firefly.security.vault.encryption.hashicorp-vault.token"
            );
        }
        
        if (config.getKeyName() == null || config.getKeyName().isEmpty()) {
            throw new IllegalStateException(
                "Vault key name is required. Set firefly.security.vault.encryption.hashicorp-vault.key-name"
            );
        }
        
        log.info("HashiCorp Vault Configuration:");
        log.info("  Address: {}", maskAddress(config.getAddress()));
        log.info("  Transit Path: {}", config.getTransitPath());
        log.info("  Key Name: {}", config.getKeyName());
        log.info("  Namespace: {}", config.getNamespace() != null ? config.getNamespace() : "default");
        
        try {
            VaultConfig vaultConfig = new VaultConfig()
                .address(config.getAddress())
                .token(config.getToken())
                .build();
            
            // Add namespace if configured (Vault Enterprise)
            if (config.getNamespace() != null && !config.getNamespace().isEmpty()) {
                vaultConfig = vaultConfig.nameSpace(config.getNamespace());
            }
            
            Vault vaultClient = new Vault(vaultConfig);
            
            log.info("HashiCorp Vault client initialized successfully");
            
            return vaultClient;
        } catch (VaultException e) {
            throw new IllegalStateException("Failed to initialize Vault client: " + e.getMessage(), e);
        }
    }

    /**
     * Mask Vault address for logging
     */
    private String maskAddress(String address) {
        if (address == null) {
            return "***";
        }
        try {
            String domain = address.replaceAll("https?://", "").split(":")[0];
            return "https://" + domain.substring(0, Math.min(10, domain.length())) + "***";
        } catch (Exception e) {
            return "***";
        }
    }

    /**
     * Build metadata string from encryption context and key name
     */
    private String buildMetadata(String context, String keyName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("provider", "HASHICORP_VAULT");
        metadata.put("address", maskAddress(properties.getEncryption().getHashicorpVault().getAddress()));
        metadata.put("transitPath", transitPath);
        metadata.put("keyName", keyName);
        if (context != null) {
            metadata.put("context", context);
        }
        return metadata.toString();
    }
}

