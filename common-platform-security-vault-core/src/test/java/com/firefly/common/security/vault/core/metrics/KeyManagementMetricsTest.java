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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for KeyManagementMetrics
 */
class KeyManagementMetricsTest {

    private MeterRegistry meterRegistry;
    private KeyManagementMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new KeyManagementMetrics(meterRegistry);
    }

    @Test
    void shouldRecordEncryptionSuccess() {
        // When
        metrics.recordEncryptionSuccess("AWS_KMS");

        // Then
        Counter counter = meterRegistry.find("vault.kms.encryption.success").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.encryption.success.by.provider")
            .tag("provider", "AWS_KMS")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordEncryptionFailure() {
        // When
        metrics.recordEncryptionFailure("AWS_KMS", "TimeoutException");

        // Then
        Counter counter = meterRegistry.find("vault.kms.encryption.failure").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.encryption.failure.by.provider")
            .tag("provider", "AWS_KMS")
            .tag("error", "TimeoutException")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordEncryptionDuration() {
        // Given
        Timer.Sample sample = metrics.startEncryptionTimer();

        // When
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metrics.recordEncryptionDuration(sample, "AWS_KMS");

        // Then
        Timer timer = meterRegistry.find("vault.kms.encryption.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThan(0);

        Timer providerTimer = meterRegistry.find("vault.kms.encryption.duration.by.provider")
            .tag("provider", "AWS_KMS")
            .timer();
        assertThat(providerTimer).isNotNull();
        assertThat(providerTimer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordDecryptionSuccess() {
        // When
        metrics.recordDecryptionSuccess("AZURE_KEY_VAULT");

        // Then
        Counter counter = meterRegistry.find("vault.kms.decryption.success").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.decryption.success.by.provider")
            .tag("provider", "AZURE_KEY_VAULT")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordDecryptionFailure() {
        // When
        metrics.recordDecryptionFailure("AZURE_KEY_VAULT", "InvalidKeyException");

        // Then
        Counter counter = meterRegistry.find("vault.kms.decryption.failure").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.decryption.failure.by.provider")
            .tag("provider", "AZURE_KEY_VAULT")
            .tag("error", "InvalidKeyException")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordDecryptionDuration() {
        // Given
        Timer.Sample sample = metrics.startDecryptionTimer();

        // When
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metrics.recordDecryptionDuration(sample, "AZURE_KEY_VAULT");

        // Then
        Timer timer = meterRegistry.find("vault.kms.decryption.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordKeyRotation() {
        // When
        metrics.recordKeyRotation("HASHICORP_VAULT", true);

        // Then
        Counter counter = meterRegistry.find("vault.kms.key.rotation").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.key.rotation.by.provider")
            .tag("provider", "HASHICORP_VAULT")
            .tag("success", "true")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordKeyRotationDuration() {
        // Given
        Timer.Sample sample = metrics.startKeyRotationTimer();

        // When
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metrics.recordKeyRotationDuration(sample, "HASHICORP_VAULT");

        // Then
        Timer timer = meterRegistry.find("vault.kms.key.rotation.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordKeyValidation() {
        // When
        metrics.recordKeyValidation("GOOGLE_CLOUD_KMS", true);

        // Then
        Counter counter = meterRegistry.find("vault.kms.key.validation").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.key.validation.by.provider")
            .tag("provider", "GOOGLE_CLOUD_KMS")
            .tag("valid", "true")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordKeyValidationDuration() {
        // Given
        Timer.Sample sample = metrics.startKeyValidationTimer();

        // When
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metrics.recordKeyValidationDuration(sample, "GOOGLE_CLOUD_KMS");

        // Then
        Timer timer = meterRegistry.find("vault.kms.key.validation.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordDataKeyGeneration() {
        // When
        metrics.recordDataKeyGeneration("IN_MEMORY", true);

        // Then
        Counter counter = meterRegistry.find("vault.kms.datakey.generation").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Counter providerCounter = meterRegistry.find("vault.kms.datakey.generation.by.provider")
            .tag("provider", "IN_MEMORY")
            .tag("success", "true")
            .counter();
        assertThat(providerCounter).isNotNull();
        assertThat(providerCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldRecordDataKeyGenerationDuration() {
        // Given
        Timer.Sample sample = metrics.startDataKeyGenerationTimer();

        // When
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        metrics.recordDataKeyGenerationDuration(sample, "IN_MEMORY");

        // Then
        Timer timer = meterRegistry.find("vault.kms.datakey.generation.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordEncryptedDataSize() {
        // When
        metrics.recordEncryptedDataSize(1024, "AWS_KMS");

        // Then
        assertThat(meterRegistry.find("vault.kms.encrypted.data.size").summary()).isNotNull();
        assertThat(meterRegistry.find("vault.kms.encrypted.data.size.by.provider")
            .tag("provider", "AWS_KMS")
            .summary()).isNotNull();
    }

    @Test
    void shouldRecordDecryptedDataSize() {
        // When
        metrics.recordDecryptedDataSize(2048, "AZURE_KEY_VAULT");

        // Then
        assertThat(meterRegistry.find("vault.kms.decrypted.data.size").summary()).isNotNull();
        assertThat(meterRegistry.find("vault.kms.decrypted.data.size.by.provider")
            .tag("provider", "AZURE_KEY_VAULT")
            .summary()).isNotNull();
    }

    @Test
    void shouldIncrementCountersMultipleTimes() {
        // When
        metrics.recordEncryptionSuccess("AWS_KMS");
        metrics.recordEncryptionSuccess("AWS_KMS");
        metrics.recordEncryptionSuccess("AZURE_KEY_VAULT");

        // Then
        Counter counter = meterRegistry.find("vault.kms.encryption.success").counter();
        assertThat(counter.count()).isEqualTo(3.0);

        Counter awsCounter = meterRegistry.find("vault.kms.encryption.success.by.provider")
            .tag("provider", "AWS_KMS")
            .counter();
        assertThat(awsCounter.count()).isEqualTo(2.0);

        Counter azureCounter = meterRegistry.find("vault.kms.encryption.success.by.provider")
            .tag("provider", "AZURE_KEY_VAULT")
            .counter();
        assertThat(azureCounter.count()).isEqualTo(1.0);
    }
}

