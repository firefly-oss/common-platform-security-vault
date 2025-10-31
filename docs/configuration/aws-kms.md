# AWS KMS Configuration

## Overview

AWS Key Management Service (KMS) is a managed service that makes it easy to create and control cryptographic keys. This guide shows how to configure the Firefly Security Vault to use AWS KMS.

## Prerequisites

- AWS Account with KMS access
- IAM permissions to use KMS keys
- AWS SDK credentials configured

## Step 1: Create KMS Key

### Using AWS Console

1. Navigate to **AWS KMS** in the AWS Console
2. Click **Create key**
3. Select **Symmetric** key type
4. Choose **Encrypt and decrypt** key usage
5. Configure key settings:
   - **Alias**: `firefly-security-vault-prod`
   - **Description**: `Encryption key for Firefly Security Vault`
6. Define key administrative permissions
7. Define key usage permissions (grant to your application's IAM role)
8. Review and create

### Using AWS CLI

```bash
# Create the key
aws kms create-key \
  --description "Firefly Security Vault Encryption Key" \
  --key-usage ENCRYPT_DECRYPT \
  --origin AWS_KMS

# Create an alias
aws kms create-alias \
  --alias-name alias/firefly-security-vault-prod \
  --target-key-id <key-id-from-previous-command>

# Enable automatic key rotation
aws kms enable-key-rotation \
  --key-id <key-id>
```

## Step 2: Configure IAM Permissions

Create an IAM policy for your application:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowKMSEncryptDecrypt",
      "Effect": "Allow",
      "Action": [
        "kms:Encrypt",
        "kms:Decrypt",
        "kms:GenerateDataKey",
        "kms:DescribeKey"
      ],
      "Resource": "arn:aws:kms:us-east-1:123456789012:key/*"
    }
  ]
}
```

Attach this policy to your application's IAM role or user.

## Step 3: Add Maven Dependency

Add the AWS KMS SDK to your `pom.xml`:

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>kms</artifactId>
    <version>2.20.0</version>
</dependency>
```

## Step 4: Configure Application

### application.yaml

```yaml
firefly:
  security:
    vault:
      encryption:
        # Set provider to AWS_KMS
        provider: AWS_KMS
        
        # KMS key ARN or alias
        master-key-id: arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012
        # OR use alias:
        # master-key-id: alias/firefly-security-vault-prod
        
        # AWS-specific configuration
        aws-kms:
          # AWS region (required)
          region: us-east-1
          
          # Endpoint override (optional, for testing with LocalStack)
          # endpoint-override: http://localhost:4566
```

### Environment Variables

Alternatively, use environment variables:

```bash
# Provider configuration
export FIREFLY_SECURITY_VAULT_ENCRYPTION_PROVIDER=AWS_KMS
export FIREFLY_SECURITY_VAULT_ENCRYPTION_MASTER_KEY_ID=arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012

# AWS region
export FIREFLY_SECURITY_VAULT_ENCRYPTION_AWS_KMS_REGION=us-east-1

# AWS credentials (if not using IAM role)
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_SESSION_TOKEN=your-session-token  # Optional, for temporary credentials
```

## Step 5: Configure AWS Credentials

### Option 1: IAM Role (Recommended for EC2/ECS/EKS)

When running on AWS infrastructure, use IAM roles:

```yaml
# No additional configuration needed
# The AWS SDK will automatically use the instance/task role
```

### Option 2: AWS Credentials File

Create `~/.aws/credentials`:

```ini
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
```

Create `~/.aws/config`:

```ini
[default]
region = us-east-1
```

### Option 3: Environment Variables

```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_DEFAULT_REGION=us-east-1
```

## Configuration Options

### Full Configuration Example

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AWS_KMS
        master-key-id: arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012
        
        aws-kms:
          # AWS region (required)
          region: us-east-1
          
          # Endpoint override (optional)
          # Use for LocalStack or custom endpoints
          endpoint-override: http://localhost:4566
          
          # Connection timeout in seconds (optional, default: 30)
          connection-timeout: 30
          
          # Request timeout in seconds (optional, default: 60)
          request-timeout: 60
```

## Features

### Envelope Encryption

AWS KMS uses envelope encryption:

1. **Data Key Generation**: Generate a unique data encryption key (DEK) for each credential
2. **Encrypt Credential**: Encrypt the credential with the DEK
3. **Encrypt DEK**: Encrypt the DEK with the KMS master key
4. **Store Both**: Store both the encrypted credential and encrypted DEK

**Benefits**:
- Reduced KMS API calls
- Better performance
- Lower costs

### Automatic Key Rotation

Enable automatic key rotation:

```bash
aws kms enable-key-rotation --key-id <key-id>
```

AWS automatically rotates the key material every year while keeping the same key ID.

### Encryption Context

Add additional authenticated data (AAD) for enhanced security:

```java
Map<String, String> encryptionContext = Map.of(
    "tenantId", "tenant-123",
    "environment", "production",
    "credentialType", "API_KEY"
);
```

The encryption context is logged in CloudTrail for audit purposes.

## Testing

### LocalStack

Test with LocalStack for local development:

1. **Start LocalStack**:
   ```bash
   docker run -d -p 4566:4566 localstack/localstack
   ```

2. **Create test key**:
   ```bash
   aws --endpoint-url=http://localhost:4566 kms create-key
   ```

3. **Configure endpoint override**:
   ```yaml
   firefly:
     security:
       vault:
         encryption:
           aws-kms:
             endpoint-override: http://localhost:4566
   ```

## Monitoring

### CloudWatch Metrics

AWS KMS automatically publishes metrics to CloudWatch:

- `NumberOfCalls` - Total API calls
- `UserErrorCount` - Client-side errors
- `SystemErrorCount` - Server-side errors
- `Throttles` - Throttled requests

### CloudTrail Logging

All KMS API calls are logged to CloudTrail:

```json
{
  "eventName": "Decrypt",
  "requestParameters": {
    "keyId": "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012",
    "encryptionContext": {
      "tenantId": "tenant-123",
      "environment": "production"
    }
  }
}
```

## Cost Optimization

### KMS Pricing

- **Key storage**: $1/month per key
- **API requests**: $0.03 per 10,000 requests

### Optimization Tips

1. **Use Envelope Encryption**: Reduces KMS API calls
2. **Cache Data Keys**: Reuse data keys when appropriate
3. **Use Aliases**: Easier key rotation without changing configuration
4. **Monitor Usage**: Set up CloudWatch alarms for unexpected usage

## Troubleshooting

### Error: AccessDeniedException

**Cause**: Insufficient IAM permissions

**Solution**: Verify IAM policy includes required KMS actions

### Error: NotFoundException

**Cause**: Key ID not found or incorrect region

**Solution**: Verify key ARN and region configuration

### Error: DisabledException

**Cause**: KMS key is disabled

**Solution**: Enable the key in AWS Console or CLI:
```bash
aws kms enable-key --key-id <key-id>
```

## Security Best Practices

1. **Use IAM Roles**: Prefer IAM roles over access keys
2. **Enable Key Rotation**: Automatically rotate keys annually
3. **Use Encryption Context**: Add AAD for audit trails
4. **Restrict Key Access**: Grant least privilege permissions
5. **Monitor Usage**: Set up CloudWatch alarms
6. **Use Separate Keys**: Different keys for dev/staging/prod

## Next Steps

- [Configuration Overview](README.md)
- [Azure Key Vault Configuration](azure-key-vault.md)
- [Deployment Guide](../operations/deployment.md)

