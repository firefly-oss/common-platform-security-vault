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
 * Port (interface) for Key Management operations
 * 
 * This is the hexagonal architecture port that defines the contract
 * for key management operations. Different adapters can implement this
 * interface (InMemory, AWS KMS, Azure Key Vault, etc.)
 */
public interface KeyManagementPort {

    /**
     * Encrypt data using the specified key
     * 
     * @param plaintext Data to encrypt
     * @param keyId Key identifier
     * @param context Additional context for encryption (optional)
     * @return Encryption result with ciphertext and metadata
     */
    Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String context);

    /**
     * Decrypt data using the specified key
     * 
     * @param ciphertext Encrypted data
     * @param keyId Key identifier
     * @param context Additional context for decryption (optional)
     * @return Decrypted plaintext
     */
    Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String context);

    /**
     * Generate a new data encryption key (DEK)
     * 
     * @param keyId Master key identifier
     * @param keySpec Key specification (e.g., AES_256)
     * @return Data key with plaintext and encrypted versions
     */
    Mono<DataKey> generateDataKey(String keyId, String keySpec);

    /**
     * Rotate a key to a new version
     * 
     * @param keyId Key identifier to rotate
     * @return Rotation result with new version information
     */
    Mono<KeyRotationResult> rotateKey(String keyId);

    /**
     * Validate that a key exists and is usable
     * 
     * @param keyId Key identifier
     * @return true if key is valid and usable
     */
    Mono<Boolean> validateKey(String keyId);

    /**
     * Get the provider type
     * 
     * @return Provider type (IN_MEMORY, AWS_KMS, AZURE_KEY_VAULT, etc.)
     */
    ProviderType getProviderType();

    /**
     * Encryption result containing ciphertext and metadata
     */
    record EncryptionResult(
        byte[] ciphertext,
        String keyId,
        String algorithm,
        String metadata
    ) {}

    /**
     * Data encryption key with plaintext and encrypted versions
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
        String newVersion,
        String message
    ) {}

    /**
     * Provider types
     */
    enum ProviderType {
        IN_MEMORY,
        AWS_KMS,
        AZURE_KEY_VAULT,
        HASHICORP_VAULT,
        GOOGLE_CLOUD_KMS
    }
}

