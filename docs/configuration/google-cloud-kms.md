# Google Cloud KMS Configuration

## Overview

Google Cloud Key Management Service (KMS) lets you create, import, and manage cryptographic keys. This guide shows how to configure Firefly Security Vault with Google Cloud KMS.

## Prerequisites

- Google Cloud Platform account
- GCP project with billing enabled
- Service account with KMS permissions
- gcloud CLI installed

## Quick Start

### 1. Enable Cloud KMS API

```bash
# Enable Cloud KMS API
gcloud services enable cloudkms.googleapis.com

# Verify API is enabled
gcloud services list --enabled | grep cloudkms
```

### 2. Create Key Ring and Key

```bash
# Set variables
export PROJECT_ID=my-gcp-project
export LOCATION=global
export KEY_RING=firefly-keyring
export KEY_NAME=firefly-encryption-key

# Create key ring
gcloud kms keyrings create ${KEY_RING} \
  --location=${LOCATION}

# Create encryption key
gcloud kms keys create ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --purpose=encryption

# Verify key created
gcloud kms keys describe ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING}
```

### 3. Create Service Account

```bash
# Create service account
gcloud iam service-accounts create firefly-security-vault \
  --display-name="Firefly Security Vault"

# Grant KMS permissions
gcloud kms keys add-iam-policy-binding ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --member="serviceAccount:firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudkms.cryptoKeyEncrypterDecrypter"

# Create and download key file
gcloud iam service-accounts keys create ~/firefly-sa-key.json \
  --iam-account=firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com
```

### 4. Add Maven Dependency

```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-kms</artifactId>
    <version>2.20.0</version>
</dependency>
```

### 5. Configure Application

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: GOOGLE_CLOUD_KMS
        master-key-id: firefly-encryption-key
        
        google-cloud-kms:
          project-id: my-gcp-project
          location-id: global
          key-ring-id: firefly-keyring
          key-id: firefly-encryption-key
          credentials-path: /path/to/firefly-sa-key.json
```

### 6. Set Environment Variables

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/firefly-sa-key.json
```

## Configuration Options

### Full Configuration

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: GOOGLE_CLOUD_KMS
        master-key-id: firefly-encryption-key
        
        google-cloud-kms:
          # GCP project ID (required)
          project-id: my-gcp-project
          
          # Location (required)
          # Options: global, us-east1, us-west1, europe-west1, asia-east1, etc.
          location-id: global
          
          # Key ring ID (required)
          key-ring-id: firefly-keyring
          
          # Key ID (required)
          key-id: firefly-encryption-key
          
          # Service account credentials path (optional)
          # If not specified, uses Application Default Credentials
          credentials-path: /path/to/service-account.json
          
          # Endpoint override (optional, for testing)
          endpoint: cloudkms.googleapis.com:443
```

## Authentication Methods

### 1. Service Account Key File (Recommended for Non-GCP)

```yaml
google-cloud-kms:
  credentials-path: /path/to/service-account.json
```

Or use environment variable:

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
```

### 2. Application Default Credentials (Recommended for GCP)

When running on GCP (GCE, GKE, Cloud Run, etc.), use ADC:

```yaml
google-cloud-kms:
  # No credentials-path needed
  project-id: my-gcp-project
  location-id: global
  key-ring-id: firefly-keyring
  key-id: firefly-encryption-key
```

### 3. Workload Identity (Recommended for GKE)

```bash
# Create Kubernetes service account
kubectl create serviceaccount firefly-security-vault -n firefly

# Bind to GCP service account
gcloud iam service-accounts add-iam-policy-binding \
  firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com \
  --role roles/iam.workloadIdentityUser \
  --member "serviceAccount:${PROJECT_ID}.svc.id.goog[firefly/firefly-security-vault]"

# Annotate Kubernetes service account
kubectl annotate serviceaccount firefly-security-vault \
  -n firefly \
  iam.gke.io/gcp-service-account=firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com
```

## IAM Permissions

Grant the service account KMS permissions:

```bash
# Encrypt/Decrypt permissions
gcloud kms keys add-iam-policy-binding ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --member="serviceAccount:firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudkms.cryptoKeyEncrypterDecrypter"

# Or use predefined roles
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudkms.cryptoKeyEncrypterDecrypter"
```

