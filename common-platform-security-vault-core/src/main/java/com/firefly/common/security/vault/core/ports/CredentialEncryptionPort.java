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


package com.firefly.common.security.vault.core.ports;

import reactor.core.publisher.Mono;

/**
 * Port (interface) for Credential Encryption operations
 * 
 * This is the application-level port that defines high-level
 * encryption operations for credentials. It uses KeyManagementPort
 * internally but provides credential-specific functionality.
 */
public interface CredentialEncryptionPort {

    /**
     * Encrypt a credential value
     * 
     * @param plaintext Credential value to encrypt
     * @param keyId Key identifier (optional, uses default if null)
     * @return Encryption result with encrypted value and metadata
     */
    Mono<CredentialEncryptionResult> encryptCredential(String plaintext, String keyId);

    /**
     * Decrypt a credential value
     * 
     * @param encryptedValue Encrypted credential value
     * @param keyId Key identifier used for encryption
     * @param iv Initialization vector used for encryption
     * @return Decrypted credential value
     */
    Mono<String> decryptCredential(String encryptedValue, String keyId, String iv);

    /**
     * Rotate encryption for a credential (decrypt with old key, encrypt with new key)
     * 
     * @param encryptedValue Current encrypted value
     * @param currentKeyId Current key identifier
     * @param currentIv Current initialization vector
     * @param newKeyId New key identifier
     * @return New encryption result
     */
    Mono<CredentialEncryptionResult> rotateCredentialEncryption(
        String encryptedValue, 
        String currentKeyId, 
        String currentIv, 
        String newKeyId
    );

    /**
     * Generate a new encryption key
     * 
     * @param keyId Key identifier
     * @return Key generation result
     */
    Mono<KeyGenerationResult> generateEncryptionKey(String keyId);

    /**
     * Validate an encryption key
     * 
     * @param keyId Key identifier
     * @return true if key is valid
     */
    Mono<Boolean> validateEncryptionKey(String keyId);

    /**
     * Get the current encryption provider type
     * 
     * @return Provider type
     */
    String getProviderType();

    /**
     * Credential encryption result
     */
    record CredentialEncryptionResult(
        String encryptedValue,
        String iv,
        String algorithm,
        String keyId
    ) {}

    /**
     * Key generation result
     */
    record KeyGenerationResult(
        String keyId,
        String algorithm,
        String provider,
        boolean success
    ) {}
}

