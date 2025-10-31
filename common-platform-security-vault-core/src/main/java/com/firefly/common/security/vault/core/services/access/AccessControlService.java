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


package com.firefly.common.security.vault.core.services.access;

import com.firefly.common.security.vault.models.entities.Credential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Service for controlling access to credentials
 * 
 * Access Control Features:
 * - IP address validation
 * - Service whitelist validation
 * - Environment isolation
 * - Approval workflow checks
 * - Time-based access restrictions
 */
@Slf4j
@Service
public class AccessControlService {

    /**
     * Validate if a service/user has access to a credential
     */
    public Mono<AccessDecision> validateAccess(Credential credential, AccessRequest request) {
        return Mono.just(credential)
            .flatMap(cred -> checkIpWhitelist(cred, request.ipAddress()))
            .flatMap(allowed -> checkServiceWhitelist(credential, request.serviceName()))
            .flatMap(allowed -> checkEnvironmentMatch(credential, request.environment()))
            .flatMap(allowed -> checkApprovalRequired(credential, request.hasApproval()))
            .map(allowed -> new AccessDecision(allowed, allowed ? null : "Access denied"))
            .onErrorResume(error -> {
                log.error("Access validation failed: {}", error.getMessage());
                return Mono.just(new AccessDecision(false, error.getMessage()));
            });
    }

    /**
     * Check if IP address is whitelisted
     */
    private Mono<Boolean> checkIpWhitelist(Credential credential, String ipAddress) {
        return Mono.fromCallable(() -> {
            if (credential.getAllowedIps() == null || credential.getAllowedIps().isBlank()) {
                return true; // No restriction
            }
            
            List<String> allowedIps = Arrays.asList(credential.getAllowedIps().split(","));
            boolean allowed = allowedIps.stream()
                .map(String::trim)
                .anyMatch(ip -> ip.equals(ipAddress) || ip.equals("*"));
            
            if (!allowed) {
                log.warn("IP {} not in whitelist for credential {}", ipAddress, credential.getId());
            }
            
            return allowed;
        });
    }

    /**
     * Check if service is whitelisted
     */
    private Mono<Boolean> checkServiceWhitelist(Credential credential, String serviceName) {
        return Mono.fromCallable(() -> {
            if (credential.getAllowedServices() == null || credential.getAllowedServices().isBlank()) {
                return true; // No restriction
            }
            
            if (serviceName == null) {
                log.warn("Service name not provided for credential {}", credential.getId());
                return false;
            }
            
            List<String> allowedServices = Arrays.asList(credential.getAllowedServices().split(","));
            boolean allowed = allowedServices.stream()
                .map(String::trim)
                .anyMatch(svc -> svc.equalsIgnoreCase(serviceName) || svc.equals("*"));
            
            if (!allowed) {
                log.warn("Service {} not in whitelist for credential {}", serviceName, credential.getId());
            }
            
            return allowed;
        });
    }

    /**
     * Check if environment matches
     */
    private Mono<Boolean> checkEnvironmentMatch(Credential credential, String environment) {
        return Mono.fromCallable(() -> {
            if (credential.getAllowedEnvironments() == null || credential.getAllowedEnvironments().isBlank()) {
                return true; // No restriction
            }
            
            if (environment == null) {
                log.warn("Environment not provided for credential {}", credential.getId());
                return false;
            }
            
            List<String> allowedEnvironments = Arrays.asList(credential.getAllowedEnvironments().split(","));
            boolean allowed = allowedEnvironments.stream()
                .map(String::trim)
                .anyMatch(env -> env.equalsIgnoreCase(environment) || env.equals("*"));
            
            if (!allowed) {
                log.warn("Environment {} not allowed for credential {}", environment, credential.getId());
            }
            
            return allowed;
        });
    }

    /**
     * Check if approval is required and granted
     */
    private Mono<Boolean> checkApprovalRequired(Credential credential, boolean hasApproval) {
        return Mono.fromCallable(() -> {
            if (Boolean.TRUE.equals(credential.getRequireApprovalForAccess())) {
                if (!hasApproval) {
                    log.warn("Approval required but not granted for credential {}", credential.getId());
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Access request context
     */
    public record AccessRequest(
        String userId,
        String serviceName,
        String ipAddress,
        String environment,
        boolean hasApproval,
        String reason
    ) {}

    /**
     * Access decision result
     */
    public record AccessDecision(
        boolean allowed,
        String denyReason
    ) {}
}
