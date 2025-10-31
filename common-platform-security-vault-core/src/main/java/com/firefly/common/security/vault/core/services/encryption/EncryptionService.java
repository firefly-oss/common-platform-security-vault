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

import reactor.core.publisher.Mono;

/**
 * Service for encrypting and decrypting credential data
 * Uses AES-256-GCM with unique IVs per credential
 */
public interface EncryptionService {

    /**
     * Encrypt sensitive data using AES-256-GCM
     * 
     * @param plaintext The data to encrypt
     * @param keyId The encryption key ID to use
     * @return Encrypted data result with encrypted value and IV
     */
    Mono<EncryptionResult> encrypt(String plaintext, String keyId);

    /**
     * Decrypt encrypted data
     * 
     * @param encryptedValue The encrypted data
     * @param keyId The encryption key ID used
     * @param iv The initialization vector used during encryption
     * @return Decrypted plaintext
     */
    Mono<String> decrypt(String encryptedValue, String keyId, String iv);

    /**
     * Rotate encryption by re-encrypting with a new key
     * 
     * @param encryptedValue Current encrypted value
     * @param currentKeyId Current key ID
     * @param currentIv Current IV
     * @param newKeyId New key ID to use
     * @return New encryption result
     */
    Mono<EncryptionResult> rotateEncryption(String encryptedValue, String currentKeyId, 
                                           String currentIv, String newKeyId);

    /**
     * Generate a new encryption key
     * 
     * @param keyId The ID for the new key
     * @param algorithm The algorithm (e.g., "AES-256-GCM")
     * @return Key generation result
     */
    Mono<KeyGenerationResult> generateKey(String keyId, String algorithm);

    /**
     * Validate encryption key
     * 
     * @param keyId The key ID to validate
     * @return True if key is valid and accessible
     */
    Mono<Boolean> validateKey(String keyId);

    /**
     * Result of encryption operation
     */
    record EncryptionResult(
        String encryptedValue,
        String iv,
        String algorithm,
        String keyId
    ) {}

    /**
     * Result of key generation
     */
    record KeyGenerationResult(
        String keyId,
        String algorithm,
        String keyLocation,
        boolean success
    ) {}
}
