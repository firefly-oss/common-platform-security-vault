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

import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory adapter for Key Management
 * 
 * WARNING: This adapter stores keys in memory and is NOT suitable for production.
 * Use this only for development and testing.
 * 
 * For production, use:
 * - AwsKmsKeyManagementAdapter
 * - AzureKeyVaultKeyManagementAdapter
 * - HashiCorpVaultKeyManagementAdapter
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "firefly.security.vault.encryption",
    name = "provider",
    havingValue = "IN_MEMORY",
    matchIfMissing = true
)
public class InMemoryKeyManagementAdapter implements KeyManagementPort {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private final Map<String, SecretKey> keyStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> keyVersions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public InMemoryKeyManagementAdapter() {
        log.warn("Using IN_MEMORY Key Management Adapter - NOT SUITABLE FOR PRODUCTION");
        // Initialize default master key
        try {
            generateMasterKey("default-master-key");
        } catch (Exception e) {
            log.error("Failed to initialize default master key", e);
        }
    }

    @Override
    public Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String context) {
        return Mono.fromCallable(() -> {
            try {
                SecretKey key = getOrCreateKey(keyId);
                
                byte[] iv = new byte[GCM_IV_LENGTH];
                secureRandom.nextBytes(iv);
                
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
                
                byte[] ciphertext = cipher.doFinal(plaintext);
                
                // Combine IV + ciphertext for storage
                byte[] combined = new byte[iv.length + ciphertext.length];
                System.arraycopy(iv, 0, combined, 0, iv.length);
                System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
                
                String metadata = String.format("keyId=%s,context=%s,version=%d", 
                    keyId, context, keyVersions.getOrDefault(keyId, 1));
                
                log.debug("Encrypted data with key: {}", keyId);
                
                return new EncryptionResult(
                    combined,
                    keyId,
                    "AES-256-GCM",
                    metadata
                );
                
            } catch (Exception e) {
                log.error("Encryption failed for keyId: {}", keyId, e);
                throw new RuntimeException("Encryption failed", e);
            }
        });
    }

    @Override
    public Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String context) {
        return Mono.fromCallable(() -> {
            try {
                SecretKey key = keyStore.get(keyId);
                if (key == null) {
                    throw new IllegalArgumentException("Key not found: " + keyId);
                }
                
                // Extract IV and ciphertext
                byte[] iv = new byte[GCM_IV_LENGTH];
                byte[] actualCiphertext = new byte[ciphertext.length - GCM_IV_LENGTH];
                System.arraycopy(ciphertext, 0, iv, 0, GCM_IV_LENGTH);
                System.arraycopy(ciphertext, GCM_IV_LENGTH, actualCiphertext, 0, actualCiphertext.length);
                
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
                
                byte[] plaintext = cipher.doFinal(actualCiphertext);
                
                log.debug("Decrypted data with key: {}", keyId);
                
                return plaintext;
                
            } catch (Exception e) {
                log.error("Decryption failed for keyId: {}", keyId, e);
                throw new RuntimeException("Decryption failed", e);
            }
        });
    }

    @Override
    public Mono<DataKey> generateDataKey(String keyId, String keySpec) {
        return Mono.fromCallable(() -> {
            try {
                // Generate plaintext data key
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(AES_KEY_SIZE, secureRandom);
                SecretKey dataKey = keyGenerator.generateKey();
                byte[] plaintextKey = dataKey.getEncoded();
                
                // Encrypt the data key with master key
                SecretKey masterKey = getOrCreateKey(keyId);
                
                byte[] iv = new byte[GCM_IV_LENGTH];
                secureRandom.nextBytes(iv);
                
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, masterKey, parameterSpec);
                
                byte[] encryptedKeyData = cipher.doFinal(plaintextKey);
                
                // Combine IV + encrypted key
                byte[] encryptedKey = new byte[iv.length + encryptedKeyData.length];
                System.arraycopy(iv, 0, encryptedKey, 0, iv.length);
                System.arraycopy(encryptedKeyData, 0, encryptedKey, iv.length, encryptedKeyData.length);
                
                log.debug("Generated data key with master key: {}", keyId);
                
                return new DataKey(plaintextKey, encryptedKey, keyId);
                
            } catch (Exception e) {
                log.error("Data key generation failed for keyId: {}", keyId, e);
                throw new RuntimeException("Data key generation failed", e);
            }
        });
    }

    @Override
    public Mono<KeyRotationResult> rotateKey(String keyId) {
        return Mono.fromCallable(() -> {
            try {
                // Generate new key version
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(AES_KEY_SIZE, secureRandom);
                SecretKey newKey = keyGenerator.generateKey();
                
                // Update key store
                keyStore.put(keyId, newKey);
                
                // Increment version
                int newVersion = keyVersions.compute(keyId, (k, v) -> v == null ? 2 : v + 1);
                
                log.info("Rotated key: {} to version: {}", keyId, newVersion);
                
                return new KeyRotationResult(
                    true,
                    String.valueOf(newVersion),
                    "Key rotated successfully"
                );
                
            } catch (Exception e) {
                log.error("Key rotation failed for keyId: {}", keyId, e);
                return new KeyRotationResult(
                    false,
                    null,
                    "Key rotation failed: " + e.getMessage()
                );
            }
        });
    }

    @Override
    public Mono<Boolean> validateKey(String keyId) {
        return Mono.fromCallable(() -> {
            boolean exists = keyStore.containsKey(keyId);
            log.debug("Key validation for {}: {}", keyId, exists);
            return exists;
        });
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.IN_MEMORY;
    }

    /**
     * Get existing key or create new one
     */
    private SecretKey getOrCreateKey(String keyId) throws Exception {
        SecretKey existingKey = keyStore.get(keyId);
        if (existingKey != null) {
            return existingKey;
        }

        // Create new key outside of computeIfAbsent to avoid recursive update
        return generateMasterKey(keyId);
    }

    /**
     * Generate a new master key
     */
    private SecretKey generateMasterKey(String keyId) throws Exception {
        // Check again if key was created by another thread
        SecretKey existingKey = keyStore.get(keyId);
        if (existingKey != null) {
            return existingKey;
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        keyStore.put(keyId, key);
        keyVersions.put(keyId, 1);
        log.info("Generated new master key: {}", keyId);
        return key;
    }
}

