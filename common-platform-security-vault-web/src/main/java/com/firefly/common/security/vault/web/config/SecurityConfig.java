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


package com.firefly.common.security.vault.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

/**
 * Security configuration for the Security Vault microservice
 * 
 * NOTE: Authentication and authorization are managed by Istio service mesh.
 * This configuration only handles:
 * - Security headers (XSS, Clickjacking protection, HSTS)
 * - Password encoding for internal use
 * 
 * Istio handles:
 * - mTLS between services
 * - JWT validation
 * - Authorization policies
 * - Rate limiting
 * - CORS policies
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Minimal security configuration - Istio handles authentication/authorization
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF - not needed for service-to-service communication
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // Permit all requests - Istio handles authorization
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            )
            
            // Configure security headers for defense in depth
            .headers(headers -> headers
                // Prevent clickjacking
                .frameOptions(frameOptions -> frameOptions
                    .mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                // Enable XSS protection
                .xssProtection(xss -> {})
                // Disable content type sniffing
                .contentTypeOptions(contentType -> {})
                // Enable HSTS (HTTP Strict Transport Security)
                .hsts(hsts -> hsts
                    .maxAge(java.time.Duration.ofDays(365))
                    .includeSubdomains(true)
                    .preload(true))
            )
            
            // Build the security filter chain
            .build();
    }

    /**
     * Password encoder for credential hashing (internal use)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 for secure password hashing
        return new BCryptPasswordEncoder(12);
    }
}
