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

package com.firefly.common.security.vault.core.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j configuration for Security Vault
 * 
 * Provides:
 * - Circuit Breaker: Prevents cascading failures when KMS is down
 * - Rate Limiter: Protects against excessive KMS API calls
 * - Retry: Automatic retry with exponential backoff for transient failures
 * 
 * @author Firefly Security Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ResilienceConfiguration {

    /**
     * Circuit Breaker for KMS operations
     *
     * Configuration:
     * - Failure rate threshold: 50% (opens circuit if 50% of calls fail)
     * - Wait duration in open state: 60 seconds
     * - Sliding window size: 10 calls
     * - Minimum number of calls: 5 (before calculating failure rate)
     * - Permitted calls in half-open state: 3
     */
    @Bean
    @Qualifier("kmsCircuitBreakerRegistry")
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50.0f)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(
                RuntimeException.class,
                Exception.class
            )
            .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        // Register event listeners for monitoring
        registry.circuitBreaker("kms-operations").getEventPublisher()
            .onStateTransition(event ->
                log.warn("Circuit Breaker state transition: {} -> {}",
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState())
            )
            .onError(event ->
                log.error("Circuit Breaker error: {}", event.getThrowable().getMessage())
            );

        log.info("Circuit Breaker configured for KMS operations");
        return registry;
    }

    /**
     * Get the KMS operations circuit breaker
     */
    @Bean
    public CircuitBreaker kmsCircuitBreaker(@Qualifier("kmsCircuitBreakerRegistry") CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("kms-operations");
    }

    /**
     * Rate Limiter for KMS operations
     *
     * Configuration:
     * - Limit: 100 calls per second
     * - Timeout: 5 seconds (wait time for permission)
     *
     * This prevents excessive API calls to KMS providers which may have
     * rate limits and cost implications.
     */
    @Bean
    @Qualifier("kmsRateLimiterRegistry")
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build();
        
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        
        // Register event listeners for monitoring
        registry.rateLimiter("kms-operations").getEventPublisher()
            .onSuccess(event ->
                log.debug("Rate limiter: Call successful")
            )
            .onFailure(event ->
                log.warn("Rate limiter: Call rejected - too many requests")
            );

        log.info("Rate Limiter configured: 100 calls/second for KMS operations");
        return registry;
    }

    /**
     * Get the KMS operations rate limiter
     */
    @Bean
    public RateLimiter kmsRateLimiter(@Qualifier("kmsRateLimiterRegistry") RateLimiterRegistry registry) {
        return registry.rateLimiter("kms-operations");
    }

    /**
     * Retry configuration for KMS operations
     *
     * Configuration:
     * - Max attempts: 3
     * - Wait duration: 1 second (exponential backoff)
     * - Exponential backoff multiplier: 2
     * - Retry on: RuntimeException, Exception
     *
     * This handles transient failures like network issues or temporary
     * KMS unavailability.
     */
    @Bean
    @Qualifier("kmsRetryRegistry")
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                Duration.ofSeconds(1), 2.0))
            .retryExceptions(
                RuntimeException.class,
                Exception.class
            )
            .build();
        
        RetryRegistry registry = RetryRegistry.of(config);
        
        // Register event listeners for monitoring
        registry.retry("kms-operations").getEventPublisher()
            .onRetry(event ->
                log.warn("Retry attempt {} for KMS operation: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage())
            )
            .onSuccess(event ->
                log.debug("Retry successful after {} attempts",
                    event.getNumberOfRetryAttempts())
            )
            .onError(event ->
                log.error("Retry failed after {} attempts: {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage())
            );

        log.info("Retry configured: 3 attempts with exponential backoff for KMS operations");
        return registry;
    }

    /**
     * Get the KMS operations retry
     */
    @Bean
    public Retry kmsRetry(@Qualifier("kmsRetryRegistry") RetryRegistry registry) {
        return registry.retry("kms-operations");
    }
}

