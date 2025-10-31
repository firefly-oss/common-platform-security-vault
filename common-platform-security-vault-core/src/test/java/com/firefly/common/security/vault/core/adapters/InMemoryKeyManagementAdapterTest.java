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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for InMemoryKeyManagementAdapter
 */
@DisplayName("InMemoryKeyManagementAdapter Tests")
class InMemoryKeyManagementAdapterTest {

    private InMemoryKeyManagementAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InMemoryKeyManagementAdapter();
    }

    @Test
    @DisplayName("Should return IN_MEMORY provider type")
    void shouldReturnInMemoryProviderType() {
        assertThat(adapter.getProviderType()).isEqualTo(KeyManagementPort.ProviderType.IN_MEMORY);
    }

    @Test
    @DisplayName("Should encrypt and decrypt data successfully")
    void shouldEncryptAndDecryptSuccessfully() {
        // Given
        String keyId = "test-key";
        String plaintext = "sensitive-data";
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        // When & Then - Encrypt
        StepVerifier.create(adapter.encrypt(plaintextBytes, keyId, "test-context"))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.ciphertext()).isNotNull();
                assertThat(result.ciphertext()).isNotEqualTo(plaintextBytes);
                assertThat(result.keyId()).isEqualTo(keyId);
                assertThat(result.algorithm()).isEqualTo("AES-256-GCM");
                assertThat(result.metadata()).contains("keyId=" + keyId);
                assertThat(result.metadata()).contains("context=test-context");

                // Decrypt
                StepVerifier.create(adapter.decrypt(result.ciphertext(), keyId, "test-context"))
                    .assertNext(decrypted -> {
                        assertThat(decrypted).isEqualTo(plaintextBytes);
                        assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo(plaintext);
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt different data with different ciphertexts")
    void shouldEncryptDifferentDataWithDifferentCiphertexts() {
        // Given
        String keyId = "test-key";
        byte[] plaintext1 = "data1".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext2 = "data2".getBytes(StandardCharsets.UTF_8);

        // When & Then
        StepVerifier.create(
            adapter.encrypt(plaintext1, keyId, null)
                .zipWith(adapter.encrypt(plaintext2, keyId, null))
        )
        .assertNext(tuple -> {
            assertThat(tuple.getT1().ciphertext()).isNotEqualTo(tuple.getT2().ciphertext());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt same data with different IVs")
    void shouldEncryptSameDataWithDifferentIVs() {
        // Given
        String keyId = "test-key";
        byte[] plaintext = "same-data".getBytes(StandardCharsets.UTF_8);

        // When & Then - Encrypt twice
        StepVerifier.create(
            adapter.encrypt(plaintext, keyId, null)
                .zipWith(adapter.encrypt(plaintext, keyId, null))
        )
        .assertNext(tuple -> {
            // Ciphertexts should be different due to different IVs
            assertThat(tuple.getT1().ciphertext()).isNotEqualTo(tuple.getT2().ciphertext());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should generate data key successfully")
    void shouldGenerateDataKeySuccessfully() {
        // Given
        String keyId = "master-key";

        // When & Then
        StepVerifier.create(adapter.generateDataKey(keyId, "AES_256"))
            .assertNext(dataKey -> {
                assertThat(dataKey).isNotNull();
                assertThat(dataKey.plaintextKey()).isNotNull();
                assertThat(dataKey.plaintextKey()).hasSize(32); // 256 bits = 32 bytes
                assertThat(dataKey.encryptedKey()).isNotNull();
                assertThat(dataKey.encryptedKey()).isNotEqualTo(dataKey.plaintextKey());
                assertThat(dataKey.keyId()).isEqualTo(keyId);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should generate different data keys on each call")
    void shouldGenerateDifferentDataKeys() {
        // Given
        String keyId = "master-key";

        // When & Then
        StepVerifier.create(
            adapter.generateDataKey(keyId, "AES_256")
                .zipWith(adapter.generateDataKey(keyId, "AES_256"))
        )
        .assertNext(tuple -> {
            assertThat(tuple.getT1().plaintextKey()).isNotEqualTo(tuple.getT2().plaintextKey());
            assertThat(tuple.getT1().encryptedKey()).isNotEqualTo(tuple.getT2().encryptedKey());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should rotate key successfully")
    void shouldRotateKeySuccessfully() {
        // Given
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(adapter.rotateKey(keyId))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.success()).isTrue();
                assertThat(result.newVersion()).isNotNull();
                assertThat(result.message()).contains("successfully");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should increment version on key rotation")
    void shouldIncrementVersionOnRotation() {
        // Given
        String keyId = "test-key";

        // When & Then - First rotation
        StepVerifier.create(adapter.rotateKey(keyId))
            .assertNext(result1 -> {
                assertThat(result1.newVersion()).isEqualTo("2");

                // Second rotation
                StepVerifier.create(adapter.rotateKey(keyId))
                    .assertNext(result2 -> {
                        assertThat(result2.newVersion()).isEqualTo("3");
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate existing key")
    void shouldValidateExistingKey() {
        // Given
        String keyId = "default-master-key"; // Created in constructor

        // When & Then
        StepVerifier.create(adapter.validateKey(keyId))
            .assertNext(isValid -> {
                assertThat(isValid).isTrue();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should not validate non-existing key")
    void shouldNotValidateNonExistingKey() {
        // Given
        String keyId = "non-existing-key";

        // When & Then
        StepVerifier.create(adapter.validateKey(keyId))
            .assertNext(isValid -> {
                assertThat(isValid).isFalse();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should create key automatically on first encrypt")
    void shouldCreateKeyAutomaticallyOnFirstEncrypt() {
        // Given
        String keyId = "auto-created-key";
        byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);

        // When & Then
        StepVerifier.create(adapter.encrypt(plaintext, keyId, null))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                
                // Verify key was created
                StepVerifier.create(adapter.validateKey(keyId))
                    .assertNext(isValid -> assertThat(isValid).isTrue())
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should fail decryption with wrong key")
    void shouldFailDecryptionWithWrongKey() {
        // Given
        String keyId1 = "key1";
        String keyId2 = "key2";
        byte[] plaintext = "secret".getBytes(StandardCharsets.UTF_8);

        // When & Then
        StepVerifier.create(
            adapter.encrypt(plaintext, keyId1, null)
                .flatMap(result -> adapter.decrypt(result.ciphertext(), keyId2, null))
        )
        .expectError(RuntimeException.class)
        .verify();
    }

    @Test
    @DisplayName("Should handle empty plaintext")
    void shouldHandleEmptyPlaintext() {
        // Given
        String keyId = "test-key";
        byte[] plaintext = new byte[0];

        // When & Then
        StepVerifier.create(adapter.encrypt(plaintext, keyId, null))
            .assertNext(result -> {
                assertThat(result.ciphertext()).isNotNull();
                
                // Decrypt
                StepVerifier.create(adapter.decrypt(result.ciphertext(), keyId, null))
                    .assertNext(decrypted -> {
                        assertThat(decrypted).isEmpty();
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle large plaintext")
    void shouldHandleLargePlaintext() {
        // Given
        String keyId = "test-key";
        byte[] plaintext = new byte[10000]; // 10KB
        for (int i = 0; i < plaintext.length; i++) {
            plaintext[i] = (byte) (i % 256);
        }

        // When & Then
        StepVerifier.create(adapter.encrypt(plaintext, keyId, null))
            .assertNext(result -> {
                assertThat(result.ciphertext()).isNotNull();
                
                // Decrypt
                StepVerifier.create(adapter.decrypt(result.ciphertext(), keyId, null))
                    .assertNext(decrypted -> {
                        assertThat(decrypted).isEqualTo(plaintext);
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null context")
    void shouldHandleNullContext() {
        // Given
        String keyId = "test-key";
        byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);

        // When & Then
        StepVerifier.create(adapter.encrypt(plaintext, keyId, null))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.metadata()).contains("context=null");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should decrypt with null context if encrypted with null context")
    void shouldDecryptWithNullContext() {
        // Given
        String keyId = "test-key";
        byte[] plaintext = "test".getBytes(StandardCharsets.UTF_8);

        // When & Then
        StepVerifier.create(
            adapter.encrypt(plaintext, keyId, null)
                .flatMap(result -> adapter.decrypt(result.ciphertext(), keyId, null))
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }
}

