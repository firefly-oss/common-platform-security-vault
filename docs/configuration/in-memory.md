# In-Memory Provider Configuration

## Overview

The In-Memory provider is a built-in KMS implementation that stores encryption keys in memory. It's perfect for local development, testing, and CI/CD pipelines.

⚠️ **Warning**: Not suitable for production use. Keys are lost when the application restarts.

## Quick Start

### Minimal Configuration

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
        master-key-id: dev-master-key
```

That's it! No additional dependencies or setup required.

## Features

- ✅ **Zero Dependencies**: No external services required
- ✅ **Fast**: All operations are in-memory
- ✅ **AES-256-GCM**: Industry-standard encryption
- ✅ **Automatic Key Generation**: Keys created on-demand
- ✅ **Thread-Safe**: Uses ConcurrentHashMap

## Configuration Options

### Full Configuration

```yaml
firefly:
  security:
    vault:
      encryption:
        # Provider type
        provider: IN_MEMORY
        
        # Master key identifier (any string)
        master-key-id: dev-master-key
        
        # Encryption algorithm (optional, default: AES_256_GCM)
        algorithm: AES_256_GCM
```

### Environment Variables

```bash
export FIREFLY_SECURITY_VAULT_ENCRYPTION_PROVIDER=IN_MEMORY
export FIREFLY_SECURITY_VAULT_ENCRYPTION_MASTER_KEY_ID=dev-master-key
```

## Use Cases

### 1. Local Development

Perfect for running the application locally:

```yaml
# application-dev.yaml
spring:
  profiles:
    active: dev

firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
        master-key-id: dev-master-key
```

Run with:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 2. Unit Tests

Use in unit tests without mocking:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "firefly.security.vault.encryption.provider=IN_MEMORY",
    "firefly.security.vault.encryption.master-key-id=test-key"
})
class CredentialServiceTest {
    @Autowired
    private CredentialService credentialService;
    
    @Test
    void shouldEncryptCredential() {
        // Test with real encryption
    }
}
```

### 3. CI/CD Pipelines

Fast and reliable for automated testing:

```yaml
# application-test.yaml
firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
        master-key-id: ci-test-key
```

## How It Works

### Key Storage

Keys are stored in a `ConcurrentHashMap`:

```java
private final ConcurrentHashMap<String, SecretKey> keyStore = new ConcurrentHashMap<>();
```

### Automatic Key Generation

If a key doesn't exist, it's automatically generated:

```java
private SecretKey getOrCreateKey(String keyId) throws Exception {
    SecretKey existingKey = keyStore.get(keyId);
    if (existingKey != null) {
        return existingKey;
    }
    return generateMasterKey(keyId);
}
```

### Encryption Process

1. Get or create AES-256 key
2. Generate random 12-byte IV
3. Encrypt with AES-GCM
4. Return ciphertext with IV and auth tag

## Limitations

### ❌ Not for Production

**Reasons**:
- Keys lost on restart
- No key backup
- No high availability
- No audit trail
- No key rotation history

### ❌ Single Instance Only

Keys are not shared between application instances. Each instance has its own key store.

### ❌ No Persistence

Keys are never written to disk. They exist only in memory.

## Migration to Production

When moving to production, switch to a production-ready provider:

### Step 1: Choose Provider

Select based on your cloud platform:
- AWS → [AWS KMS](aws-kms.md)
- Azure → [Azure Key Vault](azure-key-vault.md)
- GCP → [Google Cloud KMS](google-cloud-kms.md)
- On-Premise → [HashiCorp Vault](hashicorp-vault.md)

### Step 2: Update Configuration

```yaml
# Change from IN_MEMORY to production provider
firefly:
  security:
    vault:
      encryption:
        provider: AWS_KMS  # Changed
        master-key-id: arn:aws:kms:us-east-1:123456789012:key/prod-key
```

### Step 3: Re-encrypt Credentials

Use the rotation API to re-encrypt existing credentials:

```bash
# Rotate all credentials to new provider
POST /api/v1/credentials/rotate-all
{
  "newKeyId": "arn:aws:kms:us-east-1:123456789012:key/prod-key"
}
```

## Troubleshooting

### Keys Lost After Restart

**Expected Behavior**: In-Memory provider doesn't persist keys.

**Solution**: Use a production provider for persistent keys.

### Different Keys on Each Instance

**Expected Behavior**: Each instance generates its own keys.

**Solution**: Use a centralized KMS provider (AWS KMS, Azure Key Vault, etc.)

## Best Practices

### ✅ Do

- Use for local development
- Use for unit tests
- Use for CI/CD pipelines
- Use for proof-of-concept demos

### ❌ Don't

- Use in production
- Use in staging (unless testing migration)
- Store sensitive production data
- Rely on key persistence

## Next Steps

- [AWS KMS Configuration](aws-kms.md)
- [Azure Key Vault Configuration](azure-key-vault.md)
- [Configuration Overview](README.md)

