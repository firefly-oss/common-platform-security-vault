# Azure Key Vault Configuration

## Overview

Azure Key Vault is a cloud service for securely storing and accessing secrets, keys, and certificates. This guide shows how to configure Firefly Security Vault with Azure Key Vault.

## Prerequisites

- Azure subscription
- Azure Key Vault instance
- Service Principal with Key Vault permissions

## Quick Start

### 1. Create Key Vault

```bash
# Create resource group
az group create --name firefly-rg --location eastus

# Create Key Vault
az keyvault create \
  --name firefly-vault-prod \
  --resource-group firefly-rg \
  --location eastus

# Create encryption key
az keyvault key create \
  --vault-name firefly-vault-prod \
  --name firefly-encryption-key \
  --kty RSA \
  --size 2048
```

### 2. Create Service Principal

```bash
# Create service principal
az ad sp create-for-rbac \
  --name firefly-security-vault \
  --role "Key Vault Crypto User" \
  --scopes /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.KeyVault/vaults/firefly-vault-prod

# Output:
# {
#   "appId": "12345678-1234-1234-1234-123456789012",
#   "displayName": "firefly-security-vault",
#   "password": "your-client-secret",
#   "tenant": "87654321-4321-4321-4321-210987654321"
# }
```

### 3. Add Maven Dependency

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
    <version>4.6.0</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.10.0</version>
</dependency>
```

### 4. Configure Application

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AZURE_KEY_VAULT
        master-key-id: firefly-encryption-key
        
        azure-key-vault:
          vault-url: https://firefly-vault-prod.vault.azure.net/
          key-name: firefly-encryption-key
          tenant-id: ${AZURE_TENANT_ID}
          client-id: ${AZURE_CLIENT_ID}
          client-secret: ${AZURE_CLIENT_SECRET}
```

### 5. Set Environment Variables

```bash
export AZURE_TENANT_ID=87654321-4321-4321-4321-210987654321
export AZURE_CLIENT_ID=12345678-1234-1234-1234-123456789012
export AZURE_CLIENT_SECRET=your-client-secret
```

## Configuration Options

### Full Configuration

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AZURE_KEY_VAULT
        master-key-id: firefly-encryption-key
        
        azure-key-vault:
          # Key Vault URL (required)
          vault-url: https://firefly-vault-prod.vault.azure.net/
          
          # Key name (required)
          key-name: firefly-encryption-key
          
          # Azure AD tenant ID (required)
          tenant-id: ${AZURE_TENANT_ID}
          
          # Service principal client ID (required)
          client-id: ${AZURE_CLIENT_ID}
          
          # Service principal client secret (required)
          client-secret: ${AZURE_CLIENT_SECRET}
          
          # Encryption algorithm (optional, default: RSA-OAEP-256)
          algorithm: RSA-OAEP-256
```

## Authentication Methods

### 1. Service Principal (Recommended for Production)

```yaml
azure-key-vault:
  tenant-id: ${AZURE_TENANT_ID}
  client-id: ${AZURE_CLIENT_ID}
  client-secret: ${AZURE_CLIENT_SECRET}
```

### 2. Managed Identity (Recommended for Azure VMs/AKS)

```yaml
azure-key-vault:
  vault-url: https://firefly-vault-prod.vault.azure.net/
  key-name: firefly-encryption-key
  # No credentials needed - uses managed identity
```

Enable managed identity:

```bash
# For VM
az vm identity assign --name my-vm --resource-group firefly-rg

# For AKS
az aks update --name my-aks --resource-group firefly-rg --enable-managed-identity
```

### 3. Azure CLI (Development Only)

```bash
# Login with Azure CLI
az login

# No additional configuration needed
```

## IAM Permissions

Grant the service principal Key Vault permissions:

```bash
# Using RBAC (recommended)
az role assignment create \
  --role "Key Vault Crypto User" \
  --assignee ${AZURE_CLIENT_ID} \
  --scope /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.KeyVault/vaults/firefly-vault-prod

# Or using access policies (legacy)
az keyvault set-policy \
  --name firefly-vault-prod \
  --spn ${AZURE_CLIENT_ID} \
  --key-permissions encrypt decrypt wrapKey unwrapKey
```

## Features

### Key Rotation

Enable automatic key rotation:

```bash
# Create rotation policy
az keyvault key rotation-policy update \
  --vault-name firefly-vault-prod \
  --name firefly-encryption-key \
  --value '{
    "lifetimeActions": [{
      "trigger": {"timeAfterCreate": "P90D"},
      "action": {"type": "Rotate"}
    }],
    "attributes": {"expiryTime": "P2Y"}
  }'
```

### Soft Delete

Enable soft delete for recovery:

```bash
az keyvault update \
  --name firefly-vault-prod \
  --enable-soft-delete true \
  --enable-purge-protection true
```

### Private Endpoint

Use private endpoint for enhanced security:

```bash
az network private-endpoint create \
  --name firefly-vault-pe \
  --resource-group firefly-rg \
  --vnet-name my-vnet \
  --subnet my-subnet \
  --private-connection-resource-id /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.KeyVault/vaults/firefly-vault-prod \
  --group-id vault \
  --connection-name firefly-vault-connection
```

## Monitoring

### Diagnostic Settings

Enable logging:

```bash
az monitor diagnostic-settings create \
  --name firefly-vault-diagnostics \
  --resource /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.KeyVault/vaults/firefly-vault-prod \
  --logs '[{"category": "AuditEvent", "enabled": true}]' \
  --metrics '[{"category": "AllMetrics", "enabled": true}]' \
  --workspace /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.OperationalInsights/workspaces/my-workspace
```

### Metrics

Monitor in Azure Portal:
- Total API requests
- API latency
- Availability
- Saturation

## Cost Optimization

### Pricing

- **Key operations**: $0.03 per 10,000 operations
- **Key storage**: Free for first 25 keys
- **HSM-protected keys**: $1/month per key

### Optimization Tips

1. Use envelope encryption (reduces API calls)
2. Cache data keys when appropriate
3. Use standard keys (not HSM) for non-sensitive data

## Troubleshooting

### Error: Forbidden

**Cause**: Insufficient permissions

**Solution**: Grant Key Vault Crypto User role

### Error: Key not found

**Cause**: Wrong key name or vault URL

**Solution**: Verify configuration matches Azure resources

### Error: Authentication failed

**Cause**: Invalid credentials

**Solution**: Verify tenant ID, client ID, and client secret

## Security Best Practices

1. ✅ Use managed identity when possible
2. ✅ Enable soft delete and purge protection
3. ✅ Use private endpoints
4. ✅ Enable diagnostic logging
5. ✅ Rotate keys regularly
6. ✅ Use RBAC instead of access policies
7. ✅ Separate keys for dev/staging/prod

## Next Steps

- [Configuration Overview](README.md)
- [AWS KMS Configuration](aws-kms.md)
- [Deployment Guide](../operations/deployment.md)

