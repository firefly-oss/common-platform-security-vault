# API Reference

## Overview

The Firefly Security Vault provides a RESTful API for managing encrypted credentials. All endpoints use JSON for request and response bodies.

## Base URL

```
http://localhost:8081/api/v1
```

## Authentication

Currently, the API uses Spring Security. Configure authentication in your `application.yaml`:

```yaml
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}
```

## API Endpoints

### Credentials

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/credentials/{id}` | Get credential by ID |
| `POST` | `/credentials` | Create new credential |
| `PUT` | `/credentials/{id}` | Update credential |
| `DELETE` | `/credentials/{id}` | Delete credential (soft delete) |
| `POST` | `/credentials/filter` | Filter credentials with pagination |

### Credential Decryption

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/credentials/{id}/decrypt?reason={reason}` | Decrypt credential value |

### Credential Types

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/credential-types/{id}` | Get credential type by ID |
| `POST` | `/credential-types` | Create new credential type |
| `PUT` | `/credential-types/{id}` | Update credential type |
| `DELETE` | `/credential-types/{id}` | Delete credential type |
| `POST` | `/credential-types/filter` | Filter credential types |

### Credential Statuses

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/credential-statuses/{id}` | Get credential status by ID |
| `POST` | `/credential-statuses` | Create new credential status |
| `PUT` | `/credential-statuses/{id}` | Update credential status |
| `DELETE` | `/credential-statuses/{id}` | Delete credential status |
| `POST` | `/credential-statuses/filter` | Filter credential statuses |

### Environment Types

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/environment-types/{id}` | Get environment type by ID |
| `POST` | `/environment-types` | Create new environment type |
| `PUT` | `/environment-types/{id}` | Update environment type |
| `DELETE` | `/environment-types/{id}` | Delete environment type |
| `POST` | `/environment-types/filter` | Filter environment types |

### Encryption Keys

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/encryption-keys/{id}` | Get encryption key metadata by ID |
| `POST` | `/encryption-keys` | Create new encryption key metadata |
| `PUT` | `/encryption-keys/{id}` | Update encryption key metadata |
| `DELETE` | `/encryption-keys/{id}` | Delete encryption key metadata |
| `POST` | `/encryption-keys/filter` | Filter encryption keys |

### Credential Versions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/credential-versions/{id}` | Get credential version by ID |
| `POST` | `/credential-versions/filter` | Filter credential versions (read-only) |

## Data Models

### CredentialDTO

