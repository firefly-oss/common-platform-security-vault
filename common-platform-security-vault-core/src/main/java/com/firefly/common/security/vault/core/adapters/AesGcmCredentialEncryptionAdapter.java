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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-GCM adapter for Credential Encryption
 * 
 * This adapter implements the CredentialEncryptionPort using AES-256-GCM
 * encryption. It delegates key management operations to the configured
 * KeyManagementPort adapter (InMemory, AWS KMS, Azure Key Vault, etc.)
 * 
 * This is the hexagonal architecture adapter that bridges the application
 * core with the encryption infrastructure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AesGcmCredentialEncryptionAdapter implements CredentialEncryptionPort {

    private final KeyManagementPort keyManagementPort;
    private final SecurityVaultProperties properties;

    @Override
    public Mono<CredentialEncryptionResult> encryptCredential(String plaintext, String keyId) {
        String effectiveKeyId = keyId != null ? keyId : getDefaultKeyId();
        
        return Mono.fromCallable(() -> plaintext.getBytes(StandardCharsets.UTF_8))
            .flatMap(plaintextBytes -> 
                keyManagementPort.encrypt(plaintextBytes, effectiveKeyId, "credential")
            )
            .map(result -> {
                // Extract IV from the combined ciphertext (first 12 bytes)
                byte[] combined = result.ciphertext();
                byte[] iv = new byte[12];
                byte[] ciphertext = new byte[combined.length - 12];
                System.arraycopy(combined, 0, iv, 0, 12);
                System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);
                
                String encryptedValue = Base64.getEncoder().encodeToString(ciphertext);
                String ivBase64 = Base64.getEncoder().encodeToString(iv);
                
                log.debug("Successfully encrypted credential with keyId: {}", effectiveKeyId);
                
                return new CredentialEncryptionResult(
                    encryptedValue,
                    ivBase64,
                    result.algorithm(),
                    effectiveKeyId
                );
            })
            .doOnError(e -> log.error("Failed to encrypt credential with keyId: {}", effectiveKeyId, e));
    }

    @Override
    public Mono<String> decryptCredential(String encryptedValue, String keyId, String iv) {
        return Mono.fromCallable(() -> {
            byte[] ciphertext = Base64.getDecoder().decode(encryptedValue);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            
            // Combine IV + ciphertext as expected by KeyManagementPort
            byte[] combined = new byte[ivBytes.length + ciphertext.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(ciphertext, 0, combined, ivBytes.length, ciphertext.length);
            
            return combined;
        })
        .flatMap(combined -> keyManagementPort.decrypt(combined, keyId, "credential"))
        .map(plaintextBytes -> new String(plaintextBytes, StandardCharsets.UTF_8))
        .doOnSuccess(plaintext -> log.debug("Successfully decrypted credential with keyId: {}", keyId))
        .doOnError(e -> log.error("Failed to decrypt credential with keyId: {}", keyId, e));
    }

    @Override
    public Mono<CredentialEncryptionResult> rotateCredentialEncryption(
            String encryptedValue, 
            String currentKeyId, 
            String currentIv, 
            String newKeyId) {
        
        return decryptCredential(encryptedValue, currentKeyId, currentIv)
            .flatMap(plaintext -> encryptCredential(plaintext, newKeyId))
            .doOnSuccess(result -> 
                log.info("Successfully rotated credential encryption from {} to {}", 
                    currentKeyId, newKeyId)
            )
            .doOnError(e -> 
                log.error("Failed to rotate credential encryption from {} to {}", 
                    currentKeyId, newKeyId, e)
            );
    }

    @Override
    public Mono<KeyGenerationResult> generateEncryptionKey(String keyId) {
        return keyManagementPort.generateDataKey(keyId, "AES_256")
            .map(dataKey -> new KeyGenerationResult(
                keyId,
                "AES-256-GCM",
                keyManagementPort.getProviderType().name(),
                true
            ))
            .doOnSuccess(result -> log.info("Generated encryption key: {}", keyId))
            .doOnError(e -> log.error("Failed to generate encryption key: {}", keyId, e))
            .onErrorReturn(new KeyGenerationResult(
                keyId,
                "AES-256-GCM",
                keyManagementPort.getProviderType().name(),
                false
            ));
    }

    @Override
    public Mono<Boolean> validateEncryptionKey(String keyId) {
        return keyManagementPort.validateKey(keyId)
            .doOnSuccess(valid -> log.debug("Key validation for {}: {}", keyId, valid))
            .doOnError(e -> log.error("Failed to validate key: {}", keyId, e))
            .onErrorReturn(false);
    }

    @Override
    public String getProviderType() {
        return keyManagementPort.getProviderType().name();
    }

    /**
     * Get the default key ID from configuration
     */
    private String getDefaultKeyId() {
        String masterKeyId = properties.getEncryption().getMasterKeyId();
        return masterKeyId != null ? masterKeyId : "default-master-key";
    }
}

