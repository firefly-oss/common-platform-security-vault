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

package com.firefly.common.security.vault.core.services.impl;

import com.firefly.common.core.filters.FilterRequest;
import com.firefly.common.core.queries.PaginationResponse;
import com.firefly.common.security.vault.core.mappers.CredentialMapper;
import com.firefly.common.security.vault.core.ports.CredentialEncryptionPort;
import com.firefly.common.security.vault.core.services.access.AccessControlService;
import com.firefly.common.security.vault.core.services.audit.CredentialAuditService;
import com.firefly.common.security.vault.interfaces.dtos.CredentialDTO;
import com.firefly.common.security.vault.models.entities.Credential;
import com.firefly.common.security.vault.models.repositories.CredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CredentialServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private CredentialMapper credentialMapper;

    @Mock
    private CredentialEncryptionPort credentialEncryptionPort;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private CredentialAuditService auditService;

    private CredentialServiceImpl credentialService;

    private UUID testId;
    private Credential testCredential;
    private CredentialDTO testCredentialDTO;

    @BeforeEach
    void setUp() {
        credentialService = new CredentialServiceImpl(
            credentialRepository,
            credentialMapper,
            credentialEncryptionPort,
            accessControlService,
            auditService
        );

        testId = UUID.randomUUID();
        
        testCredential = new Credential();
        testCredential.setId(testId);
        testCredential.setCode("TEST_CREDENTIAL");
        testCredential.setName("Test Credential");
        testCredential.setEncryptedValue("encrypted-value");
        testCredential.setEncryptionKeyId(UUID.randomUUID().toString());
        testCredential.setActive(true);
        testCredential.setCreatedAt(LocalDateTime.now());
        testCredential.setUpdatedAt(LocalDateTime.now());
        testCredential.setUsageCount(0L);

        testCredentialDTO = new CredentialDTO();
        testCredentialDTO.setId(testId);
        testCredentialDTO.setCode("TEST_CREDENTIAL");
        testCredentialDTO.setName("Test Credential");
        testCredentialDTO.setEncryptedValue("encrypted-value");
        testCredentialDTO.setEncryptionKeyId(UUID.randomUUID().toString());
    }

    @Test
    void shouldGetCredentialById() {
        // Given
        when(credentialRepository.findById(testId)).thenReturn(Mono.just(testCredential));
        when(credentialMapper.toDTO(testCredential)).thenReturn(testCredentialDTO);
        when(auditService.logAccess(any(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.getById(testId))
            .assertNext(dto -> {
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(testId);
                assertThat(dto.getCode()).isEqualTo("TEST_CREDENTIAL");
            })
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verify(credentialMapper).toDTO(testCredential);
        verify(auditService).logAccess(any(), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void shouldHandleNotFoundWhenGettingById() {
        // Given
        when(credentialRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.getById(testId))
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verifyNoInteractions(credentialMapper);
    }

    @Test
    void shouldLogFailedAccessOnError() {
        // Given
        RuntimeException error = new RuntimeException("Database error");
        when(credentialRepository.findById(testId)).thenReturn(Mono.error(error));
        when(auditService.logFailedAccess(any(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.getById(testId))
            .expectError(RuntimeException.class)
            .verify();

        verify(credentialRepository).findById(testId);
    }

    @Test
    void shouldFilterCredentials() {
        // Given
        Credential credential1 = new Credential();
        credential1.setId(UUID.randomUUID());
        credential1.setActive(true);

        Credential credential2 = new Credential();
        credential2.setId(UUID.randomUUID());
        credential2.setActive(true);

        CredentialDTO dto1 = new CredentialDTO();
        dto1.setId(credential1.getId());

        CredentialDTO dto2 = new CredentialDTO();
        dto2.setId(credential2.getId());

        when(credentialRepository.findAll()).thenReturn(Flux.just(credential1, credential2));
        when(credentialMapper.toDTO(credential1)).thenReturn(dto1);
        when(credentialMapper.toDTO(credential2)).thenReturn(dto2);

        FilterRequest<CredentialDTO> filterRequest = new FilterRequest<>();

        // When & Then
        StepVerifier.create(credentialService.filter(filterRequest))
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response.getContent()).hasSize(2);
                assertThat(response.getTotalElements()).isEqualTo(2);
            })
            .verifyComplete();

        verify(credentialRepository).findAll();
    }

    @Test
    void shouldCreateCredential() {
        // Given
        CredentialEncryptionPort.CredentialEncryptionResult encryptionResult =
            new CredentialEncryptionPort.CredentialEncryptionResult(
                "encrypted-value",
                "iv-value",
                "AES-256-GCM",
                UUID.randomUUID().toString()
            );

        when(credentialMapper.toEntity(testCredentialDTO)).thenReturn(testCredential);
        when(credentialEncryptionPort.encryptCredential(anyString(), anyString()))
            .thenReturn(Mono.just(encryptionResult));
        when(credentialRepository.save(any(Credential.class))).thenReturn(Mono.just(testCredential));
        when(credentialMapper.toDTO(any(Credential.class))).thenReturn(testCredentialDTO);
        when(auditService.logCredentialCreation(any(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.create(testCredentialDTO))
            .assertNext(dto -> {
                assertThat(dto).isNotNull();
                assertThat(dto.getCode()).isEqualTo("TEST_CREDENTIAL");
            })
            .verifyComplete();

        verify(credentialMapper).toEntity(testCredentialDTO);
        verify(credentialEncryptionPort).encryptCredential(anyString(), anyString());
        verify(credentialRepository).save(any(Credential.class));
        verify(auditService).logCredentialCreation(any(), anyString(), anyString());
    }

    @Test
    void shouldUpdateCredential() {
        // Given
        CredentialDTO updateDTO = new CredentialDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDescription("Updated Description");

        Credential updatedCredential = new Credential();
        updatedCredential.setId(testId);
        updatedCredential.setName("Updated Name");
        updatedCredential.setDescription("Updated Description");

        when(credentialRepository.findById(testId)).thenReturn(Mono.just(testCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(Mono.just(updatedCredential));
        when(credentialMapper.toDTO(any(Credential.class))).thenReturn(updateDTO);
        when(auditService.logCredentialUpdate(any(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.update(testId, updateDTO))
            .assertNext(dto -> {
                assertThat(dto).isNotNull();
                assertThat(dto.getName()).isEqualTo("Updated Name");
            })
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verify(credentialRepository).save(any(Credential.class));
        verify(auditService).logCredentialUpdate(any(), anyString(), anyString());
    }

    @Test
    void shouldDeleteCredential() {
        // Given
        when(credentialRepository.findById(testId)).thenReturn(Mono.just(testCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(Mono.just(testCredential));
        when(auditService.logCredentialDeletion(any(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.delete(testId))
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verify(credentialRepository).save(any(Credential.class));
        verify(auditService).logCredentialDeletion(any(), anyString(), anyString());
    }

    @Test
    void shouldHandleNotFoundWhenDeleting() {
        // Given
        when(credentialRepository.findById(testId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.delete(testId))
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verify(credentialRepository, never()).save(any());
    }

    @Test
    void shouldDecryptCredentialValue() {
        // Given
        String decryptedValue = "decrypted-secret";
        AccessControlService.AccessRequest accessRequest = new AccessControlService.AccessRequest(
            "test-user", "test-service", "127.0.0.1", "test-env", false, "test"
        );
        AccessControlService.AccessDecision accessDecision = new AccessControlService.AccessDecision(
            true, null
        );

        when(credentialRepository.findById(testId)).thenReturn(Mono.just(testCredential));
        when(accessControlService.validateAccess(any(), any())).thenReturn(Mono.just(accessDecision));
        when(credentialEncryptionPort.decryptCredential(eq("encrypted-value"), anyString(), any()))
            .thenReturn(Mono.just(decryptedValue));
        when(credentialRepository.save(any())).thenReturn(Mono.just(testCredential));
        when(auditService.logDecryption(any(), anyString(), anyString(), anyString(), anyBoolean(), anyLong()))
            .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(credentialService.getDecryptedValue(testId, accessRequest))
            .assertNext(value -> {
                assertThat(value).isEqualTo(decryptedValue);
            })
            .verifyComplete();

        verify(credentialRepository).findById(testId);
        verify(accessControlService).validateAccess(any(), any());
        verify(credentialEncryptionPort).decryptCredential(eq("encrypted-value"), anyString(), any());
        verify(auditService).logDecryption(any(), anyString(), anyString(), anyString(), anyBoolean(), anyLong());
    }
}