Complete credential object with all fields:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "STRIPE_API_KEY_PROD",
  "name": "Stripe Production API Key",
  "description": "API key for Stripe payment processing",
  "credentialTypeId": "type-uuid",
  "credentialStatusId": "status-uuid",
  "environmentTypeId": "env-uuid",
  "providerId": "provider-uuid",
  "integrationId": "integration-uuid",
  "serviceId": "service-uuid",
  "tenantId": "tenant-uuid",
  "encryptedValue": "AQICAHh...encrypted-base64...",
  "encryptionAlgorithm": "AES-256-GCM",
  "encryptionKeyId": "key-uuid",
  "encryptionIv": "base64-iv",
  "credentialOwner": "payment-team",
  "credentialContactEmail": "payment-team@getfirefly.io",
  "expiresAt": "2026-01-01T00:00:00Z",
  "rotateBeforeDays": 7,
  "lastRotatedAt": "2025-10-31T10:00:00Z",
  "rotationEnabled": true,
  "autoRotationDays": 90,
  "lastUsedAt": "2025-10-31T12:00:00Z",
  "usageCount": 1523,
  "lastAccessedBy": "payment-service",
  "accessScope": "SERVICE",
  "allowedServices": "payment-service,billing-service",
  "allowedIps": "10.0.1.0/24,10.0.2.0/24",
  "allowedEnvironments": "PRODUCTION",
  "isSensitive": true,
  "requireApprovalForAccess": false,
  "auditAllAccess": true,
  "maskInLogs": true,
  "backupEnabled": true,
  "backupLocation": "s3://backups/credentials",
  "tags": "payment,stripe,production",
  "metadata": "{\"service\":\"stripe\",\"region\":\"us-east-1\"}",
  "active": true,
  "version": 1,
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z",
  "createdBy": "00000000-0000-0000-0000-000000000001",
  "updatedBy": "00000000-0000-0000-0000-000000000001"
}
```

### CredentialTypeDTO

```json
{
  "id": "type-uuid",
  "code": "API_KEY",
  "name": "API Key",
  "description": "External API authentication key",
  "active": true,
  "version": 1,
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

### CredentialStatusDTO

```json
{
  "id": "status-uuid",
  "code": "ACTIVE",
  "name": "Active",
  "description": "Credential is active and can be used",
  "active": true,
  "version": 1,
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

### EnvironmentTypeDTO

```json
{
  "id": "env-uuid",
  "code": "PRODUCTION",
  "name": "Production",
  "description": "Production environment",
  "active": true,
  "version": 1,
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

### EncryptionKeyDTO

```json
{
  "id": "key-uuid",
  "keyId": "master-key-prod-2025",
  "keyName": "Production Master Key 2025",
  "keyType": "SYMMETRIC",
  "keyAlgorithm": "AES-256-GCM",
  "keyProvider": "AWS_KMS",
  "keyLocation": "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012",
  "keyStatus": "ACTIVE",
  "isMasterKey": true,
  "keyPurpose": "CREDENTIAL_ENCRYPTION",
  "createdAt": "2025-10-31T10:00:00Z",
  "expiresAt": null,
  "rotatedAt": null,
  "rotationScheduleDays": 365,
  "metadata": "{\"region\":\"us-east-1\"}",
  "active": true,
  "version": 1,
  "createdBy": "system-uuid",
  "updatedAt": "2025-10-31T10:00:00Z"
}
```

### CredentialVersionDTO

```json
{
  "id": "version-uuid",
  "credentialId": "credential-uuid",
  "versionNumber": 2,
  "encryptedValue": "AQICAHh...encrypted-base64...",
  "encryptionAlgorithm": "AES-256-GCM",
  "encryptionKeyId": "key-uuid",
  "encryptionIv": "base64-iv",
  "validFrom": "2025-10-31T10:00:00Z",
  "validUntil": null,
  "isCurrent": true,
  "createdBy": "00000000-0000-0000-0000-000000000001",
  "createdAt": "2025-10-31T10:00:00Z",
  "rotationReason": "Scheduled rotation",
  "metadata": "{\"rotatedBy\":\"admin\"}"
}
```

## Examples

### Create Credential

**Request**:
```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STRIPE_API_KEY_PROD",
    "name": "Stripe Production API Key",
    "description": "API key for Stripe payment processing",
    "credentialTypeId": "type-uuid",
    "credentialStatusId": "status-uuid",
    "environmentTypeId": "env-uuid",
    "encryptedValue": "sk_live_51234567890abcdef",
    "rotationEnabled": true,
    "autoRotationDays": 90,
    "allowedServices": "payment-service",
    "allowedIps": "10.0.1.0/24",
    "isSensitive": true,
    "maskInLogs": true,
    "tags": "payment,stripe,production",
    "metadata": "{\"service\":\"stripe\",\"region\":\"us-east-1\"}"
  }'
```

**Response** (201 Created):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": "STRIPE_API_KEY_PROD",
  "name": "Stripe Production API Key",
  "description": "API key for Stripe payment processing",
  "credentialTypeId": "type-uuid",
  "credentialStatusId": "status-uuid",
  "environmentTypeId": "env-uuid",
  "encryptedValue": "AQICAHh...encrypted-base64...",
  "encryptionAlgorithm": "AES-256-GCM",
  "rotationEnabled": true,
  "autoRotationDays": 90,
  "active": true,
  "version": 1,
  "createdAt": "2025-10-31T10:00:00Z",
  "updatedAt": "2025-10-31T10:00:00Z",
  "createdBy": "00000000-0000-0000-0000-000000000001",
  "updatedBy": "00000000-0000-0000-0000-000000000001"
}
```

### Get Credential by ID

**Request**:
```bash
curl http://localhost:8081/api/v1/credentials/123e4567-e89b-12d3-a456-426614174000
```

**Response** (200 OK):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": "STRIPE_API_KEY_PROD",
  "name": "Stripe Production API Key",
  "encryptedValue": "AQICAHh...encrypted-base64...",
  "rotationEnabled": true,
  "autoRotationDays": 90,
  "lastUsedAt": "2025-10-31T12:00:00Z",
  "usageCount": 1523,
  "active": true,
  "createdAt": "2025-10-31T10:00:00Z"
}
```

### Decrypt Credential

**Request**:
```bash
curl -X POST "http://localhost:8081/api/v1/credentials/123e4567-e89b-12d3-a456-426614174000/decrypt?reason=Processing%20payment" \
  -H "X-User-Id: payment-service-account" \
  -H "X-Service-Name: payment-service" \
  -H "X-Forwarded-For: 10.0.1.50"
```

**Response** (200 OK):
```
sk_live_51234567890abcdef
```

The response is the plain text decrypted value.

### Update Credential

**Request**:
```bash
PUT /api/v1/credentials/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "name": "Stripe API Key (Updated)",
  "value": "sk_live_new_key_value",
  "description": "Updated Stripe API key"
}
```

**Response**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Stripe API Key (Updated)",
  "type": "API_KEY",
  "description": "Updated Stripe API key",
  "updatedAt": "2025-10-31T11:00:00Z"
}
```

### Rotate Credential

**Request**:
```bash
POST /api/v1/credentials/123e4567-e89b-12d3-a456-426614174000/rotate
Content-Type: application/json

