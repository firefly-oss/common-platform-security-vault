# Data Model

## Overview

The Firefly Security Vault uses PostgreSQL with R2DBC for reactive database access. This document describes the database schema and entity relationships.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Credential                              │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID (PK)                                 │
│ name                  VARCHAR(255)                              │
│ type                  VARCHAR(50)                               │
│ encrypted_value       TEXT                                      │
│ description           TEXT                                      │
│ metadata              JSONB                                     │
│ tags                  TEXT[]                                    │
│ expires_at            TIMESTAMP                                 │
│ rotation_enabled      BOOLEAN                                   │
│ rotation_interval_days INTEGER                                  │
│ last_rotated_at       TIMESTAMP                                 │
│ created_at            TIMESTAMP                                 │
│ updated_at            TIMESTAMP                                 │
│ created_by            VARCHAR(255)                              │
│ updated_by            VARCHAR(255)                              │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ 1:N
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CredentialRotationHistory                     │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID (PK)                                 │
│ credential_id         UUID (FK → Credential)                    │
│ previous_value        TEXT                                      │
│ new_value             TEXT                                      │
│ rotated_at            TIMESTAMP                                 │
│ rotated_by            VARCHAR(255)                              │
│ rotation_reason       VARCHAR(255)                              │
│ success               BOOLEAN                                   │
│ error_message         TEXT                                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Credential                              │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ 1:N
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CredentialAccessLog                          │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID (PK)                                 │
│ credential_id         UUID (FK → Credential)                    │
│ action                VARCHAR(50)                               │
│ user_id               VARCHAR(255)                              │
│ ip_address            VARCHAR(45)                               │
│ user_agent            TEXT                                      │
│ accessed_at           TIMESTAMP                                 │
│ success               BOOLEAN                                   │
│ error_message         TEXT                                      │
│ metadata              JSONB                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Credential                              │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ 1:N
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CredentialShare                            │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID (PK)                                 │
│ credential_id         UUID (FK → Credential)                    │
│ shared_with_user_id   VARCHAR(255)                              │
│ permission            VARCHAR(50)                               │
│ shared_at             TIMESTAMP                                 │
│ shared_by             VARCHAR(255)                              │
│ expires_at            TIMESTAMP                                 │
│ revoked               BOOLEAN                                   │
│ revoked_at            TIMESTAMP                                 │
│ revoked_by            VARCHAR(255)                              │
└─────────────────────────────────────────────────────────────────┘
```

## Tables

### Credential

Stores encrypted credentials.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | NO | Primary key |
| `name` | VARCHAR(255) | NO | Credential name |
| `type` | VARCHAR(50) | NO | Credential type (API_KEY, DATABASE_PASSWORD, etc.) |
| `encrypted_value` | TEXT | NO | Encrypted credential value |
| `description` | TEXT | YES | Optional description |
| `metadata` | JSONB | YES | Additional metadata as JSON |
| `tags` | TEXT[] | YES | Array of tags for categorization |
| `expires_at` | TIMESTAMP | YES | Expiration timestamp |
| `rotation_enabled` | BOOLEAN | NO | Whether automatic rotation is enabled |
| `rotation_interval_days` | INTEGER | YES | Rotation interval in days |
| `last_rotated_at` | TIMESTAMP | YES | Last rotation timestamp |
| `created_at` | TIMESTAMP | NO | Creation timestamp |
| `updated_at` | TIMESTAMP | NO | Last update timestamp |
| `created_by` | VARCHAR(255) | YES | User who created the credential |
| `updated_by` | VARCHAR(255) | YES | User who last updated the credential |

**Indexes**:
```sql
CREATE INDEX idx_credential_type ON credential(type);
CREATE INDEX idx_credential_tags ON credential USING GIN(tags);
CREATE INDEX idx_credential_created_at ON credential(created_at);
CREATE INDEX idx_credential_expires_at ON credential(expires_at);
```

### CredentialRotationHistory

Tracks credential rotation history.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | NO | Primary key |
| `credential_id` | UUID | NO | Foreign key to Credential |
| `previous_value` | TEXT | YES | Previous encrypted value |
| `new_value` | TEXT | YES | New encrypted value |
| `rotated_at` | TIMESTAMP | NO | Rotation timestamp |
| `rotated_by` | VARCHAR(255) | YES | User who performed rotation |
| `rotation_reason` | VARCHAR(255) | YES | Reason for rotation |
| `success` | BOOLEAN | NO | Whether rotation succeeded |
| `error_message` | TEXT | YES | Error message if failed |

**Indexes**:
```sql
CREATE INDEX idx_rotation_credential_id ON credential_rotation_history(credential_id);
CREATE INDEX idx_rotation_rotated_at ON credential_rotation_history(rotated_at);
```

### CredentialAccessLog

Audit log for credential access.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | NO | Primary key |
| `credential_id` | UUID | NO | Foreign key to Credential |
| `action` | VARCHAR(50) | NO | Action performed (READ, CREATE, UPDATE, DELETE, ROTATE) |
| `user_id` | VARCHAR(255) | YES | User who performed the action |
| `ip_address` | VARCHAR(45) | YES | IP address of the request |
| `user_agent` | TEXT | YES | User agent string |
| `accessed_at` | TIMESTAMP | NO | Access timestamp |
| `success` | BOOLEAN | NO | Whether action succeeded |
| `error_message` | TEXT | YES | Error message if failed |
| `metadata` | JSONB | YES | Additional metadata |

**Indexes**:
```sql
CREATE INDEX idx_access_log_credential_id ON credential_access_log(credential_id);
CREATE INDEX idx_access_log_user_id ON credential_access_log(user_id);
CREATE INDEX idx_access_log_action ON credential_access_log(action);
CREATE INDEX idx_access_log_accessed_at ON credential_access_log(accessed_at);
```

### CredentialShare

Manages credential sharing between users.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | UUID | NO | Primary key |
| `credential_id` | UUID | NO | Foreign key to Credential |
| `shared_with_user_id` | VARCHAR(255) | NO | User ID to share with |
| `permission` | VARCHAR(50) | NO | Permission level (READ, WRITE) |
| `shared_at` | TIMESTAMP | NO | Share timestamp |
| `shared_by` | VARCHAR(255) | YES | User who shared the credential |
| `expires_at` | TIMESTAMP | YES | Share expiration timestamp |
| `revoked` | BOOLEAN | NO | Whether share is revoked |
| `revoked_at` | TIMESTAMP | YES | Revocation timestamp |
| `revoked_by` | VARCHAR(255) | YES | User who revoked the share |

**Indexes**:
```sql
CREATE INDEX idx_share_credential_id ON credential_share(credential_id);
CREATE INDEX idx_share_user_id ON credential_share(shared_with_user_id);
CREATE INDEX idx_share_expires_at ON credential_share(expires_at);
```

## Credential Types

Enumeration of supported credential types:

```java
public enum CredentialType {
    API_KEY,
    DATABASE_PASSWORD,
    AWS_ACCESS_KEY,
    OAUTH_TOKEN,
    SSH_KEY,
    TLS_CERTIFICATE,
    GENERIC_SECRET
}
```

## Access Actions

Enumeration of audit log actions:

```java
public enum AccessAction {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    ROTATE,
    SHARE,
    REVOKE_SHARE
}
```

## Database Migrations

Migrations are managed by Flyway. Migration files are located in:

```
common-platform-security-vault-models/src/main/resources/db/migration/
```

### Migration Files

- `V1__create_credential_table.sql` - Create credential table
- `V2__create_rotation_history_table.sql` - Create rotation history table
- `V3__create_access_log_table.sql` - Create access log table
- `V4__create_share_table.sql` - Create share table
- `V5__add_indexes.sql` - Add performance indexes

## Example Queries

### Find Expiring Credentials

```sql
SELECT id, name, type, expires_at
FROM credential
WHERE expires_at < NOW() + INTERVAL '30 days'
  AND expires_at > NOW()
