# Frequently Asked Questions (FAQ)

## General Questions

### What is Firefly Security Vault?

Firefly Security Vault is an enterprise-grade credential management system built with hexagonal architecture. It provides secure storage, encryption, rotation, and auditing of sensitive credentials like API keys, database passwords, and OAuth tokens.

### Why use Firefly Security Vault instead of environment variables?

Environment variables have several limitations:

- ❌ No encryption at rest
- ❌ No audit trail
- ❌ No rotation capabilities
- ❌ Visible in process listings
- ❌ No access control

Firefly Security Vault provides:

- ✅ AES-256-GCM encryption
- ✅ Complete audit logs
- ✅ Automatic rotation
- ✅ Secure storage
- ✅ Role-based access control

### Is it production-ready?

Yes! The Firefly Security Vault is production-ready with:

- ✅ 5 KMS providers (AWS, Azure, Google Cloud, HashiCorp, In-Memory)
- ✅ Resilience patterns (Circuit Breaker, Rate Limiter, Retry)
- ✅ Health checks and metrics
- ✅ Comprehensive test coverage
- ✅ Production-grade encryption

## Architecture Questions

### What is Hexagonal Architecture?

Hexagonal Architecture (Ports and Adapters) separates business logic from infrastructure concerns:

- **Ports**: Interfaces defining contracts
- **Adapters**: Implementations of ports
- **Benefits**: Testability, flexibility, maintainability

See [Hexagonal Architecture Guide](../architecture/hexagonal-architecture.md) for details.

### Can I switch KMS providers without code changes?

Yes! Simply change the configuration:

```yaml
# From AWS KMS
firefly.security.vault.encryption.provider: AWS_KMS

# To Azure Key Vault
firefly.security.vault.encryption.provider: AZURE_KEY_VAULT
```

The hexagonal architecture makes this seamless.

### How does envelope encryption work?

Envelope encryption uses two keys:

1. **Data Encryption Key (DEK)**: Encrypts the credential
2. **Master Key**: Encrypts the DEK

**Benefits**:
- Reduced KMS API calls
- Better performance
- Lower costs
- Unique key per credential

See [Security Overview](../security/README.md) for details.

## Configuration Questions

### Which KMS provider should I use?

Choose based on your deployment environment:

| Environment | Recommended Provider |
|-------------|---------------------|
| **Local Development** | IN_MEMORY |
| **AWS Cloud** | AWS KMS |
| **Azure Cloud** | Azure Key Vault |
| **Google Cloud** | Google Cloud KMS |
| **On-Premise** | HashiCorp Vault |
| **Hybrid** | HashiCorp Vault |

### Can I use IN_MEMORY in production?

**No!** IN_MEMORY is only for development and testing:

- ❌ Keys lost on restart
- ❌ No persistence
- ❌ No high availability
- ❌ No audit trail

Use a production KMS provider (AWS KMS, Azure Key Vault, etc.)

### How do I configure multiple environments?

Use Spring profiles:

```yaml
# application-dev.yaml
firefly.security.vault.encryption.provider: IN_MEMORY

# application-staging.yaml
firefly.security.vault.encryption.provider: AWS_KMS

# application-prod.yaml
firefly.security.vault.encryption.provider: AWS_KMS
```

Run with: `java -jar app.jar --spring.profiles.active=prod`

## Security Questions

### How secure is the encryption?

Very secure:

- **Algorithm**: AES-256-GCM (industry standard)
- **Key Size**: 256 bits
- **Authentication**: 128-bit auth tag
- **IV**: Unique 12-byte IV per encryption
- **Key Management**: Keys stored in KMS, never in plaintext

### Are credentials encrypted in the database?

Yes! Credentials are encrypted before storage:

1. Generate unique DEK
2. Encrypt credential with DEK
3. Encrypt DEK with master key
4. Store encrypted credential + encrypted DEK

The database only contains encrypted data.

### What happens if the KMS is unavailable?

The system uses resilience patterns:

- **Circuit Breaker**: Fails fast after threshold
- **Retry**: Retries with exponential backoff
- **Rate Limiter**: Prevents overwhelming the KMS

Configure in `application.yaml`:

```yaml
firefly:
  security:
    vault:
      resilience:
        enabled: true
```

### How often should I rotate credentials?

**Recommended**:
- **High-security**: Every 30 days
- **Medium-security**: Every 90 days
- **Low-security**: Every 180 days

Enable automatic rotation:

```yaml
firefly:
  security:
    vault:
      encryption:
        auto-rotation-enabled: true
        rotation-interval-days: 90
```

## Operations Questions

### How do I backup the vault?

Backup the PostgreSQL database:

```bash
pg_dump -h db.example.com -U firefly_prod firefly_security_vault_prod \
  > backup_$(date +%Y%m%d).sql
```