{
  "newValue": "sk_live_rotated_key_value"
}
```

**Response**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "rotatedAt": "2025-10-31T12:00:00Z",
  "previousVersion": 1,
  "currentVersion": 2
}
```

### List Credentials

**Request**:
```bash
GET /api/v1/credentials?type=API_KEY&tags=production
```

**Response**:
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Stripe API Key",
      "type": "API_KEY",
      "tags": ["payment", "production"]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Delete Credential

**Request**:
```bash
DELETE /api/v1/credentials/123e4567-e89b-12d3-a456-426614174000
```

**Response**:
```
204 No Content
```

## Error Responses

### 400 Bad Request

```json
{
  "timestamp": "2025-10-31T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    },
    {
      "field": "value",
      "message": "Value cannot be empty"
    }
  ]
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-10-31T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Credential not found with id: 123e4567-e89b-12d3-a456-426614174000"
}
```

### 500 Internal Server Error

```json
{
  "timestamp": "2025-10-31T10:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to encrypt credential"
}
```

## Rate Limiting

The API implements rate limiting using Resilience4j:

- **Limit**: 100 requests per second per client
- **Response**: `429 Too Many Requests`

```json
{
  "timestamp": "2025-10-31T10:00:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later."
}
```

## Pagination

List endpoints support pagination:

**Query Parameters**:
- `page` - Page number (0-indexed, default: 0)
- `size` - Page size (default: 20, max: 100)
- `sort` - Sort field and direction (e.g., `name,asc`)

**Example**:
```bash
GET /api/v1/credentials?page=0&size=10&sort=createdAt,desc
```

## Filtering

List endpoints support filtering:

**Query Parameters**:
- `type` - Filter by credential type
- `tags` - Filter by tags (comma-separated)
- `search` - Search in name and description

**Example**:
```bash
GET /api/v1/credentials?type=API_KEY&tags=production,payment&search=stripe
```

## Next Steps

- [Credential Management Guide](credentials.md)
- [Rotation Strategies](rotation.md)
- [Audit Logs](audit.md)
- [SDK Usage](sdk.md)

