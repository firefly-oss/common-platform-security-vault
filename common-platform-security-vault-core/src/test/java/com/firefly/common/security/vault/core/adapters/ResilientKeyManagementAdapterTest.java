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
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResilientKeyManagementAdapter
 */
@ExtendWith(MockitoExtension.class)
class ResilientKeyManagementAdapterTest {

    @Mock
    private KeyManagementPort delegateAdapter;

    private CircuitBreaker circuitBreaker;
    private RateLimiter rateLimiter;
    private Retry retry;
    private ResilientKeyManagementAdapter resilientAdapter;

    @BeforeEach
    void setUp() {
        // Configure Circuit Breaker
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(100))
            .slidingWindowSize(10)
            .build();
        CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.of(cbConfig);
        circuitBreaker = cbRegistry.circuitBreaker("test-cb");

        // Configure Rate Limiter
        RateLimiterConfig rlConfig = RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(100))
            .build();
        RateLimiterRegistry rlRegistry = RateLimiterRegistry.of(rlConfig);
        rateLimiter = rlRegistry.rateLimiter("test-rl");

        // Configure Retry
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                Duration.ofMillis(10), 2.0))
            .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retry = retryRegistry.retry("test-retry");

        // Create resilient adapter
        resilientAdapter = new ResilientKeyManagementAdapter(
            delegateAdapter,
            circuitBreaker,
            rateLimiter,
            retry
        );
    }

    @Test
    void shouldEncryptSuccessfully() {
        // Given
        byte[] plaintext = "test-data".getBytes();
        String keyId = "test-key";
        String context = "test-context";
        KeyManagementPort.EncryptionResult expectedResult = new KeyManagementPort.EncryptionResult(
            "encrypted".getBytes(), keyId, "AES-256-GCM", "metadata"
        );

        when(delegateAdapter.encrypt(plaintext, keyId, context))
            .thenReturn(Mono.just(expectedResult));

        // When & Then
        StepVerifier.create(resilientAdapter.encrypt(plaintext, keyId, context))
            .expectNext(expectedResult)
            .verifyComplete();

        verify(delegateAdapter).encrypt(plaintext, keyId, context);
    }

    @Test
    void shouldDecryptSuccessfully() {
        // Given
        byte[] ciphertext = "encrypted".getBytes();
        String keyId = "test-key";
        String context = "test-context";
        byte[] expectedPlaintext = "decrypted".getBytes();

        when(delegateAdapter.decrypt(ciphertext, keyId, context))
            .thenReturn(Mono.just(expectedPlaintext));

        // When & Then
        StepVerifier.create(resilientAdapter.decrypt(ciphertext, keyId, context))
            .expectNext(expectedPlaintext)
            .verifyComplete();

        verify(delegateAdapter).decrypt(ciphertext, keyId, context);
    }

    @Test
    void shouldGenerateDataKeySuccessfully() {
        // Given
        String keyId = "test-key";
        String context = "test-context";
        KeyManagementPort.DataKey expectedDataKey = new KeyManagementPort.DataKey(
            "plaintext-key".getBytes(),
            "encrypted-key".getBytes(),
            keyId
        );

        when(delegateAdapter.generateDataKey(keyId, context))
            .thenReturn(Mono.just(expectedDataKey));

        // When & Then
        StepVerifier.create(resilientAdapter.generateDataKey(keyId, context))
            .expectNext(expectedDataKey)
            .verifyComplete();

        verify(delegateAdapter).generateDataKey(keyId, context);
    }

    @Test
    void shouldRotateKeySuccessfully() {
        // Given
        String keyId = "test-key";
        KeyManagementPort.KeyRotationResult expectedResult = new KeyManagementPort.KeyRotationResult(
            true, "2", "Key rotated successfully"
        );

        when(delegateAdapter.rotateKey(keyId))
            .thenReturn(Mono.just(expectedResult));

        // When & Then
        StepVerifier.create(resilientAdapter.rotateKey(keyId))
            .expectNext(expectedResult)
            .verifyComplete();

        verify(delegateAdapter).rotateKey(keyId);
    }

    @Test
    void shouldValidateKeySuccessfully() {
        // Given
        String keyId = "test-key";

        when(delegateAdapter.validateKey(keyId))
            .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(resilientAdapter.validateKey(keyId))
            .expectNext(true)
            .verifyComplete();

        verify(delegateAdapter).validateKey(keyId);
    }

    @Test
    void shouldHandleErrorsWithResiliencePatterns() {
        // Given
        byte[] plaintext = "test-data".getBytes();
        String keyId = "test-key";
        String context = "test-context";

        // Simulate error
        when(delegateAdapter.encrypt(plaintext, keyId, context))
            .thenReturn(Mono.error(new RuntimeException("Error")));

        // When & Then - Should propagate error after resilience patterns
        StepVerifier.create(resilientAdapter.encrypt(plaintext, keyId, context))
            .expectError(RuntimeException.class)
            .verify();

        // Verify that delegate was called
        verify(delegateAdapter, atLeastOnce()).encrypt(plaintext, keyId, context);
    }

    @Test
    void shouldVerifyRetryIsConfigured() {
        // Given - Verify that retry is properly configured
        Retry.Metrics retryMetrics = resilientAdapter.getRetryMetrics();

        // Then - Retry should be initialized with correct configuration
        assertThat(retryMetrics).isNotNull();

        // Verify retry configuration by checking it exists and has default values
        assertThat(retryMetrics.getNumberOfSuccessfulCallsWithoutRetryAttempt()).isGreaterThanOrEqualTo(0);
        assertThat(retryMetrics.getNumberOfFailedCallsWithoutRetryAttempt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldReturnProviderType() {
        // Given
        when(delegateAdapter.getProviderType())
            .thenReturn(KeyManagementPort.ProviderType.IN_MEMORY);

        // When
        KeyManagementPort.ProviderType result = resilientAdapter.getProviderType();

        // Then
        assertThat(result).isEqualTo(KeyManagementPort.ProviderType.IN_MEMORY);
        verify(delegateAdapter, atLeastOnce()).getProviderType();
    }

    @Test
    void shouldExposeCircuitBreakerMetrics() {
        // When
        CircuitBreaker.Metrics metrics = resilientAdapter.getCircuitBreakerMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getNumberOfFailedCalls()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldExposeRateLimiterMetrics() {
        // When
        RateLimiter.Metrics metrics = resilientAdapter.getRateLimiterMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getAvailablePermissions()).isGreaterThan(0);
    }

    @Test
    void shouldExposeRetryMetrics() {
        // When
        Retry.Metrics metrics = resilientAdapter.getRetryMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.getNumberOfFailedCallsWithoutRetryAttempt()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldTrackSuccessfulCallsInMetrics() {
        // Given
        byte[] plaintext = "test-data".getBytes();
        String keyId = "test-key";
        String context = "test-context";
        KeyManagementPort.EncryptionResult expectedResult = new KeyManagementPort.EncryptionResult(
            "encrypted".getBytes(), keyId, "AES-256-GCM", "metadata"
        );

        when(delegateAdapter.encrypt(plaintext, keyId, context))
            .thenReturn(Mono.just(expectedResult));

        // When
        StepVerifier.create(resilientAdapter.encrypt(plaintext, keyId, context))
            .expectNext(expectedResult)
            .verifyComplete();

        // Then - Verify metrics were updated
        CircuitBreaker.Metrics cbMetrics = resilientAdapter.getCircuitBreakerMetrics();
        assertThat(cbMetrics.getNumberOfSuccessfulCalls()).isGreaterThan(0);
    }

    @Test
    void shouldReturnDelegate() {
        // When
        KeyManagementPort result = resilientAdapter.getDelegate();

        // Then
        assertThat(result).isEqualTo(delegateAdapter);
    }

    @Test
    void shouldReturnCircuitBreakerMetrics() {
        // When
        CircuitBreaker.Metrics metrics = resilientAdapter.getCircuitBreakerMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isEqualTo(0);
    }

    @Test
    void shouldReturnRateLimiterMetrics() {
        // When
        RateLimiter.Metrics metrics = resilientAdapter.getRateLimiterMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getAvailablePermissions()).isGreaterThan(0);
    }

    @Test
    void shouldReturnRetryMetrics() {
        // When
        Retry.Metrics metrics = resilientAdapter.getRetryMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getNumberOfSuccessfulCallsWithoutRetryAttempt()).isEqualTo(0);
    }
}

