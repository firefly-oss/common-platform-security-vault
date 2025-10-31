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


package com.firefly.common.security.vault.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrics for Security Vault monitoring
 * Tracks:
 * - Credential operations (create, read, update, delete, decrypt)
 * - Encryption/decryption performance
 * - Access control decisions (allow/deny)
 * - Audit log events
 * - Rotation operations
 * - Security events and violations
 */
@Component
@RequiredArgsConstructor
public class SecurityVaultMetrics {

    private final MeterRegistry meterRegistry;

    // Credential operation counters
    private static final String CREDENTIAL_OPERATIONS = "vault.credential.operations";
    private static final String ENCRYPTION_OPERATIONS = "vault.encryption.operations";
    private static final String ACCESS_CONTROL = "vault.access.control";
    private static final String AUDIT_EVENTS = "vault.audit.events";
    private static final String ROTATION_OPERATIONS = "vault.rotation.operations";
    private static final String SECURITY_EVENTS = "vault.security.events";

    /**
     * Record credential creation
     */
    public void recordCredentialCreation(String credentialType, boolean success) {
        Counter.builder(CREDENTIAL_OPERATIONS)
            .tag("operation", "create")
            .tag("credential_type", credentialType)
            .tag("success", String.valueOf(success))
            .description("Number of credential creation operations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record credential retrieval
     */
    public void recordCredentialRetrieval(String credentialType, boolean success) {
        Counter.builder(CREDENTIAL_OPERATIONS)
            .tag("operation", "read")
            .tag("credential_type", credentialType)
            .tag("success", String.valueOf(success))
            .description("Number of credential retrieval operations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record credential update
     */
    public void recordCredentialUpdate(String credentialType, boolean success) {
        Counter.builder(CREDENTIAL_OPERATIONS)
            .tag("operation", "update")
            .tag("credential_type", credentialType)
            .tag("success", String.valueOf(success))
            .description("Number of credential update operations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record credential deletion
     */
    public void recordCredentialDeletion(String credentialType, boolean success) {
        Counter.builder(CREDENTIAL_OPERATIONS)
            .tag("operation", "delete")
            .tag("credential_type", credentialType)
            .tag("success", String.valueOf(success))
            .description("Number of credential deletion operations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record encryption operation with timing
     */
    public void recordEncryption(String algorithm, Duration duration, boolean success) {
        // Counter for encryption operations
        Counter.builder(ENCRYPTION_OPERATIONS)
            .tag("operation", "encrypt")
            .tag("algorithm", algorithm)
            .tag("success", String.valueOf(success))
            .description("Number of encryption operations")
            .register(meterRegistry)
            .increment();

        // Timer for encryption performance
        if (success) {
            Timer.builder("vault.encryption.duration")
                .tag("operation", "encrypt")
                .tag("algorithm", algorithm)
                .description("Duration of encryption operations")
                .register(meterRegistry)
                .record(duration);
        }
    }

    /**
     * Record decryption operation with timing
     */
    public void recordDecryption(String algorithm, Duration duration, boolean success) {
        // Counter for decryption operations
        Counter.builder(ENCRYPTION_OPERATIONS)
            .tag("operation", "decrypt")
            .tag("algorithm", algorithm)
            .tag("success", String.valueOf(success))
            .description("Number of decryption operations")
            .register(meterRegistry)
            .increment();

        // Timer for decryption performance
        if (success) {
            Timer.builder("vault.encryption.duration")
                .tag("operation", "decrypt")
                .tag("algorithm", algorithm)
                .description("Duration of decryption operations")
                .register(meterRegistry)
                .record(duration);
        }
    }

    /**
     * Record access control decision
     */
    public void recordAccessControl(String decision, String reason) {
        Counter.builder(ACCESS_CONTROL)
            .tag("decision", decision) // "allow" or "deny"
            .tag("reason", reason)
            .description("Access control decisions")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record access denied for specific reason
     */
    public void recordAccessDenied(String denyReason) {
        Counter.builder(ACCESS_CONTROL)
            .tag("decision", "deny")
            .tag("reason", denyReason)
            .description("Access denied events")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record audit event
     */
    public void recordAuditEvent(String eventType, String result) {
        Counter.builder(AUDIT_EVENTS)
            .tag("event_type", eventType)
            .tag("result", result)
            .description("Audit log events")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record credential rotation
     */
    public void recordRotation(String rotationType, boolean success) {
        Counter.builder(ROTATION_OPERATIONS)
            .tag("type", rotationType) // "manual", "automatic", "emergency"
            .tag("success", String.valueOf(success))
            .description("Credential rotation operations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record rotation duration
     */
    public void recordRotationDuration(String rotationType, Duration duration) {
        Timer.builder("vault.rotation.duration")
            .tag("type", rotationType)
            .description("Duration of rotation operations")
            .register(meterRegistry)
            .record(duration);
    }

    /**
     * Record security event (violations, suspicious activity)
     */
    public void recordSecurityEvent(String eventType, String severity) {
        Counter.builder(SECURITY_EVENTS)
            .tag("event_type", eventType)
            .tag("severity", severity) // "low", "medium", "high", "critical"
            .description("Security events and violations")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record failed authentication attempt
     */
    public void recordFailedAuthentication(String source) {
        Counter.builder(SECURITY_EVENTS)
            .tag("event_type", "failed_authentication")
            .tag("source", source)
            .tag("severity", "medium")
            .description("Failed authentication attempts")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record credential expiration
     */
    public void recordCredentialExpiration(String credentialType) {
        Counter.builder(SECURITY_EVENTS)
            .tag("event_type", "credential_expired")
            .tag("credential_type", credentialType)
            .tag("severity", "high")
            .description("Expired credentials")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record key rotation event
     */
    public void recordKeyRotation(String keyId, boolean success) {
        Counter.builder(SECURITY_EVENTS)
            .tag("event_type", "key_rotation")
            .tag("key_id", keyId)
            .tag("success", String.valueOf(success))
            .tag("severity", "high")
            .description("Encryption key rotation events")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Track active credentials gauge
     */
    public void registerActiveCredentialsGauge(AtomicInteger activeCredentials) {
        meterRegistry.gauge("vault.credentials.active", activeCredentials);
    }

    /**
     * Track expired credentials gauge
     */
    public void registerExpiredCredentialsGauge(AtomicInteger expiredCredentials) {
        meterRegistry.gauge("vault.credentials.expired", expiredCredentials);
    }
}