**Note**: Encrypted credentials are useless without access to the KMS master key.

### How do I restore from backup?

```bash
psql -h db.example.com -U firefly_prod firefly_security_vault_prod \
  < backup_20251031.sql
```

Ensure the KMS master key is still accessible.

### How do I monitor the vault?

Use Spring Boot Actuator endpoints:

- **Health**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

Set up Grafana dashboards for visualization.

### How do I scale horizontally?

The application is stateless and scales horizontally:

```bash
# Kubernetes
kubectl scale deployment firefly-security-vault --replicas=5

# Docker Swarm
docker service scale firefly-security-vault=5
```

## Performance Questions

### What is the expected latency?

Typical latencies:

| Operation | Latency |
|-----------|---------|
| **Encrypt** | 10-50ms |
| **Decrypt** | 10-50ms |
| **Create** | 50-100ms |
| **Read** | 20-50ms |

Latency depends on KMS provider and network.

### How many requests per second can it handle?

With default configuration:

- **Rate Limit**: 100 requests/second per instance
- **Recommended**: 3-5 instances for high availability
- **Total Capacity**: 300-500 requests/second

Adjust rate limiter for higher throughput:

```yaml
firefly:
  security:
    vault:
      resilience:
        rate-limiter:
          limit-for-period: 200
```

### Does it cache credentials?

No, credentials are not cached for security reasons. Each request:

1. Retrieves encrypted credential from database
2. Decrypts using KMS
3. Returns plaintext

**Recommendation**: Cache credentials in your application if needed.

## Integration Questions

### How do I integrate with my application?

Use the REST API:

```bash
# Get credential
curl http://vault.example.com/api/v1/credentials/{id}
```

Or use the SDK (if available):

```java
VaultClient client = VaultClient.builder()
    .baseUrl("http://vault.example.com")
    .build();

Credential credential = client.getCredential(credentialId);
String secretValue = credential.getValue();
```

### Can I use it with Kubernetes?

Yes! Deploy as a Kubernetes service:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: firefly-security-vault
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: vault
        image: firefly-security-vault:1.0.0
```

See [Deployment Guide](../operations/deployment.md) for details.

### Does it support multi-tenancy?

Yes, using metadata and tags:

```json
{
  "name": "Tenant A API Key",
  "metadata": {
    "tenantId": "tenant-a"
  },
  "tags": ["tenant-a"]
}
```

Filter by tenant:

```bash
GET /api/v1/credentials?tags=tenant-a
```

## Troubleshooting Questions

### Why am I getting "Key not found" errors?

**Possible causes**:

1. **Wrong key ID**: Verify `master-key-id` in configuration
2. **Wrong region**: Check KMS region matches configuration
3. **No permissions**: Verify IAM permissions

**Solution**: Check logs and validate configuration.

### Why is the circuit breaker open?

The circuit breaker opens after 50% failure rate:

```
Circuit breaker 'keyManagement' is OPEN
```

**Causes**:
- KMS unavailable
- Network issues
- Invalid credentials

**Solution**: Fix underlying issue, circuit breaker will auto-close after 60s.

### Why am I getting rate limited?

Default limit is 100 requests/second:

```
429 Too Many Requests
```

**Solutions**:
1. Increase rate limit in configuration
2. Add more instances
3. Implement client-side caching

## Migration Questions

### How do I migrate from environment variables?

1. **Create credentials** in the vault
2. **Update application** to fetch from vault API
3. **Remove** environment variables
4. **Test** thoroughly

### How do I migrate between KMS providers?

1. **Update configuration** to new provider
2. **Rotate all credentials** using the API:
   ```bash
   POST /api/v1/credentials/rotate-all
   ```
3. **Verify** all credentials work
4. **Remove** old KMS configuration

### Can I migrate without downtime?

Yes, using blue-green deployment:

1. Deploy new version with new KMS
2. Rotate credentials in background
3. Switch traffic to new version
4. Decommission old version

## Contributing Questions

### How do I contribute?

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Submit a pull request

See [Contributing Guide](../development/contributing.md) for details.

### How do I add a new KMS provider?

1. Implement `KeyManagementPort` interface
2. Add `@ConditionalOnProperty` annotation
3. Add Maven dependency (optional)
4. Write tests
5. Update documentation

See [Development Guide](../development/README.md) for details.

## Still Have Questions?

- 📖 [Documentation](../README.md)
- 🐛 [GitHub Issues](https://github.com/firefly-oss/common-platform-security-vault/issues)
- 💬 [Discussions](https://github.com/firefly-oss/common-platform-security-vault/discussions)
- 📧 [Email Support](mailto:dev@getfirefly.io)
- 🌐 [Website](https://getfirefly.io)

