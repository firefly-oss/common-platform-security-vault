# Hexagonal Architecture

## Overview

The Firefly Security Vault implements **Hexagonal Architecture** (also known as **Ports and Adapters** pattern), which was introduced by Alistair Cockburn. This architectural style creates a clear separation between the core business logic and external concerns like databases, web frameworks, and third-party services.

## Core Concepts

### The Hexagon

The "hexagon" represents the core application logic, which is:

- **Independent** of frameworks and libraries
- **Testable** without external dependencies
- **Flexible** to changes in infrastructure
- **Focused** on business rules

### Ports

**Ports** are interfaces that define contracts for communication:

- **Inbound Ports** (Driving Ports): Define what the application can do (use cases)
- **Outbound Ports** (Driven Ports): Define what the application needs (dependencies)

### Adapters

**Adapters** are concrete implementations of ports:

- **Inbound Adapters** (Driving Adapters): Trigger application logic (e.g., REST controllers)
- **Outbound Adapters** (Driven Adapters): Implement external dependencies (e.g., database repositories, KMS clients)

## Implementation in Security Vault

### Ports (Interfaces)

#### 1. KeyManagementPort (Outbound Port)

**Location**: `common-platform-security-vault-core/src/main/java/com/firefly/common/security/vault/core/ports/KeyManagementPort.java`

**Purpose**: Defines the contract for low-level cryptographic operations with Key Management Services.

**Methods**:

```java
public interface KeyManagementPort {
    // Encrypt plaintext using the specified key
    Mono<byte[]> encrypt(byte[] plaintext, String keyId, Map<String, String> encryptionContext);
    
    // Decrypt ciphertext using the specified key
    Mono<byte[]> decrypt(byte[] ciphertext, String keyId, Map<String, String> encryptionContext);
    
    // Generate a data encryption key (DEK)
    Mono<DataKey> generateDataKey(String keyId, Map<String, String> encryptionContext);
    
    // Rotate a key to a new version
    Mono<KeyRotationResult> rotateKey(String keyId);
    
    // Validate that a key exists and is accessible
    Mono<Boolean> validateKey(String keyId);
    
    // Get the provider type (AWS_KMS, AZURE_KEY_VAULT, etc.)
    String getProviderType();
}
```

**Records**:

```java
record DataKey(byte[] plaintext, byte[] encrypted) {}
record KeyRotationResult(String newKeyVersion, Instant rotatedAt) {}
```

#### 2. CredentialEncryptionPort (Outbound Port)

**Location**: `common-platform-security-vault-core/src/main/java/com/firefly/common/security/vault/core/ports/CredentialEncryptionPort.java`

**Purpose**: Defines the contract for high-level credential encryption operations.

**Methods**:

```java
public interface CredentialEncryptionPort {
    // Encrypt a credential value
    Mono<String> encryptCredential(String plaintext, String keyId);
    
    // Decrypt a credential value
    Mono<String> decryptCredential(String encryptedValue, String keyId);
    
    // Rotate credential encryption from old key to new key
    Mono<String> rotateCredentialEncryption(String encryptedValue, String oldKeyId, String newKeyId);
    
    // Generate a new encryption key
    Mono<String> generateEncryptionKey(String keyId);
    
    // Validate an encryption key
    Mono<Boolean> validateEncryptionKey(String keyId);
}
```

### Adapters (Implementations)

#### Outbound Adapters (Driven Adapters)

##### 1. InMemoryKeyManagementAdapter

**Purpose**: In-memory implementation for development and testing

**Features**:
- AES-256-GCM encryption
- No external dependencies
- Keys stored in ConcurrentHashMap
- Automatic key generation

**Configuration**:
```yaml
firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
```

**When to Use**: Local development, unit tests, CI/CD pipelines

##### 2. AwsKmsKeyManagementAdapter

**Purpose**: AWS KMS integration for production

