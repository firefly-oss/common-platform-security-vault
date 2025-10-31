# Quick Start Guide

## Get Started in 5 Minutes

This guide will help you get the Firefly Security Vault up and running quickly.

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL 14+ (or use H2 for testing)
- Git

## Step 1: Clone the Repository

```bash
git clone https://github.com/your-org/common-platform-security-vault.git
cd common-platform-security-vault
```

## Step 2: Configure the Application

Create `application-dev.yaml`:

```yaml
spring:
  profiles:
    active: dev
  
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/firefly_vault_dev
    username: postgres
    password: postgres

firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
        master-key-id: dev-master-key
```

## Step 3: Start PostgreSQL

### Using Docker

```bash
docker run -d \
  --name postgres-firefly \
  -e POSTGRES_DB=firefly_vault_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14-alpine
```

### Or use H2 (In-Memory Database)

```yaml
spring:
  r2dbc:
    url: r2dbc:h2:mem:///testdb
```

## Step 4: Build and Run

```bash
# Build the project
mvn clean install

# Run the application
cd common-platform-security-vault-web
mvn spring-boot:run -Dspring.profiles.active=dev
```

The application will start on `http://localhost:8081`

## Step 5: Verify Installation

### Check Health

```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "keyManagement": {"status": "UP"}
  }
}
```

### Create Your First Credential

```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My API Key",
    "type": "API_KEY",
    "value": "super-secret-api-key-12345",
    "description": "Test API key"
  }'
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My API Key",
  "type": "API_KEY",
  "description": "Test API key",
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

### Retrieve the Credential

```bash
curl http://localhost:8081/api/v1/credentials/550e8400-e29b-41d4-a716-446655440000
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My API Key",
  "type": "API_KEY",
  "value": "super-secret-api-key-12345",
  "description": "Test API key",
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

## Common Operations

### List All Credentials

```bash
curl http://localhost:8081/api/v1/credentials
```

### Update a Credential

```bash
curl -X PUT http://localhost:8081/api/v1/credentials/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated API Key",
    "value": "new-secret-value",
    "description": "Updated description"
  }'
```

### Delete a Credential

```bash
curl -X DELETE http://localhost:8081/api/v1/credentials/550e8400-e29b-41d4-a716-446655440000
```

### Rotate a Credential

```bash
curl -X POST http://localhost:8081/api/v1/credentials/550e8400-e29b-41d4-a716-446655440000/rotate \
  -H "Content-Type: application/json" \
  -d '{
    "newValue": "rotated-secret-value"
  }'
```

## Next Steps

### Switch to Production KMS

For production, switch from IN_MEMORY to a real KMS provider:

- [AWS KMS Configuration](../configuration/aws-kms.md)
- [Azure Key Vault Configuration](../configuration/azure-key-vault.md)
- [Google Cloud KMS Configuration](../configuration/google-cloud-kms.md)
- [HashiCorp Vault Configuration](../configuration/hashicorp-vault.md)

### Explore the API

- [API Reference](../api/README.md)
- [Credential Management](../api/credentials.md)
- [Rotation Strategies](../api/rotation.md)

### Deploy to Production

- [Deployment Guide](../operations/deployment.md)
- [Monitoring Setup](../operations/monitoring.md)
- [Security Best Practices](../security/best-practices.md)

## Troubleshooting

### Application Won't Start

**Check Java version**:
```bash
java -version  # Should be 21 or higher
```

**Check PostgreSQL connection**:
```bash
psql -h localhost -U postgres -d firefly_vault_dev
```

### Database Migration Errors

**Reset database**:
```bash
# Drop and recreate
dropdb firefly_vault_dev
createdb firefly_vault_dev
```

### Port Already in Use

**Change port**:
```yaml
server:
  port: 8082  # Use different port
```

## Getting Help

- [FAQ](faq.md)
- [Troubleshooting Guide](../operations/troubleshooting.md)
- [GitHub Issues](https://github.com/your-org/common-platform-security-vault/issues)

## What's Next?

Now that you have the basics working, explore:

1. **Use Cases**: See [real-world examples](use-cases.md)
2. **Architecture**: Understand the [hexagonal architecture](../architecture/hexagonal-architecture.md)
3. **Security**: Learn about [encryption and security features](../security/encryption.md)
4. **Integration**: Integrate with your applications using the [SDK](../api/sdk.md)

