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

package com.firefly.common.security.vault.core.health;

import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import com.firefly.common.security.vault.core.ports.KeyManagementPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KeyManagementHealthIndicator
 */
@ExtendWith(MockitoExtension.class)
class KeyManagementHealthIndicatorTest {

    @Mock
    private KeyManagementPort keyManagementPort;

    @Mock
    private SecurityVaultProperties properties;

    @Mock
    private SecurityVaultProperties.EncryptionConfig encryptionConfig;

    private KeyManagementHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        when(properties.getEncryption()).thenReturn(encryptionConfig);
        when(encryptionConfig.getMasterKeyId()).thenReturn("test-master-key-12345");
        when(keyManagementPort.getProviderType()).thenReturn(KeyManagementPort.ProviderType.IN_MEMORY);

        healthIndicator = new KeyManagementHealthIndicator(keyManagementPort, properties);
    }

    @Test
    void shouldReturnUpWhenKeyIsValid() {
        // Given
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getStatus()).isEqualTo(Status.UP);
                assertThat(health.getDetails()).containsEntry("provider", "IN_MEMORY");
                assertThat(health.getDetails()).containsEntry("keyValid", true);
                assertThat(health.getDetails()).containsKey("responseTimeMs");
                assertThat(health.getDetails()).containsKey("timestamp");
                assertThat(health.getDetails().get("masterKeyId")).isEqualTo("test***2345");
            })
            .verifyComplete();

        verify(keyManagementPort).validateKey("test-master-key-12345");
    }

    @Test
    void shouldReturnDownWhenKeyIsInvalid() {
        // Given
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getStatus()).isEqualTo(Status.DOWN);
                assertThat(health.getDetails()).containsEntry("provider", "IN_MEMORY");
                assertThat(health.getDetails()).containsEntry("keyValid", false);
                assertThat(health.getDetails()).containsEntry("reason", "Master key validation failed");
                assertThat(health.getDetails()).containsKey("responseTimeMs");
                assertThat(health.getDetails()).containsKey("timestamp");
            })
            .verifyComplete();

        verify(keyManagementPort).validateKey("test-master-key-12345");
    }

    @Test
    void shouldReturnDownOnError() {
        // Given
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.error(new RuntimeException("Connection failed")));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getStatus()).isEqualTo(Status.DOWN);
                assertThat(health.getDetails()).containsEntry("provider", "IN_MEMORY");
                assertThat(health.getDetails()).containsEntry("error", "RuntimeException");
                assertThat(health.getDetails()).containsEntry("message", "Connection failed");
                assertThat(health.getDetails()).containsKey("responseTimeMs");
                assertThat(health.getDetails()).containsKey("timestamp");
            })
            .verifyComplete();

        verify(keyManagementPort).validateKey("test-master-key-12345");
    }

    @Test
    void shouldReturnDownOnTimeout() {
        // Given
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.delay(Duration.ofSeconds(10)).then(Mono.just(true)));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getStatus()).isEqualTo(Status.DOWN);
                assertThat(health.getDetails()).containsEntry("provider", "IN_MEMORY");
                assertThat(health.getDetails()).containsEntry("error", "TimeoutException");
                assertThat(health.getDetails()).containsKey("responseTimeMs");
            })
            .verifyComplete();

        verify(keyManagementPort).validateKey("test-master-key-12345");
    }

    @Test
    void shouldMaskShortKeyId() {
        // Given
        when(encryptionConfig.getMasterKeyId()).thenReturn("short");
        when(keyManagementPort.validateKey("short"))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getDetails().get("masterKeyId")).isEqualTo("***");
            })
            .verifyComplete();
    }

    @Test
    void shouldMaskLongKeyId() {
        // Given
        when(encryptionConfig.getMasterKeyId()).thenReturn("very-long-master-key-id-12345");
        when(keyManagementPort.validateKey("very-long-master-key-id-12345"))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getDetails().get("masterKeyId")).isEqualTo("very***2345");
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleNullKeyId() {
        // Given
        when(encryptionConfig.getMasterKeyId()).thenReturn(null);
        when(keyManagementPort.validateKey(null))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getDetails().get("masterKeyId")).isEqualTo("***");
            })
            .verifyComplete();
    }

    @Test
    void shouldIncludeResponseTime() {
        // Given
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.delay(Duration.ofMillis(100)).then(Mono.just(true)));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                Long responseTime = (Long) health.getDetails().get("responseTimeMs");
                assertThat(responseTime).isGreaterThanOrEqualTo(100);
            })
            .verifyComplete();
    }

    @Test
    void shouldWorkWithDifferentProviderTypes() {
        // Given
        when(keyManagementPort.getProviderType()).thenReturn(KeyManagementPort.ProviderType.AWS_KMS);
        when(keyManagementPort.validateKey("test-master-key-12345"))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(healthIndicator.health())
            .assertNext(health -> {
                assertThat(health.getStatus()).isEqualTo(Status.UP);
                assertThat(health.getDetails()).containsEntry("provider", "AWS_KMS");
            })
            .verifyComplete();
    }
}