**Features**:
- Full AWS KMS SDK integration
- Envelope encryption support
- IAM-based access control
- CloudTrail audit logging
- Automatic key rotation

**Configuration**:
```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AWS_KMS
        master-key-id: arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012
        aws-kms:
          region: us-east-1
```

**When to Use**: AWS cloud deployments

##### 3. AzureKeyVaultKeyManagementAdapter

**Purpose**: Azure Key Vault integration for production

**Features**:
- Azure Key Vault SDK integration
- RSA-OAEP-256 encryption
- Azure AD authentication
- Managed HSM support
- Key versioning

**Configuration**:
```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AZURE_KEY_VAULT
        azure-key-vault:
          vault-url: https://my-vault.vault.azure.net/
          key-name: my-encryption-key
          tenant-id: ${AZURE_TENANT_ID}
          client-id: ${AZURE_CLIENT_ID}
          client-secret: ${AZURE_CLIENT_SECRET}
```

**When to Use**: Azure cloud deployments

##### 4. HashiCorpVaultKeyManagementAdapter

**Purpose**: HashiCorp Vault Transit Engine integration

**Features**:
- Vault Transit Engine
- Base64 encoding for API compatibility
- Namespace support (Vault Enterprise)
- TLS/mTLS support
- Token-based authentication

**Configuration**:
```yaml
firefly:
  security:
    vault:
      encryption:
        provider: HASHICORP_VAULT
        hashicorp-vault:
          address: https://vault.example.com:8200
          token: ${VAULT_TOKEN}
          transit-path: transit
          key-name: firefly-encryption-key
```

**When to Use**: On-premise deployments, hybrid cloud

##### 5. GoogleCloudKmsKeyManagementAdapter

**Purpose**: Google Cloud KMS integration for production

**Features**:
- Google Cloud KMS SDK integration
- Additional Authenticated Data (AAD) support
- Multi-region support
- Automatic key rotation
- Service account authentication

**Configuration**:
```yaml
firefly:
  security:
    vault:
      encryption:
        provider: GOOGLE_CLOUD_KMS
        google-cloud-kms:
          project-id: my-gcp-project
          location-id: global
          key-ring-id: my-keyring
          key-id: my-key
          credentials-path: /path/to/service-account.json
```

**When to Use**: Google Cloud Platform deployments

##### 6. AesGcmCredentialEncryptionAdapter

**Purpose**: High-level credential encryption using AES-256-GCM

**Features**:
- AES-256-GCM with 128-bit authentication tags
- Unique 12-byte IV per encryption
- Envelope encryption pattern
- Base64 encoding for storage
- Credential rotation support

**Implementation Details**:
- Uses `KeyManagementPort` to generate data keys
- Encrypts credentials with data keys
- Stores encrypted data key with ciphertext
- Format: `{encryptedDataKey}:{iv}:{ciphertext}:{authTag}`

##### 7. ResilientKeyManagementAdapter (Decorator)

**Purpose**: Add resilience patterns to any KeyManagementPort implementation

**Features**:
- Circuit Breaker (50% failure threshold, 60s wait)
- Rate Limiter (100 calls/second)
- Retry (3 attempts, exponential backoff)
- Metrics and event logging

**Implementation**:
```java
@Bean
@ConditionalOnProperty(name = "firefly.security.vault.resilience.enabled", havingValue = "true")
public KeyManagementPort resilientKeyManagementPort(
    KeyManagementPort delegate,
    CircuitBreaker circuitBreaker,
    RateLimiter rateLimiter,
    Retry retry
) {
    return new ResilientKeyManagementAdapter(delegate, circuitBreaker, rateLimiter, retry);
}
```

## Benefits of Hexagonal Architecture

### 1. Testability

**Easy to Test**: Mock ports instead of concrete implementations

