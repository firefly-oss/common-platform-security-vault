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
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Decorator that adds resilience patterns to KeyManagementPort implementations
 * 
 * This decorator wraps any KeyManagementPort implementation and adds:
 * - Circuit Breaker: Prevents cascading failures
 * - Rate Limiter: Protects against excessive API calls
 * - Retry: Automatic retry with exponential backoff
 * 
 * Usage:
 * <pre>
 * KeyManagementPort resilientAdapter = new ResilientKeyManagementAdapter(
 *     originalAdapter,
 *     circuitBreaker,
 *     rateLimiter,
 *     retry
 * );
 * </pre>
 * 
 * @author Firefly Security Team
 * @since 1.0.0
 */
@Slf4j
public class ResilientKeyManagementAdapter implements KeyManagementPort {

    private final KeyManagementPort delegate;
    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    public ResilientKeyManagementAdapter(
            KeyManagementPort delegate,
            CircuitBreaker circuitBreaker,
            RateLimiter rateLimiter,
            Retry retry) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
        this.retry = retry;

        log.info("Resilient Key Management Adapter initialized for provider: {}",
            delegate.getProviderType());
    }

    @Override
    public Mono<EncryptionResult> encrypt(byte[] plaintext, String keyId, String encryptionContext) {
        return delegate.encrypt(plaintext, keyId, encryptionContext)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError(error ->
                log.error("Encryption failed after resilience patterns applied: {}",
                    error.getMessage())
            );
    }

    @Override
    public Mono<byte[]> decrypt(byte[] ciphertext, String keyId, String encryptionContext) {
        return delegate.decrypt(ciphertext, keyId, encryptionContext)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError(error ->
                log.error("Decryption failed after resilience patterns applied: {}",
                    error.getMessage())
            );
    }

    @Override
    public Mono<DataKey> generateDataKey(String keyId, String encryptionContext) {
        return delegate.generateDataKey(keyId, encryptionContext)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError(error ->
                log.error("Data key generation failed after resilience patterns applied: {}",
                    error.getMessage())
            );
    }

    @Override
    public Mono<KeyRotationResult> rotateKey(String keyId) {
        return delegate.rotateKey(keyId)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError(error ->
                log.error("Key rotation failed after resilience patterns applied: {}",
                    error.getMessage())
            );
    }

    @Override
    public Mono<Boolean> validateKey(String keyId) {
        return delegate.validateKey(keyId)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError(error ->
                log.error("Key validation failed after resilience patterns applied: {}",
                    error.getMessage())
            );
    }

    @Override
    public ProviderType getProviderType() {
        return delegate.getProviderType();
    }

    /**
     * Get the underlying delegate adapter
     */
    public KeyManagementPort getDelegate() {
        return delegate;
    }

    /**
     * Get circuit breaker metrics
     */
    public CircuitBreaker.Metrics getCircuitBreakerMetrics() {
        return circuitBreaker.getMetrics();
    }

    /**
     * Get rate limiter metrics
     */
    public RateLimiter.Metrics getRateLimiterMetrics() {
        return rateLimiter.getMetrics();
    }

    /**
     * Get retry metrics
     */
    public Retry.Metrics getRetryMetrics() {
        return retry.getMetrics();
    }
}

