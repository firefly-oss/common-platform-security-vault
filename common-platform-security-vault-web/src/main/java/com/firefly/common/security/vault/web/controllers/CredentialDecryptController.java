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


package com.firefly.common.security.vault.web.controllers;

import com.firefly.common.security.vault.core.services.access.AccessControlService;
import com.firefly.common.security.vault.core.services.impl.CredentialServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Separate controller for sensitive decrypt operations
 */
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
@Tag(name = "Credential Decryption", description = "API for decrypting secure credentials")
public class CredentialDecryptController {

    private final CredentialServiceImpl credentialService;

    /**
     * POST /api/v1/credentials/:id/decrypt : Decrypt and retrieve credential value
     *
     * @param id the ID of the credential to decrypt
     * @param exchange the server web exchange to extract request context
     * @return the ResponseEntity with status 200 (OK) and the decrypted value
     */
    @PostMapping("/{id}/decrypt")
    @Operation(
            operationId = "decryptCredential",
            summary = "Decrypt a credential",
            description = "Decrypts and returns the credential value with full access control and audit logging",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful decryption"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "Credential not found")
            }
    )
    public Mono<ResponseEntity<String>> decrypt(
            @Parameter(description = "ID of the credential to decrypt", required = true)
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            ServerWebExchange exchange) {
        
        // Extract request context for access control
        String userId = extractUserId(exchange);
        String serviceName = extractServiceName(exchange);
        String ipAddress = extractIpAddress(exchange);
        
        // Build access request
        AccessControlService.AccessRequest accessRequest = new AccessControlService.AccessRequest(
            userId,
            serviceName,
            ipAddress,
            "production", // environment - should be extracted from context or config
            false, // hasApproval - should be validated if needed
            reason
        );
        
        return credentialService.getDecryptedValue(id, accessRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(SecurityException.class, e -> 
                    Mono.just(ResponseEntity.status(403).body(e.getMessage())));
    }

    /**
     * Extract user ID from Istio headers or JWT claims
     */
    private String extractUserId(ServerWebExchange exchange) {
        // Istio forwards user identity in headers
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId == null) {
            userId = exchange.getRequest().getHeaders().getFirst("X-Forwarded-User");
        }
        return userId != null ? userId : "anonymous";
    }

    /**
     * Extract service name from Istio headers
     */
    private String extractServiceName(ServerWebExchange exchange) {
        // Istio provides source service information
        String serviceName = exchange.getRequest().getHeaders().getFirst("X-Source-Service");
        if (serviceName == null) {
            serviceName = exchange.getRequest().getHeaders().getFirst("X-Forwarded-Service");
        }
        return serviceName != null ? serviceName : "unknown";
    }

    /**
     * Extract real client IP address
     */
    private String extractIpAddress(ServerWebExchange exchange) {
        // Check X-Forwarded-For header first (set by Istio/proxy)
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xff.split(",")[0].trim();
        }
        
        // Fallback to remote address
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
}
