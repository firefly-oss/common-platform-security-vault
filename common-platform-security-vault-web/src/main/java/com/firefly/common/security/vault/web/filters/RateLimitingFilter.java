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


package com.firefly.common.security.vault.web.filters;

import com.firefly.common.security.vault.core.config.SecurityVaultProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter for Security Vault API
 * 
 * Implements a simple token bucket algorithm per IP address
 * For production, consider using Redis-based rate limiting for distributed systems
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "firefly.security.vault.access-control",
    name = "enable-rate-limiting",
    havingValue = "true"
)
public class RateLimitingFilter implements WebFilter {

    private final SecurityVaultProperties properties;
    
    // In-memory rate limit tracking (use Redis for production distributed systems)
    private final Map<String, RateLimitBucket> rateLimitBuckets = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Skip rate limiting for health checks and actuator endpoints
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator") || path.equals("/health")) {
            return chain.filter(exchange);
        }
        
        String clientId = getClientIdentifier(exchange);
        int rateLimit = properties.getAccessControl().getRateLimitPerMinute();
        
        RateLimitBucket bucket = rateLimitBuckets.computeIfAbsent(
            clientId, 
            k -> new RateLimitBucket(rateLimit)
        );
        
        if (bucket.tryConsume()) {
            // Add rate limit headers
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(rateLimit));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));
            
            return chain.filter(exchange);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
            
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(rateLimit));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
            exchange.getResponse().getHeaders().add("X-RateLimit-Reset", String.valueOf(bucket.getResetTime()));
            exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(bucket.getRetryAfterSeconds()));
            
            return exchange.getResponse().setComplete();
        }
    }
    
    /**
     * Get client identifier (IP address or authenticated user)
     */
    private String getClientIdentifier(ServerWebExchange exchange) {
        // Try to get from X-Forwarded-For header (if behind proxy)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        // Try to get from X-Real-IP header
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        
        // Fall back to remote address
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
    
    /**
     * Simple token bucket implementation
     * For production, use Bucket4j with Redis or similar distributed solution
     */
    private static class RateLimitBucket {
        private final int capacity;
        private final AtomicInteger tokens;
        private volatile Instant lastRefill;
        private final Duration refillInterval = Duration.ofMinutes(1);
        
        public RateLimitBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefill = Instant.now();
        }
        
        public synchronized boolean tryConsume() {
            refillIfNeeded();
            
            int currentTokens = tokens.get();
            if (currentTokens > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
        
        public int getRemaining() {
            refillIfNeeded();
            return Math.max(0, tokens.get());
        }
        
        public long getResetTime() {
            return lastRefill.plus(refillInterval).getEpochSecond();
        }
        
        public long getRetryAfterSeconds() {
            Instant resetTime = lastRefill.plus(refillInterval);
            return Math.max(0, Duration.between(Instant.now(), resetTime).getSeconds());
        }
        
        private void refillIfNeeded() {
            Instant now = Instant.now();
            if (Duration.between(lastRefill, now).compareTo(refillInterval) >= 0) {
                tokens.set(capacity);
                lastRefill = now;
            }
        }
    }
}