ORDER BY expires_at ASC;
```

### Audit Log for User

```sql
SELECT c.name, a.action, a.accessed_at, a.success
FROM credential_access_log a
JOIN credential c ON a.credential_id = c.id
WHERE a.user_id = 'user-123'
ORDER BY a.accessed_at DESC
LIMIT 100;
```

### Credentials Due for Rotation

```sql
SELECT id, name, last_rotated_at, rotation_interval_days
FROM credential
WHERE rotation_enabled = true
  AND (last_rotated_at IS NULL 
       OR last_rotated_at < NOW() - (rotation_interval_days || ' days')::INTERVAL)
ORDER BY last_rotated_at ASC NULLS FIRST;
```

### Shared Credentials

```sql
SELECT c.name, s.shared_with_user_id, s.permission, s.expires_at
FROM credential_share s
JOIN credential c ON s.credential_id = c.id
WHERE s.revoked = false
  AND (s.expires_at IS NULL OR s.expires_at > NOW())
ORDER BY s.shared_at DESC;
```

## Data Retention

### Audit Logs

Recommended retention: **90 days** for compliance

```sql
-- Delete old audit logs
DELETE FROM credential_access_log
WHERE accessed_at < NOW() - INTERVAL '90 days';
```

### Rotation History

Recommended retention: **1 year**

```sql
-- Delete old rotation history
DELETE FROM credential_rotation_history
WHERE rotated_at < NOW() - INTERVAL '1 year';
```

## Backup Strategy

### Full Backup

```bash
# Daily full backup
pg_dump -h db.example.com -U firefly_prod firefly_security_vault_prod \
  > backup_$(date +%Y%m%d).sql
```

### Incremental Backup

Use PostgreSQL WAL archiving for point-in-time recovery:

```sql
-- Enable WAL archiving
archive_mode = on
archive_command = 'cp %p /backup/wal/%f'
```

## Next Steps

- [Architecture Overview](README.md)
- [Hexagonal Architecture](hexagonal-architecture.md)
- [API Reference](../api/README.md)

