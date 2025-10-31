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


package com.firefly.common.security.vault.core.services.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AES-256-GCM encryption service implementation
 * 
 * Security Features:
 * - AES-256 encryption with GCM mode for authenticated encryption
 * - Unique IV (Initialization Vector) per encryption operation
 * - 128-bit authentication tag for integrity verification
 * - Secure key storage in memory with future KMS integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AesGcmEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int AES_KEY_SIZE = 256; // 256 bits
    
    // In production, this should be replaced with KMS (AWS KMS, Azure Key Vault, HashiCorp Vault)
    private final Map<String, SecretKey> keyStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public Mono<EncryptionResult> encrypt(String plaintext, String keyId) {
        return Mono.fromCallable(() -> {
            try {
                // Get or generate encryption key
                SecretKey key = getOrGenerateKey(keyId);
                
                // Generate unique IV for this encryption
                byte[] iv = generateIv();
                
                // Initialize cipher
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
                
                // Encrypt
                byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
                
                // Encode to Base64 for storage
                String encryptedValue = Base64.getEncoder().encodeToString(encryptedBytes);
                String ivBase64 = Base64.getEncoder().encodeToString(iv);
                
                log.debug("Successfully encrypted data with keyId: {}", keyId);
                
                return new EncryptionResult(encryptedValue, ivBase64, "AES-256-GCM", keyId);
                
            } catch (Exception e) {
                log.error("Encryption failed for keyId: {}", keyId, e);
                throw new EncryptionException("Failed to encrypt data", e);
            }
        });
    }

    @Override
    public Mono<String> decrypt(String encryptedValue, String keyId, String iv) {
        return Mono.fromCallable(() -> {
            try {
                // Get encryption key
                SecretKey key = keyStore.get(keyId);
                if (key == null) {
                    throw new EncryptionException("Encryption key not found: " + keyId);
                }
                
                // Decode from Base64
                byte[] encryptedBytes = Base64.getDecoder().decode(encryptedValue);
                byte[] ivBytes = Base64.getDecoder().decode(iv);
                
                // Initialize cipher
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes);
                cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
                
                // Decrypt
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                
                log.debug("Successfully decrypted data with keyId: {}", keyId);
                
                return new String(decryptedBytes, "UTF-8");
                
            } catch (Exception e) {
                log.error("Decryption failed for keyId: {}", keyId, e);
                throw new EncryptionException("Failed to decrypt data", e);
            }
        });
    }

    @Override
    public Mono<EncryptionResult> rotateEncryption(String encryptedValue, String currentKeyId, 
                                                   String currentIv, String newKeyId) {
        return decrypt(encryptedValue, currentKeyId, currentIv)
            .flatMap(plaintext -> encrypt(plaintext, newKeyId))
            .doOnSuccess(result -> log.info("Successfully rotated encryption from {} to {}", 
                                           currentKeyId, newKeyId));
    }

    @Override
    public Mono<KeyGenerationResult> generateKey(String keyId, String algorithm) {
        return Mono.fromCallable(() -> {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(AES_KEY_SIZE, secureRandom);
                SecretKey secretKey = keyGenerator.generateKey();
                
                keyStore.put(keyId, secretKey);
                
                log.info("Generated new encryption key: {}", keyId);
                
                return new KeyGenerationResult(keyId, "AES-256-GCM", "IN_MEMORY", true);
                
            } catch (Exception e) {
                log.error("Key generation failed for keyId: {}", keyId, e);
                throw new EncryptionException("Failed to generate key", e);
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

    /**
     * Generate a cryptographically secure random IV
     */
    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * Get existing key or generate new one
     */
    private SecretKey getOrGenerateKey(String keyId) {
        return keyStore.computeIfAbsent(keyId, id -> {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(AES_KEY_SIZE, secureRandom);
                SecretKey key = keyGenerator.generateKey();
                log.info("Auto-generated encryption key: {}", id);
                return key;
            } catch (Exception e) {
                throw new EncryptionException("Failed to generate key", e);
            }
        });
    }

    /**
     * Custom exception for encryption operations
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message) {
            super(message);
        }
        
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