## Features

### Key Rotation

Enable automatic key rotation:

```bash
# Set rotation period (90 days)
gcloud kms keys update ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --rotation-period=90d \
  --next-rotation-time=$(date -u -d "+90 days" +%Y-%m-%dT%H:%M:%SZ)

# Verify rotation schedule
gcloud kms keys describe ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING}
```

### Multi-Region Keys

Create multi-region keys for high availability:

```bash
# Create multi-region key ring
gcloud kms keyrings create firefly-keyring-multi \
  --location=us

# Create multi-region key
gcloud kms keys create firefly-encryption-key-multi \
  --location=us \
  --keyring=firefly-keyring-multi \
  --purpose=encryption
```

### HSM-Protected Keys

Create HSM-protected keys for enhanced security:

```bash
# Create HSM-protected key
gcloud kms keys create firefly-encryption-key-hsm \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --purpose=encryption \
  --protection-level=hsm
```

### Additional Authenticated Data (AAD)

Use AAD for enhanced security:

```java
Map<String, String> encryptionContext = Map.of(
    "tenantId", "tenant-123",
    "environment", "production"
);
```

## Monitoring

### Cloud Logging

View KMS operations in Cloud Logging:

```bash
# View recent KMS operations
gcloud logging read "resource.type=cloudkms_cryptokey" \
  --limit=50 \
  --format=json
```

### Cloud Monitoring

Create alerts for KMS operations:

```bash
# Create alert policy
gcloud alpha monitoring policies create \
  --notification-channels=CHANNEL_ID \
  --display-name="KMS High Error Rate" \
  --condition-display-name="Error rate > 5%" \
  --condition-threshold-value=0.05 \
  --condition-threshold-duration=300s
```

### Metrics

Monitor in Cloud Console:
- Request count
- Request latency
- Error rate
- Key versions

## Cost Optimization

### Pricing

- **Key versions**: $0.06 per month per active key version
- **Key operations**: $0.03 per 10,000 operations
- **HSM keys**: $2.50 per month per active key version

### Optimization Tips

1. Use envelope encryption (reduces KMS API calls)
2. Destroy old key versions
3. Use software keys (not HSM) for non-sensitive data
4. Use regional keys instead of global when possible

```bash
# Destroy old key version
gcloud kms keys versions destroy 1 \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --key=${KEY_NAME}
```

## Security Best Practices

### 1. Use Least Privilege

```bash
# Grant only encrypt/decrypt, not admin
gcloud kms keys add-iam-policy-binding ${KEY_NAME} \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --member="serviceAccount:firefly-security-vault@${PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/cloudkms.cryptoKeyEncrypterDecrypter"
```

### 2. Enable Audit Logging

```bash
# Enable data access logs
gcloud projects get-iam-policy ${PROJECT_ID} \
  --format=json > policy.json

# Edit policy.json to add auditConfigs

gcloud projects set-iam-policy ${PROJECT_ID} policy.json
```

### 3. Use VPC Service Controls

```bash
# Create service perimeter
gcloud access-context-manager perimeters create firefly-perimeter \
  --title="Firefly Security Vault Perimeter" \
  --resources=projects/${PROJECT_ID} \
  --restricted-services=cloudkms.googleapis.com
```

### 4. Separate Keys by Environment

```bash
# Dev key
gcloud kms keys create firefly-encryption-key-dev \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --purpose=encryption

# Prod key
gcloud kms keys create firefly-encryption-key-prod \
  --location=${LOCATION} \
  --keyring=${KEY_RING} \
  --purpose=encryption
```

## Troubleshooting

### Error: Permission denied

**Cause**: Insufficient IAM permissions

**Solution**: Grant `roles/cloudkms.cryptoKeyEncrypterDecrypter` role

### Error: Key not found

**Cause**: Wrong project, location, or key name

**Solution**: Verify configuration matches GCP resources

### Error: Authentication failed

**Cause**: Invalid service account credentials

**Solution**: Verify credentials file path and permissions

## Next Steps

- [Configuration Overview](README.md)
- [AWS KMS Configuration](aws-kms.md)
- [Deployment Guide](../operations/deployment.md)

