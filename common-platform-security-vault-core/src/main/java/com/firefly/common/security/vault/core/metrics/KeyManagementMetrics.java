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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Metrics for Key Management operations
 * 
 * Tracks:
 * - Encryption/Decryption operations
 * - Success/Failure rates
 * - Response times
 * - Provider-specific metrics
 * 
 * Metrics are exported to:
 * - Prometheus (via /actuator/prometheus)
 * - Spring Boot Actuator (via /actuator/metrics)
 */
@Slf4j
@Component
public class KeyManagementMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter encryptionSuccessCounter;
    private final Counter encryptionFailureCounter;
    private final Counter decryptionSuccessCounter;
    private final Counter decryptionFailureCounter;
    private final Counter keyRotationCounter;
    private final Counter keyValidationCounter;
    private final Counter dataKeyGenerationCounter;

    // Timers
    private final Timer encryptionTimer;
    private final Timer decryptionTimer;
    private final Timer keyRotationTimer;
    private final Timer keyValidationTimer;
    private final Timer dataKeyGenerationTimer;

    public KeyManagementMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.encryptionSuccessCounter = Counter.builder("vault.kms.encryption.success")
            .description("Number of successful encryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.encryptionFailureCounter = Counter.builder("vault.kms.encryption.failure")
            .description("Number of failed encryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.decryptionSuccessCounter = Counter.builder("vault.kms.decryption.success")
            .description("Number of successful decryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.decryptionFailureCounter = Counter.builder("vault.kms.decryption.failure")
            .description("Number of failed decryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.keyRotationCounter = Counter.builder("vault.kms.key.rotation")
            .description("Number of key rotation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.keyValidationCounter = Counter.builder("vault.kms.key.validation")
            .description("Number of key validation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.dataKeyGenerationCounter = Counter.builder("vault.kms.datakey.generation")
            .description("Number of data key generation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        // Initialize timers
        this.encryptionTimer = Timer.builder("vault.kms.encryption.duration")
            .description("Duration of encryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.decryptionTimer = Timer.builder("vault.kms.decryption.duration")
            .description("Duration of decryption operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.keyRotationTimer = Timer.builder("vault.kms.key.rotation.duration")
            .description("Duration of key rotation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.keyValidationTimer = Timer.builder("vault.kms.key.validation.duration")
            .description("Duration of key validation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        this.dataKeyGenerationTimer = Timer.builder("vault.kms.datakey.generation.duration")
            .description("Duration of data key generation operations")
            .tag("component", "key-management")
            .register(meterRegistry);

        log.info("Key Management metrics initialized");
    }

    // Encryption metrics
    public void recordEncryptionSuccess(String provider) {
        encryptionSuccessCounter.increment();
        Counter.builder("vault.kms.encryption.success.by.provider")
            .tag("provider", provider)
            .register(meterRegistry)
            .increment();
    }

    public void recordEncryptionFailure(String provider, String errorType) {
        encryptionFailureCounter.increment();
        Counter.builder("vault.kms.encryption.failure.by.provider")
            .tag("provider", provider)
            .tag("error", errorType)
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startEncryptionTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordEncryptionDuration(Timer.Sample sample, String provider) {
        sample.stop(Timer.builder("vault.kms.encryption.duration.by.provider")
            .tag("provider", provider)
            .register(meterRegistry));
        sample.stop(encryptionTimer);
    }

    // Decryption metrics
    public void recordDecryptionSuccess(String provider) {
        decryptionSuccessCounter.increment();
        Counter.builder("vault.kms.decryption.success.by.provider")
            .tag("provider", provider)
            .register(meterRegistry)
            .increment();
    }

    public void recordDecryptionFailure(String provider, String errorType) {
        decryptionFailureCounter.increment();
        Counter.builder("vault.kms.decryption.failure.by.provider")
            .tag("provider", provider)
            .tag("error", errorType)
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startDecryptionTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDecryptionDuration(Timer.Sample sample, String provider) {
        sample.stop(Timer.builder("vault.kms.decryption.duration.by.provider")
            .tag("provider", provider)
            .register(meterRegistry));
        sample.stop(decryptionTimer);
    }

    // Key rotation metrics
    public void recordKeyRotation(String provider, boolean success) {
        keyRotationCounter.increment();
        Counter.builder("vault.kms.key.rotation.by.provider")
            .tag("provider", provider)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startKeyRotationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordKeyRotationDuration(Timer.Sample sample, String provider) {
        sample.stop(Timer.builder("vault.kms.key.rotation.duration.by.provider")
            .tag("provider", provider)
            .register(meterRegistry));
        sample.stop(keyRotationTimer);
    }

    // Key validation metrics
    public void recordKeyValidation(String provider, boolean valid) {
        keyValidationCounter.increment();
        Counter.builder("vault.kms.key.validation.by.provider")
            .tag("provider", provider)
            .tag("valid", String.valueOf(valid))
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startKeyValidationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordKeyValidationDuration(Timer.Sample sample, String provider) {
        sample.stop(Timer.builder("vault.kms.key.validation.duration.by.provider")
            .tag("provider", provider)
            .register(meterRegistry));
        sample.stop(keyValidationTimer);
    }

    // Data key generation metrics
    public void recordDataKeyGeneration(String provider, boolean success) {
        dataKeyGenerationCounter.increment();
        Counter.builder("vault.kms.datakey.generation.by.provider")
            .tag("provider", provider)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startDataKeyGenerationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDataKeyGenerationDuration(Timer.Sample sample, String provider) {
        sample.stop(Timer.builder("vault.kms.datakey.generation.duration.by.provider")
            .tag("provider", provider)
            .register(meterRegistry));
        sample.stop(dataKeyGenerationTimer);
    }

    // Data size metrics
    public void recordEncryptedDataSize(long bytes, String provider) {
        meterRegistry.summary("vault.kms.encrypted.data.size")
            .record(bytes);
        meterRegistry.summary("vault.kms.encrypted.data.size.by.provider", "provider", provider)
            .record(bytes);
    }

    public void recordDecryptedDataSize(long bytes, String provider) {
        meterRegistry.summary("vault.kms.decrypted.data.size")
            .record(bytes);
        meterRegistry.summary("vault.kms.decrypted.data.size.by.provider", "provider", provider)
            .record(bytes);
    }
}

