# HashiCorp Vault Configuration

## Overview

HashiCorp Vault is a tool for securely accessing secrets. This guide shows how to configure Firefly Security Vault with HashiCorp Vault Transit Engine.

## Prerequisites

- HashiCorp Vault server (1.11+)
- Vault token with transit permissions
- Network access to Vault server

## Quick Start

### 1. Start Vault Server

#### Development Mode

```bash
# Start Vault in dev mode (NOT for production)
vault server -dev

# Export Vault address and token
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='root'
```

#### Production Mode

```bash
# Start Vault server
vault server -config=config.hcl

# Initialize Vault
vault operator init

# Unseal Vault (repeat 3 times with different keys)
vault operator unseal <unseal-key-1>
vault operator unseal <unseal-key-2>
vault operator unseal <unseal-key-3>

# Login
vault login <root-token>
```

### 2. Enable Transit Engine

```bash
# Enable transit secrets engine
vault secrets enable transit

# Create encryption key
vault write -f transit/keys/firefly-encryption-key

# Verify key created
vault read transit/keys/firefly-encryption-key
```

### 3. Create Policy

```bash
# Create policy file
cat > firefly-policy.hcl <<EOF
path "transit/encrypt/firefly-encryption-key" {
  capabilities = ["update"]
}

path "transit/decrypt/firefly-encryption-key" {
  capabilities = ["update"]
}

path "transit/datakey/plaintext/firefly-encryption-key" {
  capabilities = ["update"]
}

path "transit/keys/firefly-encryption-key" {
  capabilities = ["read"]
}

path "transit/keys/firefly-encryption-key/rotate" {
  capabilities = ["update"]
}
EOF

# Apply policy
vault policy write firefly-policy firefly-policy.hcl

# Create token with policy
vault token create -policy=firefly-policy
```

### 4. Add Maven Dependency

```xml
<dependency>
    <groupId>com.bettercloud</groupId>
    <artifactId>vault-java-driver</artifactId>
    <version>6.1.0</version>
</dependency>
```

### 5. Configure Application

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: HASHICORP_VAULT
        master-key-id: firefly-encryption-key
        
        hashicorp-vault:
          address: https://vault.example.com:8200
          token: ${VAULT_TOKEN}
          transit-path: transit
          key-name: firefly-encryption-key
```

### 6. Set Environment Variables

```bash
export VAULT_TOKEN=s.xxxxxxxxxxxxxxxxxxxxxxxx
```

## Configuration Options

### Full Configuration

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: HASHICORP_VAULT
        master-key-id: firefly-encryption-key
        
        hashicorp-vault:
          # Vault server address (required)
          address: https://vault.example.com:8200
          
          # Vault token (required)
          token: ${VAULT_TOKEN}
          
          # Transit engine path (optional, default: transit)
          transit-path: transit
          
          # Encryption key name (required)
          key-name: firefly-encryption-key
          
          # Namespace (Vault Enterprise only)
          namespace: firefly
          
          # TLS configuration
          tls:
            # Skip TLS verification (dev only, default: false)
            skip-verify: false
            
            # Client certificate path
            client-cert-path: /path/to/client.crt
            
            # Client key path
            client-key-path: /path/to/client.key
            
            # CA certificate path
            ca-cert-path: /path/to/ca.crt
```

## Authentication Methods

### 1. Token (Simplest)

```yaml
hashicorp-vault:
  token: ${VAULT_TOKEN}
```

### 2. AppRole (Recommended for Production)

```bash
# Enable AppRole
vault auth enable approle

# Create role
vault write auth/approle/role/firefly-role \
  token_policies="firefly-policy" \
  token_ttl=1h \
  token_max_ttl=4h

# Get role ID
vault read auth/approle/role/firefly-role/role-id

# Generate secret ID
vault write -f auth/approle/role/firefly-role/secret-id
```

Configuration:

```yaml
hashicorp-vault:
  auth-method: approle
  role-id: ${VAULT_ROLE_ID}
  secret-id: ${VAULT_SECRET_ID}
```