```java
@Test
void shouldEncryptCredential() {
    // Mock the port
    KeyManagementPort mockPort = mock(KeyManagementPort.class);
    when(mockPort.encrypt(any(), any(), any()))
        .thenReturn(Mono.just("encrypted".getBytes()));
    
    // Test the service
    CredentialEncryptionPort service = new AesGcmCredentialEncryptionAdapter(mockPort);
    
    StepVerifier.create(service.encryptCredential("secret", "key-id"))
        .expectNextMatches(encrypted -> encrypted != null)
        .verifyComplete();
}
```

### 2. Flexibility

**Switch Providers**: Change KMS provider via configuration without code changes

```yaml
# Development
firefly.security.vault.encryption.provider: IN_MEMORY

# Production (AWS)
firefly.security.vault.encryption.provider: AWS_KMS

# Production (Azure)
firefly.security.vault.encryption.provider: AZURE_KEY_VAULT
```

### 3. Maintainability

**Clear Boundaries**: Each adapter is independent and can be modified without affecting others

- Update AWS SDK version → Only change `AwsKmsKeyManagementAdapter`
- Add new KMS provider → Implement `KeyManagementPort` interface
- Change encryption algorithm → Only modify `AesGcmCredentialEncryptionAdapter`

### 4. Extensibility

**Add New Providers**: Implement the port interface

```java
@Component
@ConditionalOnProperty(name = "firefly.security.vault.encryption.provider", havingValue = "CUSTOM_KMS")
public class CustomKmsKeyManagementAdapter implements KeyManagementPort {
    @Override
    public Mono<byte[]> encrypt(byte[] plaintext, String keyId, Map<String, String> context) {
        // Custom implementation
    }
    
    // ... other methods
}
```

## Dependency Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    REST Controllers                         │
│                  (Inbound Adapters)                         │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  Service Implementations                    │
│                  (Application Layer)                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Port Interfaces                          │
│                    (Domain Layer)                           │
│  • KeyManagementPort                                        │
│  • CredentialEncryptionPort                                 │
└─────────────────────────────────────────────────────────────┘
                          ▲
                          │
┌─────────────────────────────────────────────────────────────┐
│                  Adapter Implementations                    │
│                  (Infrastructure Layer)                     │
│  • AwsKmsKeyManagementAdapter                              │
│  • AzureKeyVaultKeyManagementAdapter                       │
│  • HashiCorpVaultKeyManagementAdapter                      │
│  • GoogleCloudKmsKeyManagementAdapter                      │
│  • InMemoryKeyManagementAdapter                            │
│  • AesGcmCredentialEncryptionAdapter                       │
└─────────────────────────────────────────────────────────────┘
```

**Key Principle**: Dependencies point inward. The domain layer (ports) has no dependencies on infrastructure.

## Best Practices

### 1. Keep Ports Simple

Ports should define clear, focused contracts:

✅ **Good**: `Mono<byte[]> encrypt(byte[] plaintext, String keyId)`  
❌ **Bad**: `Mono<EncryptionResult> encryptWithLoggingAndMetrics(EncryptionRequest request)`

### 2. One Adapter Per Provider

Each KMS provider should have its own adapter:

✅ **Good**: `AwsKmsKeyManagementAdapter`, `AzureKeyVaultKeyManagementAdapter`  
❌ **Bad**: `UniversalKmsAdapter` with if/else for each provider

### 3. Use Conditional Bean Loading

Load only the configured adapter:

```java
@Component
@ConditionalOnProperty(name = "firefly.security.vault.encryption.provider", havingValue = "AWS_KMS")
@ConditionalOnClass(name = "software.amazon.awssdk.services.kms.KmsAsyncClient")
public class AwsKmsKeyManagementAdapter implements KeyManagementPort {
    // ...
}
```

### 4. Leverage Reactive Programming

All port methods return `Mono<T>` or `Flux<T>` for non-blocking operations:

```java
Mono<byte[]> encrypt(byte[] plaintext, String keyId, Map<String, String> context);
```

## Next Steps

- [Data Model](data-model.md)
- [Design Decisions](design-decisions.md)
- [Configuration Guide](../configuration/README.md)

