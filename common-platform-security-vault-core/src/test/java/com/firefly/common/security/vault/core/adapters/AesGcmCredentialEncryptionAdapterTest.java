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
import com.firefly.common.security.vault.core.ports.CredentialEncryptionPort;
import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AesGcmCredentialEncryptionAdapter
 */
@DisplayName("AesGcmCredentialEncryptionAdapter Tests")
class AesGcmCredentialEncryptionAdapterTest {

    private AesGcmCredentialEncryptionAdapter adapter;
    private InMemoryKeyManagementAdapter keyManagementAdapter;
    private SecurityVaultProperties properties;

    @BeforeEach
    void setUp() {
        keyManagementAdapter = new InMemoryKeyManagementAdapter();
        properties = new SecurityVaultProperties();
        properties.setEncryption(new SecurityVaultProperties.EncryptionConfig());
        properties.getEncryption().setMasterKeyId("test-master-key");
        
        adapter = new AesGcmCredentialEncryptionAdapter(keyManagementAdapter, properties);
    }

    @Test
    @DisplayName("Should return provider type from key management adapter")
    void shouldReturnProviderType() {
        assertThat(adapter.getProviderType()).isEqualTo("IN_MEMORY");
    }

    @Test
    @DisplayName("Should encrypt credential successfully")
    void shouldEncryptCredentialSuccessfully() {
        // Given
        String plaintext = "my-secret-password";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(adapter.encryptCredential(plaintext, keyId))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.encryptedValue()).isNotNull();
                assertThat(result.encryptedValue()).isNotEmpty();
                assertThat(result.iv()).isNotNull();
                assertThat(result.iv()).isNotEmpty();
                assertThat(result.algorithm()).isEqualTo("AES-256-GCM");
                assertThat(result.keyId()).isEqualTo(keyId);
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt and decrypt credential successfully")
    void shouldEncryptAndDecryptSuccessfully() {
        // Given
        String plaintext = "my-secret-api-key";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should use default key ID when null is provided")
    void shouldUseDefaultKeyIdWhenNull() {
        // Given
        String plaintext = "test-data";

        // When & Then
        StepVerifier.create(adapter.encryptCredential(plaintext, null))
            .assertNext(result -> {
                assertThat(result.keyId()).isEqualTo("test-master-key");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt different credentials with different IVs")
    void shouldEncryptWithDifferentIVs() {
        // Given
        String plaintext = "same-password";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .zipWith(adapter.encryptCredential(plaintext, keyId))
        )
        .assertNext(tuple -> {
            assertThat(tuple.getT1().iv()).isNotEqualTo(tuple.getT2().iv());
            assertThat(tuple.getT1().encryptedValue()).isNotEqualTo(tuple.getT2().encryptedValue());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should rotate credential encryption successfully")
    void shouldRotateCredentialEncryption() {
        // Given
        String plaintext = "original-secret";
        String oldKeyId = "old-key";
        String newKeyId = "new-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, oldKeyId)
                .flatMap(oldResult -> 
                    adapter.rotateCredentialEncryption(
                        oldResult.encryptedValue(),
                        oldResult.keyId(),
                        oldResult.iv(),
                        newKeyId
                    )
                )
        )
        .assertNext(newResult -> {
            assertThat(newResult.keyId()).isEqualTo(newKeyId);
            assertThat(newResult.encryptedValue()).isNotNull();
            assertThat(newResult.iv()).isNotNull();
            
            // Verify can decrypt with new key
            StepVerifier.create(
                adapter.decryptCredential(newResult.encryptedValue(), newResult.keyId(), newResult.iv())
            )
            .assertNext(decrypted -> {
                assertThat(decrypted).isEqualTo(plaintext);
            })
            .verifyComplete();
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should generate encryption key successfully")
    void shouldGenerateEncryptionKey() {
        // Given
        String keyId = "new-encryption-key";

        // When & Then
        StepVerifier.create(adapter.generateEncryptionKey(keyId))
            .assertNext(result -> {
                assertThat(result).isNotNull();
                assertThat(result.keyId()).isEqualTo(keyId);
                assertThat(result.algorithm()).isEqualTo("AES-256-GCM");
                assertThat(result.provider()).isEqualTo("IN_MEMORY");
                assertThat(result.success()).isTrue();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should validate existing encryption key")
    void shouldValidateExistingKey() {
        // Given
        String keyId = "default-master-key"; // Created by InMemoryKeyManagementAdapter

        // When & Then
        StepVerifier.create(adapter.validateEncryptionKey(keyId))
            .assertNext(isValid -> {
                assertThat(isValid).isTrue();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should not validate non-existing encryption key")
    void shouldNotValidateNonExistingKey() {
        // Given
        String keyId = "non-existing-key";

        // When & Then
        StepVerifier.create(adapter.validateEncryptionKey(keyId))
            .assertNext(isValid -> {
                assertThat(isValid).isFalse();
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle empty credential")
    void shouldHandleEmptyCredential() {
        // Given
        String plaintext = "";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEmpty();
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should handle long credential")
    void shouldHandleLongCredential() {
        // Given
        String plaintext = "a".repeat(10000); // 10KB credential
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should handle special characters in credential")
    void shouldHandleSpecialCharacters() {
        // Given
        String plaintext = "password!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should handle unicode characters in credential")
    void shouldHandleUnicodeCharacters() {
        // Given
        String plaintext = "密码🔐🔑🗝️";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should handle JSON credential")
    void shouldHandleJsonCredential() {
        // Given
        String plaintext = "{\"username\":\"admin\",\"password\":\"secret123\",\"apiKey\":\"abc-def-ghi\"}";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext, keyId)
                .flatMap(result -> 
                    adapter.decryptCredential(result.encryptedValue(), result.keyId(), result.iv())
                )
        )
        .assertNext(decrypted -> {
            assertThat(decrypted).isEqualTo(plaintext);
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt multiple credentials independently")
    void shouldEncryptMultipleCredentialsIndependently() {
        // Given
        String plaintext1 = "credential-1";
        String plaintext2 = "credential-2";
        String plaintext3 = "credential-3";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(
            adapter.encryptCredential(plaintext1, keyId)
                .zipWith(adapter.encryptCredential(plaintext2, keyId))
                .zipWith(adapter.encryptCredential(plaintext3, keyId))
        )
        .assertNext(tuple -> {
            CredentialEncryptionPort.CredentialEncryptionResult result1 = tuple.getT1().getT1();
            CredentialEncryptionPort.CredentialEncryptionResult result2 = tuple.getT1().getT2();
            CredentialEncryptionPort.CredentialEncryptionResult result3 = tuple.getT2();
            
            // All should have different IVs and encrypted values
            assertThat(result1.iv()).isNotEqualTo(result2.iv());
            assertThat(result1.iv()).isNotEqualTo(result3.iv());
            assertThat(result2.iv()).isNotEqualTo(result3.iv());
            
            assertThat(result1.encryptedValue()).isNotEqualTo(result2.encryptedValue());
            assertThat(result1.encryptedValue()).isNotEqualTo(result3.encryptedValue());
            assertThat(result2.encryptedValue()).isNotEqualTo(result3.encryptedValue());
        })
        .verifyComplete();
    }

    @Test
    @DisplayName("Should use default master key when properties have null master key ID")
    void shouldUseDefaultMasterKeyWhenPropertiesNull() {
        // Given
        properties.getEncryption().setMasterKeyId(null);
        String plaintext = "test-data";

        // When & Then
        StepVerifier.create(adapter.encryptCredential(plaintext, null))
            .assertNext(result -> {
                assertThat(result.keyId()).isEqualTo("default-master-key");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should handle Base64 encoding correctly")
    void shouldHandleBase64EncodingCorrectly() {
        // Given
        String plaintext = "test-credential";
        String keyId = "test-key";

        // When & Then
        StepVerifier.create(adapter.encryptCredential(plaintext, keyId))
            .assertNext(result -> {
                // Verify Base64 encoding (should not throw exception)
                assertThat(result.encryptedValue()).matches("^[A-Za-z0-9+/]*={0,2}$");
                assertThat(result.iv()).matches("^[A-Za-z0-9+/]*={0,2}$");
            })
            .verifyComplete();
    }
}