### 3. Kubernetes (For K8s Deployments)

```bash
# Enable Kubernetes auth
vault auth enable kubernetes

# Configure Kubernetes auth
vault write auth/kubernetes/config \
  kubernetes_host="https://kubernetes.default.svc:443"

# Create role
vault write auth/kubernetes/role/firefly \
  bound_service_account_names=firefly-security-vault \
  bound_service_account_namespaces=firefly \
  policies=firefly-policy \
  ttl=1h
```

Configuration:

```yaml
hashicorp-vault:
  auth-method: kubernetes
  kubernetes-role: firefly
```

## Features

### Key Rotation

```bash
# Rotate encryption key
vault write -f transit/keys/firefly-encryption-key/rotate

# View key versions
vault read transit/keys/firefly-encryption-key

# Output:
# latest_version: 2
# min_decryption_version: 1
```

### Key Configuration

```bash
# Configure key
vault write transit/keys/firefly-encryption-key/config \
  min_decryption_version=1 \
  min_encryption_version=0 \
  deletion_allowed=false \
  exportable=false \
  allow_plaintext_backup=false
```

### Convergent Encryption

Enable for deterministic encryption:

```bash
vault write transit/keys/firefly-encryption-key/config \
  convergent_encryption=true \
  derived=true
```

## High Availability

### Vault Cluster

```hcl
# config.hcl
storage "consul" {
  address = "127.0.0.1:8500"
  path    = "vault/"
}

listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = 0
  tls_cert_file = "/path/to/cert.pem"
  tls_key_file  = "/path/to/key.pem"
}

api_addr = "https://vault-1.example.com:8200"
cluster_addr = "https://vault-1.example.com:8201"

ui = true
```

### Load Balancing

Configure multiple Vault addresses:

```yaml
hashicorp-vault:
  addresses:
    - https://vault-1.example.com:8200
    - https://vault-2.example.com:8200
    - https://vault-3.example.com:8200
```

## Monitoring

### Audit Logging

```bash
# Enable audit logging
vault audit enable file file_path=/var/log/vault/audit.log

# View audit logs
tail -f /var/log/vault/audit.log
```

### Metrics

Vault exposes Prometheus metrics:

```hcl
# config.hcl
telemetry {
  prometheus_retention_time = "30s"
  disable_hostname = true
}
```

Scrape metrics:

```
http://vault.example.com:8200/v1/sys/metrics?format=prometheus
```

## Security Best Practices

### 1. Use TLS

```hcl
listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/path/to/cert.pem"
  tls_key_file  = "/path/to/key.pem"
}
```

### 2. Enable Auto-Unseal

```hcl
seal "awskms" {
  region     = "us-east-1"
  kms_key_id = "arn:aws:kms:us-east-1:123456789012:key/12345678"
}
```

### 3. Rotate Tokens

```bash
# Create periodic token
vault token create \
  -policy=firefly-policy \
  -period=24h
```

### 4. Use Namespaces (Enterprise)

```bash
# Create namespace
vault namespace create firefly

# Use namespace
export VAULT_NAMESPACE=firefly
```

## Troubleshooting

### Error: Permission denied

**Cause**: Insufficient policy permissions

**Solution**: Update policy to include required paths

### Error: Connection refused

**Cause**: Vault server not running or wrong address

**Solution**: Verify Vault address and server status

### Error: Vault is sealed

**Cause**: Vault needs to be unsealed

**Solution**: Unseal Vault with unseal keys

```bash
vault operator unseal <unseal-key>
```

## Cost Optimization

HashiCorp Vault is open-source and free. Enterprise features include:

- **Namespaces**: Multi-tenancy
- **Performance Replication**: Read scalability
- **DR Replication**: Disaster recovery
- **HSM Support**: Hardware security modules

## Next Steps

- [Configuration Overview](README.md)
- [AWS KMS Configuration](aws-kms.md)
- [Deployment Guide](../operations/deployment.md)

