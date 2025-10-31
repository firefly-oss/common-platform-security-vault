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
 * Interface for Key Management System (KMS) providers
 * 
 * Supported KMS:
 * - AWS KMS
 * - Azure Key Vault
 * - HashiCorp Vault
 * - Google Cloud KMS
 * 
 * This abstraction allows switching between different KMS providers
 * without changing the core vault logic.
 */
public interface KmsProvider {

    /**
     * Encrypt data using KMS
     * 
     * @param plaintext Data to encrypt
     * @param keyId KMS key identifier
     * @param context Additional encryption context (for AWS KMS)
     * @return Encrypted data with metadata
     */
    Mono<KmsEncryptionResult> encrypt(byte[] plaintext, String keyId, String context);

    /**
     * Decrypt data using KMS
     * 
     * @param ciphertext Encrypted data
     * @param keyId KMS key identifier
     * @param context Encryption context used during encryption
     * @return Decrypted data
     */
    Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String context);

    /**
     * Generate a data encryption key (DEK)
     * 
     * @param keyId Master key identifier
     * @param keySpec Key specification (e.g., AES_256)
     * @return Generated key with encrypted version
     */
    Mono<DataKey> generateDataKey(String keyId, String keySpec);

    /**
     * Rotate a KMS key
     * 
     * @param keyId Key to rotate
     * @return Rotation result
     */
    Mono<KeyRotationResult> rotateKey(String keyId);

    /**
     * Validate KMS key accessibility
     * 
     * @param keyId Key to validate
     * @return True if key is accessible
     */
    Mono<Boolean> validateKey(String keyId);

    /**
     * Get KMS provider type
     */
    KmsType getProviderType();

    /**
     * KMS encryption result
     */
    record KmsEncryptionResult(
        byte[] ciphertext,
        String keyId,
        String algorithm,
        String metadata
    ) {}

    /**
     * Data encryption key (plaintext + encrypted)
     */
    record DataKey(
        byte[] plaintextKey,
        byte[] encryptedKey,
        String keyId
    ) {}

    /**
     * Key rotation result
     */
    record KeyRotationResult(
        boolean success,
        String newKeyVersion,
        String message
    ) {}

    /**
     * Supported KMS types
     */
    enum KmsType {
        AWS_KMS,
        AZURE_KEY_VAULT,
        HASHICORP_VAULT,
        GOOGLE_CLOUD_KMS,
        IN_MEMORY  // For development/testing only
    }
}
