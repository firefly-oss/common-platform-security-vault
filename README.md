<!--
Copyright 2025 Firefly Software Solutions Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<div align="center">

<img src="https://via.placeholder.com/150x150/4A90E2/FFFFFF?text=FV" alt="Firefly Vault Logo" width="150" height="150">

# Firefly Security Vault

### Enterprise-Grade Secrets Management Microservice

**Secure • Scalable • Production-Ready**

---

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/firefly-oss/common-platform-security-vault)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.2.2-green.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/architecture-hexagonal-blueviolet.svg)](docs/architecture/hexagonal-architecture.md)
[![Tests](https://img.shields.io/badge/tests-94%20passing-success.svg)](common-platform-security-vault-core/src/test)
[![Coverage](https://img.shields.io/badge/coverage-85%25-green.svg)](common-platform-security-vault-core/src/test)

[![Website](https://img.shields.io/badge/website-getfirefly.io-blue)](https://getfirefly.io)
[![Documentation](https://img.shields.io/badge/docs-comprehensive-brightgreen)](./docs)
[![API Docs](https://img.shields.io/badge/API-OpenAPI%203.0-85EA2D)](http://localhost:8081/swagger-ui.html)

[🚀 Quick Start](#-quick-start-5-minutes) • [📖 Documentation](#-documentation) • [🔌 API Reference](#-api-reference) • [💬 Community](#-support--community)

</div>

---

## 📑 Table of Contents

### Getting Started
- [What is Firefly Security Vault?](#-what-is-firefly-security-vault)
- [Key Features](#-key-features)
- [Quick Start (5 Minutes)](#-quick-start-5-minutes)
- [Step-by-Step Tutorials](#-step-by-step-tutorials)

### Architecture & Design
- [Architecture Overview](#-architecture-deep-dive)
- [Technology Stack](#-technology-stack)
- [Data Model](#-data-model)
- [Module Structure](#module-structure)

### Production Deployment
- [Production Deployment Guide](#-production-deployment-guide)
  - [AWS KMS Setup](#1-aws-kms-recommended)
  - [Azure Key Vault Setup](#2-azure-key-vault)
  - [HashiCorp Vault Setup](#3-hashicorp-vault)
  - [Google Cloud KMS Setup](#4-google-cloud-kms)
- [Best Practices](#production-best-practices)
- [Monitoring & Observability](#-monitoring--observability)

### API & Integration
- [API Reference](#-api-reference)
- [Java SDK Usage](#-java-sdk-usage)
- [Real-World Use Cases](#-real-world-use-cases)

### Security & Compliance
- [Security & Compliance](#-security--compliance)
- [Encryption Details](#encryption-details)
- [Access Control](#access-control-matrix)
- [Audit Trail](#audit-trail)

### Additional Resources
- [FAQ](#-frequently-asked-questions-faq)
- [Contributing](#-contributing)
- [License](#-license)
- [Support & Community](#-support--community)
- [Roadmap](#-roadmap)

---

## 🔐 What is Firefly Security Vault?

**Firefly Security Vault** is an enterprise-grade, production-ready microservice designed to centralize and secure the management of sensitive credentials across your entire infrastructure. Built with **Hexagonal Architecture** and **reactive programming**, it provides a robust, scalable solution for storing, rotating, and auditing access to secrets.

### 🎯 The Problem We Solve

Modern distributed systems face critical challenges in credential management:

```
❌ Hardcoded secrets in source code
❌ Credentials scattered across multiple systems
❌ No audit trail of who accessed what
❌ Manual rotation processes prone to errors
❌ Lack of encryption at rest
❌ No centralized access control
❌ Compliance and regulatory risks
```

### ✅ Our Solution

Firefly Security Vault provides a **single source of truth** for all your sensitive credentials:

```
✅ Centralized credential storage with AES-256-GCM encryption
✅ Pluggable KMS providers (AWS, Azure, Google Cloud, HashiCorp)
✅ Automatic rotation with configurable policies
✅ Complete audit trail for compliance (SOC 2, PCI-DSS, GDPR)
✅ Fine-grained access control (IP, service, environment)
✅ Production-ready resilience (Circuit Breaker, Retry, Rate Limiter)
✅ Multi-tenant support with complete isolation
✅ Reactive, non-blocking architecture for high performance
```

### 🏢 Who Should Use This?

| Use Case | Example |
|----------|---------|
| **Financial Services** | Secure API keys for payment gateways (Stripe, PayPal), banking APIs |
| **SaaS Platforms** | OAuth tokens, database credentials, third-party integrations |
| **E-commerce** | Payment processor credentials, shipping API keys |
| **Healthcare** | HIPAA-compliant credential storage for medical systems |
| **Enterprise IT** | Centralized secret management for microservices architecture |
| **DevOps Teams** | Secure CI/CD pipeline credentials, infrastructure secrets |

### 🏗️ Architecture at a Glance

This microservice is built using **Hexagonal Architecture** (Ports and Adapters):

```
┌─────────────────────────────────────────────────────────────┐
│                    YOUR APPLICATIONS                        │
│  (Banking Services, Payment Services, Analytics, etc.)      │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ REST API / SDK
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              FIREFLY SECURITY VAULT MICROSERVICE            │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  REST API Layer (Spring WebFlux)                    │    │
│  │  • Credential CRUD                                  │    │
│  │  • Rotation Management                              │    │
│  │  • Access Control                                   │    │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Business Logic (Domain Services)                   │    │
│  │  • Encryption/Decryption                            │    │
│  │  • Access Validation                                │    │
│  │  • Audit Logging                                    │    │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Adapters (Pluggable KMS Providers)                 │    │
│  │  • AWS KMS  • Azure Key Vault  • HashiCorp Vault    │    │
│  │  • Google Cloud KMS  • In-Memory (dev/test)         │    │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Data Layer (PostgreSQL + R2DBC)                    │    │
│  │  • Encrypted credentials                            │    │
│  │  • Audit logs                                       │    │
│  │  • Version history                                  │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### 🔒 Enterprise-Grade Security

<table>
<tr>
<td width="50%">

**Encryption at Rest**
- AES-256-GCM encryption
- Unique 12-byte IV per operation
- 128-bit authentication tags
- Envelope encryption support

</td>
<td width="50%">

**Key Management**
- AWS KMS integration
- Azure Key Vault integration
- HashiCorp Vault integration
- Google Cloud KMS integration
- In-memory provider (dev/test)

</td>
</tr>
<tr>
<td>

**Access Control**
- IP address whitelisting
- Service-based restrictions
- Environment isolation
- Approval workflows
- Rate limiting

</td>
<td>

**Compliance & Audit**
- Complete access audit trail
- Failed access attempt logging
- Performance metrics tracking
- Retention policy support
- SOC 2, PCI-DSS, GDPR ready

</td>
</tr>
</table>

### 🔄 Lifecycle Management

| Feature | Description | Status |
|---------|-------------|--------|
| **Automatic Rotation** | Policy-driven rotation schedules (e.g., every 90 days) | ✅ Production |
| **Manual Rotation** | On-demand rotation with reason tracking | ✅ Production |
| **Version History** | Complete history of all credential versions | ✅ Production |
| **Rollback Support** | Restore previous credential versions | ✅ Production |
| **Expiration Alerts** | Configurable warnings before expiration | ✅ Production |
| **Multi-Version Support** | Keep configurable number of versions | ✅ Production |

### 🏗️ Architecture Excellence

| Aspect | Implementation | Benefit |
|--------|----------------|---------|
| **Hexagonal Architecture** | Ports & Adapters pattern | Switch KMS providers without code changes |
| **Reactive Programming** | Spring WebFlux + R2DBC | Non-blocking, high-throughput operations |
| **Resilience Patterns** | Resilience4j (Circuit Breaker, Retry, Rate Limiter) | Fault-tolerant, production-ready |
| **Multi-Module Design** | Interfaces, Core, Web, SDK | Clean separation of concerns |
| **Dependency Injection** | Spring Boot 3.2.2 | Testable, maintainable codebase |

### 📊 Observability & Monitoring

```yaml
✅ Prometheus Metrics       # Request rates, latencies, error rates
✅ Health Checks            # Database, KMS provider connectivity
✅ Structured Logging       # JSON logs with correlation IDs
✅ Distributed Tracing      # OpenTelemetry ready
✅ Custom Dashboards        # Grafana dashboard templates included
✅ Alerting Rules           # Pre-configured Prometheus alerts
```

### 🚀 Developer Experience

- **Auto-Generated SDK**: OpenAPI 3.0 spec → Java SDK (WebClient-based)
- **Interactive API Docs**: Swagger UI at `/swagger-ui.html`
- **Comprehensive Tests**: 94 unit tests with 85% coverage
- **Docker Support**: Multi-stage Dockerfile included
- **Kubernetes Ready**: Deployment manifests and Helm charts
- **CI/CD Examples**: GitHub Actions, GitLab CI templates

### 🌐 Multi-Tenant & Multi-Environment

```
┌─────────────────────────────────────────────────────────┐
│  Tenant A                                               │
│  ├── Development   (separate credentials)               │
│  ├── Staging       (separate credentials)               │
│  └── Production    (separate credentials)               │
├─────────────────────────────────────────────────────────┤
│  Tenant B                                               │
│  ├── Development   (complete isolation)                 │
│  ├── Staging       (complete isolation)                 │
│  └── Production    (complete isolation)                 │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Why Choose Firefly Security Vault?

### vs. HashiCorp Vault

| Feature | Firefly Security Vault | HashiCorp Vault |
|---------|------------------------|-----------------|
| **Setup Complexity** | ✅ Simple (Spring Boot app) | ⚠️ Complex (cluster setup) |
| **KMS Integration** | ✅ Native (AWS, Azure, GCP, HashiCorp) | ⚠️ Requires plugins |
| **Banking-Specific** | ✅ Built for financial services | ❌ General purpose |
| **Java SDK** | ✅ Auto-generated from OpenAPI | ⚠️ Community-maintained |
| **Reactive Support** | ✅ Native (Spring WebFlux) | ❌ Blocking I/O |
| **Cost** | ✅ Open Source (Apache 2.0) | ⚠️ Enterprise features paid |

### vs. AWS Secrets Manager

| Feature | Firefly Security Vault | AWS Secrets Manager |
|---------|------------------------|---------------------|
| **Multi-Cloud** | ✅ AWS, Azure, GCP, HashiCorp | ❌ AWS only |
| **Self-Hosted** | ✅ Full control | ❌ AWS managed only |
| **Customization** | ✅ Fully customizable | ❌ Limited |
| **Audit Trail** | ✅ Custom audit logic | ✅ CloudTrail |
| **Cost** | ✅ Infrastructure only | ⚠️ Per secret + API calls |
| **Vendor Lock-in** | ✅ None | ❌ AWS locked |

### vs. Azure Key Vault

| Feature | Firefly Security Vault | Azure Key Vault |
|---------|------------------------|-----------------|
| **Multi-Cloud** | ✅ AWS, Azure, GCP, HashiCorp | ❌ Azure only |
| **Banking Features** | ✅ Rotation, versioning, approval | ⚠️ Basic features |
| **Custom Logic** | ✅ Full control | ❌ Limited |
| **Open Source** | ✅ Apache 2.0 | ❌ Proprietary |
| **Cost** | ✅ Infrastructure only | ⚠️ Per operation |

## 🚀 Quick Start (5 Minutes)

Get the microservice running locally in 5 minutes for development and testing.

### Prerequisites

| Requirement | Version | Download |
|------------|---------|----------|
| **Java** | 21+ | [OpenJDK 21](https://openjdk.org/projects/jdk/21/) |
| **PostgreSQL** | 14+ | [PostgreSQL](https://www.postgresql.org/download/) |
| **Maven** | 3.8+ | [Maven](https://maven.apache.org/download.cgi) |

### Step 1: Clone the Repository

```bash
git clone https://github.com/firefly-oss/common-platform-security-vault.git
cd common-platform-security-vault
```

### Step 2: Setup Database

Create a PostgreSQL database and user:

```sql
-- Connect to PostgreSQL as admin
psql -U postgres

-- Create database and user
CREATE DATABASE firefly_security_vault;
CREATE USER firefly_user WITH PASSWORD 'firefly_password';
GRANT ALL PRIVILEGES ON DATABASE firefly_security_vault TO firefly_user;

-- Exit psql
\q
```

### Step 3: Configure Application

Create `common-platform-security-vault-web/src/main/resources/application-local.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/firefly_security_vault
    username: firefly_user
    password: firefly_password

firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY  # For local development only
        master-key-id: local-dev-key
      audit:
        enabled: true
```

### Step 4: Build and Run

```bash
# Build the entire project
mvn clean install

# Run the microservice
cd common-platform-security-vault-web
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Step 5: Verify It's Working

Once the service starts, you should see:

```
Started SecurityVaultApplication in X.XXX seconds
```

Test the endpoints:

| Endpoint | URL | Expected Response |
|----------|-----|-------------------|
| **Health Check** | http://localhost:8081/actuator/health | `{"status":"UP"}` |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | Interactive API docs |
| **Metrics** | http://localhost:8081/actuator/metrics | Prometheus metrics |

### Step 6: Create Your First Credential

Using curl:

```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MY_FIRST_SECRET",
    "name": "My First Secret",
    "description": "Testing the vault",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "my-secret-value"
  }'
```

Or use Swagger UI at http://localhost:8081/swagger-ui.html

**🎉 Congratulations!** Your Security Vault microservice is running locally.

---

## 📚 Step-by-Step Tutorials

### Tutorial 1: Storing and Retrieving API Keys

**Scenario**: You need to store a Stripe API key for your payment service.

**Step 1**: Create the credential

```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STRIPE_API_KEY_PROD",
    "name": "Stripe Production API Key",
    "description": "Stripe API key for production payments",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "sk_live_51H...",
    "rotationEnabled": true,
    "autoRotationDays": 90,
    "expiresAt": "2025-12-31T23:59:59Z"
  }'
```

**Step 2**: Retrieve the credential

```bash
# Get by ID
curl http://localhost:8081/api/v1/credentials/{credential-id}

# Or filter by code
curl -X POST http://localhost:8081/api/v1/credentials/filter \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STRIPE_API_KEY_PROD"
  }'
```

**Step 3**: Decrypt the value (in your application)

```java
// Using the auto-generated SDK
import com.firefly.common.security.vault.sdk.api.CredentialDecryptionApi;
import com.firefly.common.security.vault.sdk.invoker.ApiClient;
import java.util.UUID;

ApiClient apiClient = new ApiClient();
apiClient.setBasePath("http://localhost:8081");

CredentialDecryptionApi decryptionApi = new CredentialDecryptionApi(apiClient);

// Get credential ID first (or store it when creating the credential)
UUID credentialId = UUID.fromString("your-credential-id");

// Decrypt the credential
String decryptedValue = decryptionApi.decryptCredential(credentialId, "Accessing Stripe API", null)
    .block(); // For blocking call, or use .subscribe() for reactive
```

### Tutorial 2: Setting Up Automatic Rotation

**Scenario**: Rotate database passwords every 30 days automatically.

**Step 1**: Create a rotation policy

```bash
curl -X POST http://localhost:8081/api/v1/credential-rotation-policies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Database Password Rotation",
    "description": "Rotate DB passwords every 30 days",
    "rotationIntervalDays": 30,
    "enabled": true,
    "autoRotate": true
  }'
```

**Step 2**: Create credential with rotation enabled

```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DB_ANALYTICS_PASSWORD",
    "name": "Analytics Database Password",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "current-password",
    "rotationEnabled": true,
    "autoRotationDays": 30
  }'
```

**Step 3**: Monitor rotation history

```bash
# View version history
curl http://localhost:8081/api/v1/credential-versions?credentialId={id}
```

### Tutorial 3: Implementing Access Control

**Scenario**: Restrict a credential to specific services and IP addresses.

```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PAYMENT_GATEWAY_KEY",
    "name": "Payment Gateway API Key",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "secret-key",
    "allowedServices": "payment-service,billing-service",
    "allowedIps": "10.0.1.0/24,10.0.2.100",
    "allowedEnvironments": "PRODUCTION",
    "requireApprovalForAccess": true,
    "auditAllAccess": true
  }'
```

**Access Control Features**:
- ✅ Only `payment-service` and `billing-service` can access
- ✅ Only from IPs in `10.0.1.0/24` or `10.0.2.100`
- ✅ Only in `PRODUCTION` environment
- ✅ Requires approval before access
- ✅ All access attempts are logged

---

## 🏭 Production Deployment Guide

This section provides complete guidance for deploying the Security Vault microservice to production.

### Production Checklist

Before deploying to production, ensure you have:

- [ ] Chosen a KMS provider (AWS KMS, Azure Key Vault, HashiCorp Vault, or Google Cloud KMS)
- [ ] Created encryption keys in your KMS provider
- [ ] Configured database with proper credentials and connection pooling
- [ ] Set up monitoring and alerting
- [ ] Configured resilience patterns (Circuit Breaker, Rate Limiter, Retry)
- [ ] Enabled audit logging
- [ ] Configured TLS/SSL certificates
- [ ] Set up backup and disaster recovery
- [ ] Reviewed security best practices

### Option 1: Deploy with AWS KMS

**Best for**: Applications running on AWS (EC2, ECS, EKS, Lambda)

#### Step 1: Create KMS Key in AWS

```bash
# Using AWS CLI
aws kms create-key \
  --description "Firefly Security Vault Master Key" \
  --key-usage ENCRYPT_DECRYPT \
  --origin AWS_KMS

# Note the KeyId from the response
# Example: arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012
```

#### Step 2: Create IAM Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
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

#### Step 3: Configure Application

Create `application-prod.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 30m

firefly:
  security:
    vault:
      encryption:
        provider: AWS_KMS
        master-key-id: ${AWS_KMS_KEY_ARN}
        aws-kms:
          region: ${AWS_REGION}
          # Optional: Use specific credentials
          # access-key-id: ${AWS_ACCESS_KEY_ID}
          # secret-access-key: ${AWS_SECRET_ACCESS_KEY}
      audit:
        enabled: true
        log-decryption: true
        log-failed-access: true

# Resilience Configuration
resilience4j:
  circuitbreaker:
    instances:
      kms-operations:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        sliding-window-size: 10
        minimum-number-of-calls: 5
  ratelimiter:
    instances:
      kms-operations:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 5s
  retry:
    instances:
      kms-operations:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2.0

# Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Step 4: Deploy

**Using Docker**:

```bash
# Build Docker image
docker build -t firefly-security-vault:latest .

# Run container
docker run -d \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_NAME=firefly_security_vault \
  -e DB_USERNAME=firefly_user \
  -e DB_PASSWORD=your-password \
  -e AWS_KMS_KEY_ARN=arn:aws:kms:us-east-1:123456789012:key/... \
  -e AWS_REGION=us-east-1 \
  firefly-security-vault:latest
```

**Using Kubernetes**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: firefly-security-vault
spec:
  replicas: 3
  selector:
    matchLabels:
      app: firefly-security-vault
  template:
    metadata:
      labels:
        app: firefly-security-vault
    spec:
      serviceAccountName: firefly-vault-sa  # With IAM role for KMS access
      containers:
      - name: vault
        image: firefly-security-vault:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: vault-secrets
              key: db-host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: vault-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: vault-secrets
              key: db-password
        - name: AWS_KMS_KEY_ARN
          value: "arn:aws:kms:us-east-1:123456789012:key/..."
        - name: AWS_REGION
          value: "us-east-1"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Option 2: Deploy with Azure Key Vault

**Best for**: Applications running on Azure (VMs, AKS, Azure Functions)

#### Step 1: Create Key Vault and Key

```bash
# Create resource group
az group create --name firefly-rg --location eastus

# Create Key Vault
az keyvault create \
  --name firefly-vault \
  --resource-group firefly-rg \
  --location eastus

# Create encryption key
az keyvault key create \
  --vault-name firefly-vault \
  --name firefly-encryption-key \
  --kty RSA \
  --size 2048
```

#### Step 2: Create Service Principal

```bash
# Create service principal
az ad sp create-for-rbac \
  --name firefly-vault-sp \
  --role "Key Vault Crypto User" \
  --scopes /subscriptions/{subscription-id}/resourceGroups/firefly-rg/providers/Microsoft.KeyVault/vaults/firefly-vault

# Note the output:
# - appId (client-id)
# - password (client-secret)
# - tenant
```

#### Step 3: Configure Application

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: AZURE_KEY_VAULT
        azure-key-vault:
          vault-url: https://firefly-vault.vault.azure.net/
          key-name: firefly-encryption-key
          tenant-id: ${AZURE_TENANT_ID}
          client-id: ${AZURE_CLIENT_ID}
          client-secret: ${AZURE_CLIENT_SECRET}
```

### Option 3: Deploy with HashiCorp Vault

**Best for**: On-premise deployments or hybrid cloud

#### Step 1: Setup HashiCorp Vault

```bash
# Enable transit secrets engine
vault secrets enable transit

# Create encryption key
vault write -f transit/keys/firefly-encryption-key

# Create policy
vault policy write firefly-vault - <<EOF
path "transit/encrypt/firefly-encryption-key" {
  capabilities = ["update"]
}
path "transit/decrypt/firefly-encryption-key" {
  capabilities = ["update"]
}
path "transit/datakey/plaintext/firefly-encryption-key" {
  capabilities = ["update"]
}
EOF

# Create token
vault token create -policy=firefly-vault
```

#### Step 2: Configure Application

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

### Option 4: Deploy with Google Cloud KMS

**Best for**: Applications running on Google Cloud (GCE, GKE, Cloud Functions)

#### Step 1: Create KMS Key

```bash
# Create key ring
gcloud kms keyrings create firefly-keyring \
  --location us-east1

# Create encryption key
gcloud kms keys create firefly-encryption-key \
  --location us-east1 \
  --keyring firefly-keyring \
  --purpose encryption
```

#### Step 2: Grant Permissions

```bash
# Grant service account permissions
gcloud kms keys add-iam-policy-binding firefly-encryption-key \
  --location us-east1 \
  --keyring firefly-keyring \
  --member serviceAccount:firefly-vault@project-id.iam.gserviceaccount.com \
  --role roles/cloudkms.cryptoKeyEncrypterDecrypter
```

#### Step 3: Configure Application

```yaml
firefly:
  security:
    vault:
      encryption:
        provider: GOOGLE_CLOUD_KMS
        google-cloud-kms:
          project-id: ${GCP_PROJECT_ID}
          location: us-east1
          key-ring: firefly-keyring
          key-name: firefly-encryption-key
          # Optional: Use service account key file
          # credentials-path: /path/to/service-account-key.json
```

### Production Best Practices

#### 1. Database Configuration

```yaml
spring:
  r2dbc:
    pool:
      initial-size: 10        # Start with 10 connections
      max-size: 50            # Max 50 connections
      max-idle-time: 30m      # Close idle connections after 30 minutes
      max-acquire-time: 3s    # Timeout for acquiring connection
      max-create-connection-time: 5s
      validation-query: SELECT 1
```

#### 2. Enable TLS/SSL

```yaml
server:
  port: 8081
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: firefly-vault
```

#### 3. Configure Logging

```yaml
logging:
  level:
    com.firefly.common.security.vault: INFO
    org.springframework.r2dbc: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/firefly-vault/application.log
    max-size: 100MB
    max-history: 30
```

#### 4. Set Up Monitoring

**Prometheus Scrape Config**:

```yaml
scrape_configs:
  - job_name: 'firefly-vault'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['firefly-vault:8081']
```

**Grafana Dashboard**: Import dashboard ID `12345` (create custom dashboard)

#### 5. Configure Alerts

**Example Prometheus Alert Rules**:

```yaml
groups:
  - name: firefly-vault
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate in Firefly Vault"

      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{state="open"} == 1
        for: 1m
        annotations:
          summary: "Circuit breaker is open - KMS may be down"

      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        annotations:
          summary: "95th percentile latency > 1 second"
```

### Scaling Considerations

| Metric | Recommendation |
|--------|----------------|
| **CPU** | 2-4 cores per instance |
| **Memory** | 1-2 GB per instance |
| **Replicas** | Minimum 3 for high availability |
| **Database Connections** | 50 per instance (adjust based on load) |
| **Rate Limit** | 100 requests/second per instance |

### Disaster Recovery

1. **Database Backups**: Daily automated backups with 30-day retention
2. **KMS Key Backup**: Enable automatic key rotation and backup in KMS provider
3. **Configuration Backup**: Store configuration in version control
4. **Monitoring**: Set up alerts for service degradation
5. **Runbook**: Document recovery procedures

---

## 🏗️ Architecture Deep Dive

### Hexagonal Architecture (Ports and Adapters)

The microservice uses **Hexagonal Architecture** to achieve:
- **Provider Independence**: Switch KMS providers without changing business logic
- **Testability**: Easy to mock and test in isolation
- **Maintainability**: Clear separation of concerns

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│                   (REST Controllers)                        │
│                                                             │
│  /api/v1/credentials  /api/v1/rotation  /api/v1/audit       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                         │
│                  (Business Services)                        │
│                                                             │
│  CredentialService  RotationService  AccessControlService   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│                  (Ports - Interfaces)                       │
│                                                             │
│  KeyManagementPort ◄───────┐                                │
│  CredentialEncryptionPort  │  Business logic depends        │
│                            │  on interfaces, not            │
│                            │  implementations               │
└────────────────────────────┼────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                        │
│              (Adapters - Implementations)                   │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ AWS KMS      │  │ Azure Key    │  │ HashiCorp    │       │
│  │ Adapter      │  │ Vault Adapter│  │ Vault Adapter│       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Google Cloud │  │ In-Memory    │  │ Resilient    │       │
│  │ KMS Adapter  │  │ Adapter      │  │ Wrapper      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Module Structure

```
common-platform-security-vault/
│
├── common-platform-security-vault-models/
│   ├── entities/           # JPA/R2DBC entities
│   ├── repositories/       # R2DBC repositories
│   └── migrations/         # Flyway SQL scripts
│
├── common-platform-security-vault-interfaces/
│   ├── dto/               # Data Transfer Objects
│   ├── requests/          # API request models
│   └── responses/         # API response models
│
├── common-platform-security-vault-core/
│   ├── ports/             # Domain interfaces
│   │   ├── KeyManagementPort.java
│   │   └── CredentialEncryptionPort.java
│   │
│   ├── adapters/          # Infrastructure implementations
│   │   ├── AwsKmsKeyManagementAdapter.java
│   │   ├── AzureKeyVaultKeyManagementAdapter.java
│   │   ├── HashiCorpVaultKeyManagementAdapter.java
│   │   ├── GoogleCloudKmsKeyManagementAdapter.java
│   │   ├── InMemoryKeyManagementAdapter.java
│   │   ├── ResilientKeyManagementAdapter.java  # Resilience wrapper
│   │   └── AesGcmCredentialEncryptionAdapter.java
│   │
│   ├── services/          # Business logic
│   │   ├── impl/
│   │   │   ├── CredentialServiceImpl.java
│   │   │   ├── RotationServiceImpl.java
│   │   │   └── AccessControlServiceImpl.java
│   │   └── interfaces/
│   │
│   ├── config/            # Spring configuration
│   │   ├── KmsProviderConfiguration.java
│   │   ├── ResilienceConfiguration.java
│   │   └── SecurityVaultConfigurationValidator.java
│   │
│   ├── health/            # Health indicators
│   │   └── KeyManagementHealthIndicator.java
│   │
│   └── metrics/           # Metrics collectors
│       └── KeyManagementMetrics.java
│
├── common-platform-security-vault-web/
│   ├── controllers/       # REST API endpoints
│   ├── exception/         # Exception handlers
│   └── SecurityVaultApplication.java  # Main class
│
└── common-platform-security-vault-sdk/
    ├── src/main/resources/api-spec/
    │   └── openapi.yml    # OpenAPI 3.0 specification
    └── target/generated-sources/
        └── src/gen/java/main/
            ├── api/       # Auto-generated API clients (CredentialsApi, CredentialDecryptionApi, etc.)
            ├── model/     # Auto-generated DTOs
            └── invoker/   # ApiClient and configuration
```

### Key Design Patterns

| Pattern | Purpose | Implementation |
|---------|---------|----------------|
| **Hexagonal Architecture** | Decouple business logic from infrastructure | Ports (interfaces) + Adapters (implementations) |
| **Decorator Pattern** | Add resilience to KMS operations | `ResilientKeyManagementAdapter` wraps other adapters |
| **Strategy Pattern** | Switch KMS providers at runtime | Different adapter implementations of `KeyManagementPort` |
| **Repository Pattern** | Abstract data access | R2DBC repositories for reactive database access |
| **DTO Pattern** | Separate API contracts from domain entities | DTOs in `interfaces` module, entities in `models` module |

### Resilience Patterns

The microservice implements three resilience patterns using **Resilience4j**:

#### 1. Circuit Breaker

**Purpose**: Prevent cascading failures when KMS is unavailable

**How it works**:
```
Normal State (CLOSED):
  Request → KMS → Success ✓

Failure State (OPEN):
  Request → Circuit Breaker → Fail Fast ✗
  (No call to KMS - prevents overload)

Recovery State (HALF_OPEN):
  Request → KMS → Test if recovered
  If success → CLOSED
  If failure → OPEN
```

**Configuration**:
- Failure threshold: 50% (opens after 50% of calls fail)
- Wait duration: 60 seconds (stays open for 60s before testing)
- Sliding window: 10 calls (evaluates last 10 calls)

#### 2. Rate Limiter

**Purpose**: Prevent exceeding KMS provider rate limits

**How it works**:
```
100 requests/second allowed
Request 1-100: ✓ Allowed
Request 101: ✗ Rejected (wait or fail)
After 1 second: Reset to 0
```

**Configuration**:
- Limit: 100 calls per second
- Timeout: 5 seconds (max wait time for permission)

#### 3. Retry

**Purpose**: Automatically retry transient failures

**How it works**:
```
Attempt 1: Network timeout → Retry
Wait 1 second...
Attempt 2: Network timeout → Retry
Wait 2 seconds... (exponential backoff)
Attempt 3: Success ✓
```

**Configuration**:
- Max attempts: 3
- Backoff: Exponential (1s, 2s, 4s)
- Retry on: RuntimeException, Exception

**Operator Order** (Critical!):
```java
Mono.defer(() -> delegate.encrypt(...))
    .transformDeferred(RetryOperator.of(retry))           // 1. Retry first
    .transformDeferred(RateLimiterOperator.of(rateLimiter)) // 2. Rate limit
    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker)) // 3. Circuit breaker
```

See [Resilience Patterns Documentation](docs/resilience-patterns.md) for details.

---

## 🛠️ Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 21 | Modern Java with virtual threads, pattern matching, records |
| **Framework** | Spring Boot | 3.2.2 | Application framework with auto-configuration |
| **Web** | Spring WebFlux | 3.2.2 | Reactive, non-blocking web framework |
| **Database** | PostgreSQL | 14+ | ACID-compliant relational database |
| **DB Access** | R2DBC | Latest | Reactive database connectivity |
| **Migrations** | Flyway | Latest | Database version control and migrations |
| **Mapping** | MapStruct | Latest | Compile-time DTO-Entity mapping |
| **Build** | Maven | 3.8+ | Dependency management and build automation |
| **Encryption** | AES-256-GCM | - | Authenticated encryption with associated data |
| **KMS SDKs** | AWS, Azure, HashiCorp, Google | Latest | Cloud provider KMS integrations |
| **Resilience** | Resilience4j | Latest | Circuit Breaker, Rate Limiter, Retry |
| **Metrics** | Micrometer | Latest | Vendor-neutral metrics facade |
| **Monitoring** | Spring Boot Actuator | 3.2.2 | Health checks and operational endpoints |
| **Logging** | SLF4J + Logback | Latest | Structured logging |
| **Testing** | JUnit 5 + Mockito + Reactor Test | Latest | Unit and integration testing |
| **API Docs** | SpringDoc OpenAPI | Latest | Automatic Swagger/OpenAPI generation |

---

## 📊 Data Model

The microservice manages **10 core entities** with complete CRUD operations:

### Entity Relationship Diagram

```
┌─────────────────┐         ┌──────────────────┐
│  Credential     │────────▶│  CredentialType  │
│                 │         └──────────────────┘
│  - id           │
│  - code         │         ┌──────────────────┐
│  - name         │────────▶│ CredentialStatus │
│  - encrypted    │         └──────────────────┘
│  - tenantId     │
│  - providerId   │         ┌──────────────────┐
│  - rotation     │────────▶│ EnvironmentType  │
│  - expiration   │         └──────────────────┘
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐         ┌──────────────────┐
│CredentialVersion│         │EncryptionKey     │
│                 │         │                  │
│  - version      │         │  - keyId         │
│  - encrypted    │         │  - algorithm     │
│  - rotatedAt    │         │  - provider      │
│  - rotatedBy    │         │  - status        │
└─────────────────┘         └──────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐         ┌──────────────────┐
│CredentialAccess │         │CredentialShare   │
│      Log        │         │                  │
│                 │         │  - sharedWith    │
│  - accessedAt   │         │  - permissions   │
│  - userId       │         │  - expiresAt     │
│  - ipAddress    │         │  - approved      │
│  - success      │         └──────────────────┘
└─────────────────┘

┌─────────────────┐         ┌──────────────────┐
│CredentialRotation│        │CredentialAlert   │
│     Policy      │         │                  │
│                 │         │  - type          │
│  - interval     │         │  - severity      │
│  - autoRotate   │         │  - message       │
│  - enabled      │         │  - acknowledged  │
└─────────────────┘         └──────────────────┘
```

### Core Entities

#### 1. Credential (Main Entity)

Stores encrypted credentials with metadata.

**Key Fields**:
- `id` (UUID) - Primary key
- `code` (String) - Unique identifier (e.g., "STRIPE_API_KEY_PROD")
- `name` (String) - Human-readable name
- `encryptedValue` (String) - Base64-encoded encrypted value
- `tenantId` (UUID) - Multi-tenant isolation
- `providerId` (UUID) - External provider reference
- `credentialTypeId` (UUID) - Type of credential
- `credentialStatusId` (UUID) - Current status
- `environmentTypeId` (UUID) - Environment (dev/staging/prod)
- `rotationEnabled` (Boolean) - Enable automatic rotation
- `autoRotationDays` (Integer) - Rotation interval
- `expiresAt` (Timestamp) - Expiration date
- `allowedServices` (String) - Comma-separated service whitelist
- `allowedIps` (String) - Comma-separated IP whitelist
- `requireApprovalForAccess` (Boolean) - Approval workflow
- `maskInLogs` (Boolean) - Mask value in audit logs

#### 2. CredentialType

Defines types of credentials.

**Predefined Types**:
- `API_KEY` - REST API keys
- `OAUTH2_CLIENT` - OAuth 2.0 client credentials
- `JWT_TOKEN` - JWT signing keys
- `DATABASE_CREDENTIALS` - Database connection strings
- `TLS_CERTIFICATE` - TLS/SSL certificates
- `SSH_KEY` - SSH private keys
- `ENCRYPTION_KEY` - Encryption keys
- `WEBHOOK_SECRET` - Webhook signing secrets

#### 3. CredentialStatus

Tracks credential lifecycle states.

**States**:
- `ACTIVE` - Currently in use
- `INACTIVE` - Temporarily disabled
- `EXPIRED` - Past expiration date
- `REVOKED` - Permanently revoked
- `ROTATING` - Currently being rotated
- `COMPROMISED` - Security breach detected
- `PENDING_APPROVAL` - Awaiting approval

#### 4. EnvironmentType

Categorizes credentials by environment.

**Environments**:
- `DEVELOPMENT` - Local development
- `TESTING` - QA/Testing environment
- `STAGING` - Pre-production
- `PRODUCTION` - Live production
- `SANDBOX` - Isolated sandbox

#### 5. EncryptionKey

Metadata for encryption keys (not the actual keys).

**Key Fields**:
- `keyId` (String) - KMS key identifier
- `algorithm` (String) - Encryption algorithm (e.g., "AES-256-GCM")
- `provider` (String) - KMS provider (AWS_KMS, AZURE_KEY_VAULT, etc.)
- `status` (String) - ACTIVE, ROTATING, RETIRED
- `createdAt` (Timestamp) - Creation timestamp
- `rotatedAt` (Timestamp) - Last rotation

#### 6. CredentialVersion

Complete version history for rollback support.

**Key Fields**:
- `credentialId` (UUID) - Parent credential
- `version` (Integer) - Version number (1, 2, 3, ...)
- `encryptedValue` (String) - Encrypted value at this version
- `rotatedAt` (Timestamp) - When this version was created
- `rotatedBy` (String) - User who rotated
- `rotationReason` (String) - Reason for rotation

#### 7. CredentialAccessLog

Audit trail of all credential access.

**Key Fields**:
- `credentialId` (UUID) - Accessed credential
- `accessedAt` (Timestamp) - Access timestamp
- `userId` (String) - User who accessed
- `serviceName` (String) - Service that accessed
- `ipAddress` (String) - Source IP address
- `success` (Boolean) - Access granted or denied
- `decrypted` (Boolean) - Was value decrypted
- `durationMs` (Long) - Access duration
- `failureReason` (String) - Reason if failed

#### 8. CredentialShare

Secure credential sharing between tenants/services.

**Key Fields**:
- `credentialId` (UUID) - Shared credential
- `sharedWithTenantId` (UUID) - Recipient tenant
- `sharedWithServiceName` (String) - Recipient service
- `permissions` (String) - READ, WRITE, ROTATE
- `expiresAt` (Timestamp) - Share expiration
- `requiresApproval` (Boolean) - Approval required
- `approved` (Boolean) - Approval status
- `approvedBy` (String) - Approver

#### 9. CredentialRotationPolicy

Automated rotation policies.

**Key Fields**:
- `name` (String) - Policy name
- `credentialTypeId` (UUID) - Applies to credential type
- `environmentTypeId` (UUID) - Applies to environment
- `rotationIntervalDays` (Integer) - Rotation frequency
- `autoRotate` (Boolean) - Automatic rotation
- `enabled` (Boolean) - Policy enabled
- `notifyBeforeDays` (Integer) - Alert before rotation

#### 10. CredentialAlert

Security alerts and notifications.

**Key Fields**:
- `credentialId` (UUID) - Related credential
- `alertType` (String) - EXPIRING, EXPIRED, COMPROMISED, ROTATION_FAILED
- `severity` (String) - LOW, MEDIUM, HIGH, CRITICAL
- `message` (String) - Alert message
- `acknowledged` (Boolean) - Alert acknowledged
- `acknowledgedBy` (String) - Who acknowledged
- `acknowledgedAt` (Timestamp) - When acknowledged

### Database Schema

All tables include standard audit fields:
- `created_at` (Timestamp) - Creation timestamp
- `created_by` (String) - Creator
- `updated_at` (Timestamp) - Last update
- `updated_by` (String) - Last updater
- `active` (Boolean) - Soft delete flag

**Indexes**:
- Primary keys on all `id` fields
- Unique index on `credential.code`
- Index on `credential.tenant_id` for multi-tenant queries
- Index on `credential_access_log.accessed_at` for audit queries
- Index on `credential.expires_at` for expiration checks

---

## 🔌 API Reference

The microservice exposes a comprehensive REST API with **10 controllers** and **~50 endpoints**.

### Base URL

```
http://localhost:8081/api/v1
```

### Authentication

Currently, the API does not include built-in authentication. In production, you should:
1. Deploy behind an API Gateway (AWS API Gateway, Kong, etc.)
2. Use OAuth 2.0 / JWT tokens
3. Implement Spring Security with your authentication provider

### API Endpoints Overview

| Endpoint | Description | Operations | Controller |
|----------|-------------|------------|------------|
| `/api/v1/credentials` | Manage encrypted credentials | CRUD + Filter | `CredentialController` |
| `/api/v1/credentials/{id}/decrypt` | Decrypt credential values | POST (decrypt) | `CredentialDecryptController` |
| `/api/v1/credential-types` | Manage credential types | CRUD + Filter | `CredentialTypeController` |
| `/api/v1/credential-statuses` | Manage credential statuses | CRUD + Filter | `CredentialStatusController` |
| `/api/v1/environment-types` | Manage environment types | CRUD + Filter | `EnvironmentTypeController` |
| `/api/v1/encryption-keys` | Manage encryption key metadata | CRUD + Filter | `EncryptionKeyController` |
| `/api/v1/credential-versions` | View credential version history | Read + Filter | `CredentialVersionController` |

### Common Operations

All controllers support these standard operations:

#### 1. Get by ID

```http
GET /api/v1/{resource}/{id}
```

**Example**:
```bash
curl http://localhost:8081/api/v1/credentials/123e4567-e89b-12d3-a456-426614174000
```

**Response**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": "STRIPE_API_KEY_PROD",
  "name": "Stripe Production API Key",
  "encryptedValue": "AQICAHh...encrypted-base64...",
  "rotationEnabled": true,
  "autoRotationDays": 90,
  "expiresAt": "2025-12-31T23:59:59"
}
```

#### 2. Filter with Pagination

```http
POST /api/v1/{resource}/filter
Content-Type: application/json

{
  "page": 0,
  "size": 20,
  "sort": "createdAt,desc",
  "filters": {
    "field": "value"
  }
}
```

**Example**:
```bash
curl -X POST http://localhost:8081/api/v1/credentials/filter \
  -H "Content-Type: application/json" \
  -d '{
    "page": 0,
    "size": 10,
    "filters": {
      "tenantId": "tenant-123",
      "environmentTypeId": "prod-env-id",
      "rotationEnabled": true
    }
  }'
```

**Response**:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5
}
```

#### 3. Create

```http
POST /api/v1/{resource}
Content-Type: application/json

{
  "field1": "value1",
  "field2": "value2"
}
```

**Example**:
```bash
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "NEW_API_KEY",
    "name": "New API Key",
    "encryptedValue": "secret-value",
    "rotationEnabled": true,
    "autoRotationDays": 90
  }'
```

#### 4. Update

```http
PUT /api/v1/{resource}/{id}
Content-Type: application/json

{
  "field1": "new-value1"
}
```

#### 5. Delete (Soft Delete)

```http
DELETE /api/v1/{resource}/{id}
```

Sets `active=false` instead of physically deleting the record.

### Special Endpoints

#### Decrypt Credential

```http
POST /api/v1/credentials/{id}/decrypt?reason=Processing%20payment
```

**Query Parameters**:
- `reason` (optional) - Reason for accessing the credential (for audit trail)

**Headers**:
- `X-User-Id` - User ID (extracted from JWT or service context)
- `X-Service-Name` - Service name (extracted from Istio headers or config)
- `X-Forwarded-For` - IP address (extracted from request)

**Example**:
```bash
curl -X POST "http://localhost:8081/api/v1/credentials/123e4567-e89b-12d3-a456-426614174000/decrypt?reason=Processing%20payment" \
  -H "X-User-Id: payment-service-account" \
  -H "X-Service-Name: payment-service" \
  -H "X-Forwarded-For: 10.0.1.50"
```

**Response** (200 OK):
```
sk_live_51H...
```

The response is the **plain text decrypted value** as a string.

**Access Control**: This endpoint validates:
- ✅ Service is in `allowedServices` list
- ✅ IP is in `allowedIps` list
- ✅ Environment matches
- ✅ Approval is provided if required (`requireApprovalForAccess`)
- ✅ Credential is ACTIVE and not expired
- ✅ All access is logged in the audit trail

#### Get Credential Versions

```http
GET /api/v1/credential-versions?credentialId={credentialId}
```

**Example**:
```bash
curl "http://localhost:8081/api/v1/credential-versions?credentialId=123e4567-e89b-12d3-a456-426614174000"
```

**Response**:
```json
{
  "content": [
    {
      "id": "version-1",
      "credentialId": "123e4567-e89b-12d3-a456-426614174000",
      "versionNumber": 2,
      "encryptedValue": "encrypted-value-v2",
      "isCurrent": true,
      "createdAt": "2025-10-31T10:30:00",
      "rotationReason": "Scheduled rotation"
    },
    {
      "id": "version-2",
      "credentialId": "123e4567-e89b-12d3-a456-426614174000",
      "versionNumber": 1,
      "encryptedValue": "encrypted-value-v1",
      "isCurrent": false,
      "createdAt": "2025-08-01T10:00:00",
      "rotationReason": "Initial version"
    }
  ],
  "totalElements": 2
}
```

### Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2025-10-31T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Credential with code 'DUPLICATE_KEY' already exists",
  "path": "/api/v1/credentials"
}
```

**Common HTTP Status Codes**:
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Circuit breaker open

### Rate Limiting

The API is protected by a rate limiter:
- **Limit**: 100 requests/second per instance
- **Response**: `429 Too Many Requests` when exceeded

### Interactive API Documentation

When the service is running, access:

**Swagger UI**: http://localhost:8081/swagger-ui.html

This provides:
- ✅ Interactive API testing
- ✅ Request/response examples
- ✅ Schema documentation
- ✅ Try-it-out functionality

---

## 📦 Java SDK Usage

The microservice includes an **auto-generated Java SDK** using OpenAPI Generator. The SDK provides type-safe, reactive API clients.

### Adding the SDK Dependency

```xml
<dependency>
    <groupId>com.firefly</groupId>
    <artifactId>common-platform-security-vault-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### SDK Architecture

The SDK is generated from the OpenAPI specification and includes:

| Package | Description | Classes |
|---------|-------------|---------|
| `com.firefly.common.security.vault.sdk.api` | API clients | `CredentialsApi`, `CredentialDecryptionApi`, `CredentialTypesApi`, etc. |
| `com.firefly.common.security.vault.sdk.model` | DTOs | `CredentialDTO`, `EncryptionKeyDTO`, `PaginationResponse`, etc. |
| `com.firefly.common.security.vault.sdk.invoker` | Configuration | `ApiClient` (base client with WebClient) |

### Basic Usage

```java
import com.firefly.common.security.vault.sdk.api.CredentialsApi;
import com.firefly.common.security.vault.sdk.api.CredentialDecryptionApi;
import com.firefly.common.security.vault.sdk.invoker.ApiClient;
import com.firefly.common.security.vault.sdk.model.CredentialDTO;
import reactor.core.publisher.Mono;

// 1. Configure the API client
ApiClient apiClient = new ApiClient();
apiClient.setBasePath("http://localhost:8081");

// 2. Create API instances
CredentialsApi credentialsApi = new CredentialsApi(apiClient);
CredentialDecryptionApi decryptionApi = new CredentialDecryptionApi(apiClient);

// 3. Create a credential (reactive)
CredentialDTO newCredential = CredentialDTO.builder()
    .code("MY_API_KEY")
    .name("My API Key")
    .credentialTypeId(credentialTypeId)
    .credentialStatusId(credentialStatusId)
    .environmentTypeId(environmentTypeId)
    .encryptedValue("my-secret-value")
    .rotationEnabled(true)
    .autoRotationDays(90)
    .build();

Mono<CredentialDTO> createdCredential = credentialsApi.createCredential(newCredential, null);

// 4. Subscribe to get the result
createdCredential.subscribe(
    credential -> System.out.println("Created: " + credential.getId()),
    error -> System.err.println("Error: " + error.getMessage())
);

// 5. Or block for synchronous call
CredentialDTO result = createdCredential.block();
```

### Decrypting Credentials

```java
import java.util.UUID;

UUID credentialId = UUID.fromString("your-credential-id");

// Decrypt with reason (for audit trail)
Mono<String> decryptedValue = decryptionApi.decryptCredential(
    credentialId,
    "Accessing payment gateway",  // reason
    null                          // idempotency key (optional)
);

// Use the decrypted value
String apiKey = decryptedValue.block();
```

### Filtering and Pagination

```java
import com.firefly.common.security.vault.sdk.model.FilterRequestCredentialDTO;
import com.firefly.common.security.vault.sdk.model.PaginationRequest;
import com.firefly.common.security.vault.sdk.model.PaginationResponseCredentialDTO;

// Create filter request
FilterRequestCredentialDTO filterRequest = new FilterRequestCredentialDTO();
PaginationRequest pagination = new PaginationRequest();
pagination.setPage(0);
pagination.setSize(20);
filterRequest.setPagination(pagination);

// Filter credentials
Mono<PaginationResponseCredentialDTO> response = credentialsApi.filterCredentials(filterRequest, null);

response.subscribe(page -> {
    System.out.println("Total: " + page.getTotalElements());
    page.getContent().forEach(cred ->
        System.out.println("- " + cred.getCode())
    );
});
```

### Error Handling

```java
credentialsApi.getCredentialById(credentialId, null)
    .doOnError(error -> {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webError = (WebClientResponseException) error;
            System.err.println("HTTP Status: " + webError.getStatusCode());
            System.err.println("Response: " + webError.getResponseBodyAsString());
        }
    })
    .onErrorResume(error -> {
        // Fallback logic
        return Mono.just(defaultCredential);
    })
    .subscribe();
```

### Spring Boot Integration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaultSdkConfig {

    @Bean
    public ApiClient vaultApiClient(@Value("${vault.base-url}") String baseUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        return apiClient;
    }

    @Bean
    public CredentialsApi credentialsApi(ApiClient apiClient) {
        return new CredentialsApi(apiClient);
    }

    @Bean
    public CredentialDecryptionApi credentialDecryptionApi(ApiClient apiClient) {
        return new CredentialDecryptionApi(apiClient);
    }
}
```

Then inject in your services:

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CredentialDecryptionApi decryptionApi;

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return decryptionApi.decryptCredential(stripeApiKeyId, "Processing payment", null)
            .flatMap(apiKey -> {
                // Use the decrypted API key
                return stripeClient.charge(request, apiKey);
            });
    }
}
```

### Available API Clients

| API Client | Purpose | Key Methods |
|------------|---------|-------------|
| `CredentialsApi` | Manage credentials | `createCredential()`, `getCredentialById()`, `updateCredential()`, `deleteCredential()`, `filterCredentials()` |
| `CredentialDecryptionApi` | Decrypt credentials | `decryptCredential()` |
| `CredentialTypesApi` | Manage credential types | `createCredentialType()`, `getCredentialTypeById()`, `filterCredentialTypes()` |
| `CredentialStatusesApi` | Manage credential statuses | `createCredentialStatus()`, `getCredentialStatusById()` |
| `EnvironmentTypesApi` | Manage environments | `createEnvironmentType()`, `getEnvironmentTypeById()` |
| `EncryptionKeysApi` | Manage encryption keys | `getEncryptionKeyById()`, `filterEncryptionKeys()` |
| `CredentialVersionsApi` | View version history | `getCredentialVersionById()`, `filterCredentialVersions()` |

### SDK Configuration Options

```java
ApiClient apiClient = new ApiClient();

// Set base URL
apiClient.setBasePath("https://vault.production.getfirefly.io");

// Add custom headers (e.g., authentication)
apiClient.addDefaultHeader("Authorization", "Bearer " + jwtToken);

// Configure timeouts (via WebClient)
WebClient customWebClient = WebClient.builder()
    .baseUrl("http://localhost:8081")
    .defaultHeader("X-Service-Name", "payment-service")
    .build();

ApiClient customApiClient = new ApiClient(customWebClient);
```

---

## 🔒 Security & Compliance

### Encryption Details

The microservice uses **AES-256-GCM** (Galois/Counter Mode) for encryption:

**Why AES-256-GCM?**
- ✅ **NIST Approved**: Recommended by NIST for sensitive data
- ✅ **Authenticated Encryption**: Provides both confidentiality and integrity
- ✅ **Performance**: Hardware-accelerated on modern CPUs
- ✅ **Tamper Detection**: 128-bit authentication tag prevents tampering

**Encryption Process**:

```
1. Generate unique 12-byte IV (Initialization Vector)
   └─ Uses SecureRandom for cryptographic randomness

2. Encrypt plaintext with AES-256-GCM
   ├─ Key: From KMS provider (AWS KMS, Azure, etc.)
   ├─ IV: Unique per operation
   └─ Output: Ciphertext + 128-bit authentication tag

3. Encode result as Base64
   └─ Format: IV || Ciphertext || Auth Tag

4. Store in database
   └─ Only encrypted value stored, never plaintext
```

**Decryption Process**:

```
1. Retrieve encrypted value from database
   └─ Base64-encoded string

2. Decode Base64
   └─ Extract: IV || Ciphertext || Auth Tag

3. Verify authentication tag
   └─ Ensures data hasn't been tampered with

4. Decrypt with AES-256-GCM
   ├─ Key: From KMS provider
   ├─ IV: Extracted from encrypted value
   └─ Output: Plaintext (if auth tag valid)

5. Return plaintext to caller
   └─ Log access in audit trail
```

### Envelope Encryption

The microservice uses **envelope encryption** for enhanced security:

```
┌─────────────────────────────────────────────────────────┐
│                    KMS Provider                         │
│              (AWS KMS, Azure, etc.)                     │
│                                                         │
│  ┌─────────────────────────────────────────────────┐    │
│  │         Master Encryption Key (MEK)             │    │
│  │         (Never leaves KMS)                      │    │
│  └─────────────────────────────────────────────────┘    │
│                        │                                │
│                        │ Encrypts/Decrypts              │
│                        ▼                                │
│  ┌─────────────────────────────────────────────────┐    │
│  │      Data Encryption Key (DEK)                  │    │
│  │      (Encrypted DEK stored with data)           │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Encrypts/Decrypts
                        ▼
┌─────────────────────────────────────────────────────────┐
│                  Application Data                       │
│              (Credential values)                        │
└─────────────────────────────────────────────────────────┘
```

**Benefits**:
- ✅ Master key never leaves KMS
- ✅ Can encrypt large amounts of data efficiently
- ✅ Easy key rotation (re-encrypt DEK with new MEK)
- ✅ Compliance with security standards

### Access Control Matrix

| Control Type | Implementation | Enforcement Point |
|-------------|----------------|-------------------|
| **Service Whitelist** | `allowedServices` field | Decryption endpoint |
| **IP Whitelist** | `allowedIps` field (CIDR notation) | Decryption endpoint |
| **Environment Isolation** | `environmentTypeId` field | Decryption endpoint |
| **Approval Workflow** | `requireApprovalForAccess` flag | Decryption endpoint |
| **Tenant Isolation** | `tenantId` field | All endpoints |
| **Status Check** | `credentialStatusId` must be ACTIVE | Decryption endpoint |
| **Expiration Check** | `expiresAt` must be in future | Decryption endpoint |

**Example Access Control**:

```java
// Credential configuration
{
  "allowedServices": "payment-service,billing-service",
  "allowedIps": "10.0.1.0/24,10.0.2.100",
  "allowedEnvironments": "PRODUCTION",
  "requireApprovalForAccess": true
}

// Access request
{
  "serviceName": "payment-service",  // ✓ In allowedServices
  "ipAddress": "10.0.1.50",          // ✓ In allowedIps (10.0.1.0/24)
  "environment": "PRODUCTION",        // ✓ Matches allowedEnvironments
  "hasApproval": true                 // ✓ Approval provided
}
// Result: Access GRANTED
```

### Audit Trail

Every credential access is logged with:

| Field | Description | Example |
|-------|-------------|---------|
| `credentialId` | Which credential was accessed | `123e4567-...` |
| `accessedAt` | When it was accessed | `2025-10-31T10:30:00Z` |
| `userId` | Who accessed it | `john.doe@example.com` |
| `serviceName` | Which service accessed it | `payment-service` |
| `ipAddress` | Source IP address | `10.0.1.50` |
| `success` | Was access granted | `true` / `false` |
| `decrypted` | Was value decrypted | `true` / `false` |
| `durationMs` | How long it took | `45` ms |
| `failureReason` | Why it failed (if failed) | `IP not whitelisted` |

**Audit Query Examples**:

```bash
# Who accessed credential X in the last 24 hours?
POST /api/v1/credential-access-logs/filter
{
  "filters": {
    "credentialId": "123e4567-...",
    "accessedAt": { "gte": "2025-10-30T10:30:00Z" }
  }
}

# Failed access attempts from specific IP
POST /api/v1/credential-access-logs/filter
{
  "filters": {
    "ipAddress": "192.168.1.100",
    "success": false
  }
}

# All decryptions by service
POST /api/v1/credential-access-logs/filter
{
  "filters": {
    "serviceName": "payment-service",
    "decrypted": true
  }
}
```

### Compliance Features

| Requirement | Implementation | Standard |
|------------|----------------|----------|
| **Encryption at Rest** | AES-256-GCM | PCI DSS, HIPAA, SOC 2 |
| **Encryption in Transit** | TLS 1.3 | PCI DSS, HIPAA, SOC 2 |
| **Access Logging** | Complete audit trail | PCI DSS, HIPAA, SOC 2, GDPR |
| **Key Rotation** | Automatic rotation policies | PCI DSS, NIST |
| **Separation of Duties** | Approval workflows | SOC 2, ISO 27001 |
| **Data Retention** | Configurable retention policies | GDPR, CCPA |
| **Tamper Detection** | GCM authentication tags | NIST, FIPS 140-2 |

### Security Best Practices

#### 1. Never Log Plaintext Credentials

```java
// ❌ BAD
log.info("Decrypted value: {}", decryptedValue);

// ✅ GOOD
log.info("Credential {} decrypted successfully", credentialId);
```

#### 2. Use Environment Variables for Sensitive Config

```yaml
# ❌ BAD
firefly:
  security:
    vault:
      encryption:
        aws-kms:
          access-key-id: AKIAIOSFODNN7EXAMPLE
          secret-access-key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# ✅ GOOD
firefly:
  security:
    vault:
      encryption:
        aws-kms:
          access-key-id: ${AWS_ACCESS_KEY_ID}
          secret-access-key: ${AWS_SECRET_ACCESS_KEY}
```

#### 3. Enable TLS in Production

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

#### 4. Implement Rate Limiting

Already built-in with Resilience4j:
- 100 requests/second per instance
- Prevents brute-force attacks
- Protects KMS provider rate limits

#### 5. Monitor Security Alerts

```bash
# Set up alerts for:
- Failed access attempts (potential breach)
- Expired credentials (compliance risk)
- Compromised credentials (security incident)
- Rotation failures (operational risk)
```

---

## 💡 Real-World Use Cases

### Use Case 1: Payment Provider Integration

**Scenario**: Your banking platform integrates with Stripe for payment processing. You need to store Stripe API keys securely.

**Solution**:

```bash
# 1. Create credential type for API keys (if not exists)
curl -X POST http://localhost:8081/api/v1/credential-types \
  -H "Content-Type: application/json" \
  -d '{
    "code": "API_KEY",
    "name": "API Key",
    "description": "External API keys"
  }'

# 2. Create production environment (if not exists)
curl -X POST http://localhost:8081/api/v1/environment-types \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PRODUCTION",
    "name": "Production",
    "description": "Live production environment"
  }'

# 3. Store Stripe API key
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "STRIPE_API_KEY_PROD",
    "name": "Stripe Production API Key",
    "credentialTypeId": "{api-key-type-id}",
    "environmentTypeId": "{production-env-id}",
    "encryptedValue": "sk_live_51H...",
    "rotationEnabled": true,
    "autoRotationDays": 90,
    "allowedServices": "payment-service",
    "allowedIps": "10.0.1.0/24",
    "requireApprovalForAccess": false,
    "maskInLogs": true,
    "expiresAt": "2025-12-31T23:59:59"
  }'

# 4. Retrieve in your payment service
curl -X POST "http://localhost:8081/api/v1/credentials/{id}/decrypt?reason=Processing%20customer%20payment" \
  -H "X-User-Id: payment-service-account" \
  -H "X-Service-Name: payment-service" \
  -H "X-Forwarded-For: 10.0.1.50"

# Response: sk_live_51H...
```

**Benefits**:
- ✅ API key encrypted with AES-256-GCM
- ✅ Automatic rotation every 90 days
- ✅ Only accessible from payment-service
- ✅ Only accessible from 10.0.1.0/24 network
- ✅ All access logged for audit
- ✅ Masked in logs to prevent leakage

### Use Case 2: Multi-Environment Database Credentials

**Scenario**: You have analytics databases in dev, staging, and production. Each needs separate credentials.

**Solution**:

```bash
# Create credentials for each environment
for ENV in DEVELOPMENT STAGING PRODUCTION; do
  curl -X POST http://localhost:8081/api/v1/credentials \
    -H "Content-Type: application/json" \
    -d "{
      \"code\": \"DB_ANALYTICS_${ENV}\",
      \"name\": \"Analytics Database ${ENV}\",
      \"credentialTypeId\": \"{database-creds-type-id}\",
      \"environmentTypeId\": \"{${ENV}-env-id}\",
      \"encryptedValue\": \"postgresql://user:pass@db-${ENV}.example.com:5432/analytics\",
      \"rotationEnabled\": true,
      \"autoRotationDays\": 30,
      \"allowedServices\": \"analytics-service,reporting-service\",
      \"maskInLogs\": true
    }"
done
```

**Benefits**:
- ✅ Complete environment isolation
- ✅ Different rotation policies per environment
- ✅ Separate access controls
- ✅ Easy to manage and audit

### Use Case 3: OAuth 2.0 Client Credentials with Approval

**Scenario**: Your platform integrates with a third-party payment gateway using OAuth 2.0. Access requires manager approval.

**Solution**:

```bash
# 1. Store OAuth credentials
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "OAUTH2_PAYMENT_GATEWAY",
    "name": "Payment Gateway OAuth Client",
    "credentialTypeId": "{oauth2-type-id}",
    "environmentTypeId": "{production-env-id}",
    "encryptedValue": "{\"client_id\":\"abc123\",\"client_secret\":\"xyz789\"}",
    "requireApprovalForAccess": true,
    "allowedServices": "payment-integration-service",
    "rotationEnabled": false
  }'

# 2. Request access (will fail without approval)
curl -X POST "http://localhost:8081/api/v1/credentials/{id}/decrypt?reason=Testing%20payment%20integration" \
  -H "X-User-Id: developer@example.com" \
  -H "X-Service-Name: payment-integration-service" \
  -H "X-Forwarded-For: 10.0.1.100"
# Response: 403 Forbidden - Approval required

# 3. Request with approval (approval mechanism would be implemented separately)
curl -X POST "http://localhost:8081/api/v1/credentials/{id}/decrypt?reason=Approved%20by%20manager%40example.com%20-%20Ticket%20%2312345" \
  -H "X-User-Id: developer@example.com" \
  -H "X-Service-Name: payment-integration-service" \
  -H "X-Forwarded-For: 10.0.1.100"
# Response: 200 OK - Access granted (if approval was granted through separate workflow)
```

**Benefits**:
- ✅ Approval workflow for sensitive credentials
- ✅ Audit trail includes approval reason
- ✅ Prevents unauthorized access

### Use Case 4: Multi-Tenant Credential Management

**Scenario**: You have multiple tenants and each needs isolated credentials.

**Solution**:

```bash
# 1. Create credential for Tenant A
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WEBHOOK_SECRET_TENANT_A",
    "name": "Tenant A Webhook Secret",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "tenantId": "tenant-a-uuid",
    "encryptedValue": "webhook-secret-value-a",
    "allowedServices": "tenant-a-integration-service",
    "maskInLogs": true
  }'

# 2. Create credential for Tenant B
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WEBHOOK_SECRET_TENANT_B",
    "name": "Tenant B Webhook Secret",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "tenantId": "tenant-b-uuid",
    "encryptedValue": "webhook-secret-value-b",
    "allowedServices": "tenant-b-integration-service",
    "maskInLogs": true
  }'

# 3. Filter credentials by tenant
curl -X POST http://localhost:8081/api/v1/credentials/filter \
  -H "Content-Type: application/json" \
  -d '{
    "page": 0,
    "size": 10,
    "filters": {
      "tenantId": "tenant-a-uuid"
    }
  }'
```

**Benefits**:
- ✅ Complete tenant isolation
- ✅ Separate access controls per tenant
- ✅ Easy filtering by tenant
- ✅ Audit trail per tenant
- ✅ Scalable multi-tenant architecture

### Use Case 5: Credential Rotation and Version History

**Scenario**: Track credential rotation and maintain version history.

**Solution**:

```bash
# 1. Create credential with rotation enabled
curl -X POST http://localhost:8081/api/v1/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DB_MAIN_PASSWORD",
    "name": "Main Database Password",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "current-password",
    "rotationEnabled": true,
    "autoRotationDays": 30,
    "rotateBeforeDays": 7
  }'

# 2. Update credential (simulates rotation)
curl -X PUT http://localhost:8081/api/v1/credentials/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DB_MAIN_PASSWORD",
    "name": "Main Database Password",
    "credentialTypeId": "00000000-0000-0000-0000-000000000001",
    "credentialStatusId": "00000000-0000-0000-0000-000000000001",
    "environmentTypeId": "00000000-0000-0000-0000-000000000001",
    "encryptedValue": "new-rotated-password",
    "rotationEnabled": true,
    "autoRotationDays": 30
  }'

# 3. View rotation history
curl "http://localhost:8081/api/v1/credential-versions/filter?credentialId={id}"

# 4. Filter credentials that need rotation soon
curl -X POST http://localhost:8081/api/v1/credentials/filter \
  -H "Content-Type: application/json" \
  -d '{
    "page": 0,
    "size": 10,
    "filters": {
      "rotationEnabled": true
    }
  }'
```

**Benefits**:
- ✅ Rotation tracking with autoRotationDays
- ✅ Alert before rotation with rotateBeforeDays
- ✅ Complete version history via credential-versions
- ✅ Manual rotation via UPDATE endpoint
- ✅ Filter credentials needing rotation

---

## 📊 Monitoring & Observability

### Health Checks

The microservice exposes health check endpoints via Spring Boot Actuator:

```bash
# Overall health
curl http://localhost:8081/actuator/health

# Response
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "keyManagement": {
      "status": "UP",
      "details": {
        "provider": "AWS_KMS",
        "keyId": "arn:aws:kms:...",
        "lastCheck": "2025-10-31T10:30:00Z"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

**Health Indicators**:
- `db` - Database connectivity
- `keyManagement` - KMS provider connectivity
- `diskSpace` - Available disk space
- `ping` - Basic liveness check

### Metrics

The microservice exposes Prometheus-compatible metrics:

```bash
# Prometheus metrics endpoint
curl http://localhost:8081/actuator/prometheus
```

**Key Metrics**:

| Metric | Type | Description |
|--------|------|-------------|
| `http_server_requests_seconds` | Histogram | HTTP request latency |
| `resilience4j_circuitbreaker_state` | Gauge | Circuit breaker state (0=closed, 1=open) |
| `resilience4j_circuitbreaker_failure_rate` | Gauge | Circuit breaker failure rate |
| `resilience4j_ratelimiter_available_permissions` | Gauge | Available rate limiter permissions |
| `resilience4j_retry_calls` | Counter | Retry attempts |
| `kms_encryption_total` | Counter | Total encryption operations |
| `kms_decryption_total` | Counter | Total decryption operations |
| `kms_encryption_duration_seconds` | Histogram | Encryption operation latency |
| `kms_decryption_duration_seconds` | Histogram | Decryption operation latency |
| `credential_access_total` | Counter | Total credential access attempts |
| `credential_access_denied_total` | Counter | Denied access attempts |
| `credential_rotation_total` | Counter | Total credential rotations |

**Example Prometheus Queries**:

```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# 95th percentile latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Circuit breaker state
resilience4j_circuitbreaker_state{name="kms-operations"}

# Failed access attempts
rate(credential_access_denied_total[5m])
```

### Logging

The microservice uses structured logging with SLF4J and Logback:

**Log Levels**:
- `ERROR` - Critical errors requiring immediate attention
- `WARN` - Warning conditions (circuit breaker open, retry attempts)
- `INFO` - Informational messages (startup, configuration)
- `DEBUG` - Detailed debugging information
- `TRACE` - Very detailed trace information

**Example Log Output**:

```
2025-10-31 10:30:00 INFO  [main] c.f.c.s.v.SecurityVaultApplication - Starting SecurityVaultApplication
2025-10-31 10:30:01 INFO  [main] c.f.c.s.v.c.KmsProviderConfiguration - Initializing KMS provider: AWS_KMS
2025-10-31 10:30:02 INFO  [main] c.f.c.s.v.c.ResilienceConfiguration - Configuring resilience patterns
2025-10-31 10:30:03 INFO  [main] c.f.c.s.v.SecurityVaultApplication - Started SecurityVaultApplication in 3.456 seconds
2025-10-31 10:30:15 INFO  [reactor-http-nio-2] c.f.c.s.v.s.CredentialServiceImpl - Credential created: STRIPE_API_KEY_PROD
2025-10-31 10:30:20 INFO  [reactor-http-nio-3] c.f.c.s.v.s.CredentialServiceImpl - Credential decrypted: STRIPE_API_KEY_PROD (user=payment-service)
2025-10-31 10:30:25 WARN  [reactor-http-nio-4] c.f.c.s.v.a.ResilientKeyManagementAdapter - Retry attempt 1 for KMS operation: Network timeout
2025-10-31 10:30:27 WARN  [reactor-http-nio-5] c.f.c.s.v.a.ResilientKeyManagementAdapter - Circuit breaker state transition: CLOSED -> OPEN
```

**Configure Logging**:

```yaml
logging:
  level:
    root: INFO
    com.firefly.common.security.vault: DEBUG
    org.springframework.r2dbc: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} %-5level [%thread] %logger{36} - %msg%n"
  file:
    name: /var/log/firefly-vault/application.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 1GB
```

### Alerting

**Recommended Alerts**:

```yaml
# Prometheus Alert Rules
groups:
  - name: firefly-vault-alerts
    rules:
      # High error rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate in Firefly Security Vault"
          description: "Error rate is {{ $value }} errors/second"

      # Circuit breaker open
      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{name="kms-operations"} == 1
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Circuit breaker is open - KMS may be down"
          description: "Circuit breaker for KMS operations is in OPEN state"

      # High latency
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency detected"
          description: "95th percentile latency is {{ $value }} seconds"

      # Failed access attempts
      - alert: HighFailedAccessRate
        expr: rate(credential_access_denied_total[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High rate of failed credential access attempts"
          description: "{{ $value }} failed access attempts per second"

      # Database connection issues
      - alert: DatabaseDown
        expr: up{job="firefly-vault"} == 0 or health_component_status{component="db"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database is down or unreachable"
          description: "Cannot connect to PostgreSQL database"

      # Expiring credentials
      - alert: CredentialsExpiringSoon
        expr: credential_expiring_soon_total > 0
        for: 1h
        labels:
          severity: warning
        annotations:
          summary: "Credentials expiring soon"
          description: "{{ $value }} credentials will expire in the next 7 days"
```

### Grafana Dashboard

**Example Dashboard Panels**:

1. **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
2. **Error Rate**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m])`
3. **Latency (p50, p95, p99)**: `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
4. **Circuit Breaker State**: `resilience4j_circuitbreaker_state`
5. **KMS Operations**: `rate(kms_encryption_total[5m])` + `rate(kms_decryption_total[5m])`
6. **Failed Access Attempts**: `rate(credential_access_denied_total[5m])`
7. **Active Credentials**: `credential_active_total`
8. **Credential Rotations**: `rate(credential_rotation_total[1h])`

---

## 🧪 Testing

The microservice includes **94 unit tests** with **85% code coverage**.

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl common-platform-security-vault-core

# Run specific test class
mvn test -Dtest=ResilientKeyManagementAdapterTest

# Run with coverage report
mvn test jacoco:report
```

### Test Structure

```
common-platform-security-vault-core/src/test/java/
├── adapters/
│   ├── ResilientKeyManagementAdapterTest.java      # 16 tests
│   ├── InMemoryKeyManagementAdapterTest.java       # 16 tests
│   └── AesGcmCredentialEncryptionAdapterTest.java  # 17 tests
├── config/
│   └── SecurityVaultConfigurationValidatorTest.java # 12 tests
├── health/
│   └── KeyManagementHealthIndicatorTest.java       # 9 tests
├── metrics/
│   └── KeyManagementMetricsTest.java               # 15 tests
└── services/
    └── impl/
        └── CredentialServiceImplTest.java          # 9 tests
```

### Test Coverage

| Component | Tests | Coverage |
|-----------|-------|----------|
| **Adapters** | 49 tests | 90% |
| **Services** | 9 tests | 80% |
| **Configuration** | 12 tests | 85% |
| **Health Checks** | 9 tests | 95% |
| **Metrics** | 15 tests | 90% |
| **Total** | **94 tests** | **85%** |

### Writing Tests

**Example Test**:

```java
@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private CredentialEncryptionPort encryptionPort;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    @Test
    void shouldCreateCredential() {
        // Given
        CredentialDTO dto = new CredentialDTO();
        dto.setCode("TEST_CREDENTIAL");
        dto.setEncryptedValue("secret-value");

        Credential entity = new Credential();
        entity.setId(UUID.randomUUID());
        entity.setCode("TEST_CREDENTIAL");

        when(credentialRepository.save(any())).thenReturn(Mono.just(entity));

        // When
        Mono<CredentialDTO> result = credentialService.create(dto);

        // Then
        StepVerifier.create(result)
            .assertNext(created -> {
                assertThat(created.getCode()).isEqualTo("TEST_CREDENTIAL");
            })
            .verifyComplete();

        verify(credentialRepository).save(any());
    }
}
```

---

## 📚 Additional Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:

| Document | Description |
|----------|-------------|
| [Resilience Patterns](docs/resilience-patterns.md) | Circuit Breaker, Rate Limiter, Retry patterns |
| [Architecture Guide](docs/architecture/README.md) | Hexagonal architecture, design patterns, decisions |
| [Configuration Guide](docs/configuration/README.md) | KMS provider setup and configuration |
| [Deployment Guide](docs/operations/deployment.md) | Production deployment strategies |
| [Monitoring Guide](docs/operations/monitoring.md) | Metrics, health checks, alerting |
| [API Reference](docs/api/README.md) | Complete API documentation |
| [Development Guide](docs/development/README.md) | Contributing, testing, best practices |
| [Security Guide](docs/security/README.md) | Security features and best practices |

## ❓ Frequently Asked Questions (FAQ)

### General Questions

**Q: What is the difference between this and HashiCorp Vault?**

A: Firefly Security Vault is a **credential management microservice** specifically designed for the Firefly banking platform, while HashiCorp Vault is a general-purpose secrets management tool. Key differences:

| Feature | Firefly Security Vault | HashiCorp Vault |
|---------|----------------------|-----------------|
| **Purpose** | Banking credential management | General secrets management |
| **Architecture** | Hexagonal (pluggable KMS) | Monolithic |
| **KMS Support** | AWS, Azure, Google, HashiCorp | Self-managed |
| **Multi-tenant** | Built-in | Requires namespaces |
| **Audit Trail** | Banking-specific audit logs | General audit logs |
| **Rotation** | Policy-driven with approval workflows | Manual or plugin-based |
| **Integration** | REST API + Java SDK | CLI + HTTP API |

You can actually use HashiCorp Vault **as a KMS provider** for Firefly Security Vault!

**Q: Can I use this without the Firefly banking platform?**

A: Yes! While designed for Firefly, it's a standalone microservice that can be used by any application needing secure credential management.

**Q: Which KMS provider should I choose?**

A: Choose based on your infrastructure:
- **AWS KMS**: If running on AWS (EC2, ECS, EKS, Lambda)
- **Azure Key Vault**: If running on Azure (VMs, AKS, Functions)
- **Google Cloud KMS**: If running on Google Cloud (GCE, GKE, Cloud Functions)
- **HashiCorp Vault**: If on-premise or hybrid cloud
- **In-Memory**: For local development and testing only

**Q: Is this production-ready?**

A: Yes! The microservice includes:
- ✅ 94 unit tests with 85% coverage
- ✅ Production-grade resilience patterns (Circuit Breaker, Rate Limiter, Retry)
- ✅ Complete audit logging
- ✅ Health checks and metrics
- ✅ Multi-KMS provider support
- ✅ Comprehensive documentation

### Security Questions

**Q: How are credentials encrypted?**

A: Credentials are encrypted using **AES-256-GCM** with:
- Unique 12-byte IV per operation
- 128-bit authentication tag for tamper detection
- Keys managed by your chosen KMS provider (AWS KMS, Azure Key Vault, etc.)
- Envelope encryption (data keys encrypted with master keys)

**Q: Are encryption keys stored in the database?**

A: **No!** Only encrypted data is stored in the database. Encryption keys are managed by your KMS provider and never leave the KMS.

**Q: What happens if the KMS provider is down?**

A: The microservice includes resilience patterns:
1. **Retry**: Automatically retries 3 times with exponential backoff
2. **Circuit Breaker**: Opens after 50% failure rate, preventing cascading failures
3. **Rate Limiter**: Prevents overwhelming the KMS with requests

If KMS is down for extended periods, the circuit breaker will fail fast and return errors immediately.

**Q: Can I rotate encryption keys?**

A: Yes! The microservice supports:
- **Automatic rotation**: Policy-driven rotation schedules
- **Manual rotation**: On-demand rotation with reason tracking
- **Version history**: Complete history of all rotations
- **Rollback**: Roll back to previous versions if needed

### Deployment Questions

**Q: Can I run multiple instances for high availability?**

A: Yes! The microservice is stateless and can be scaled horizontally:
- Deploy 3+ instances behind a load balancer
- Each instance connects to the same PostgreSQL database
- Each instance connects to the same KMS provider
- No session affinity required

**Q: What are the resource requirements?**

A: Recommended per instance:
- **CPU**: 2-4 cores
- **Memory**: 1-2 GB
- **Database Connections**: 50 (configurable)
- **Network**: Low latency to KMS provider

**Q: How do I backup credentials?**

A: Credentials are stored in PostgreSQL:
1. **Database Backups**: Use PostgreSQL's `pg_dump` or continuous archiving
2. **Encrypted Backups**: Credentials are already encrypted in the database
3. **KMS Key Backup**: Enable automatic key rotation and backup in your KMS provider

**Q: Can I use this with Kubernetes?**

A: Yes! See the [Production Deployment Guide](#option-1-deploy-with-aws-kms) for Kubernetes deployment examples.

### Integration Questions

**Q: How do I integrate this with my application?**

A: Three options:
1. **REST API**: Call the API directly using HTTP client
2. **Java SDK**: Use the auto-generated Java client library
3. **Service Mesh**: Deploy as a sidecar container

**Q: Does this support authentication?**

A: The microservice does not include built-in authentication. In production:
- Deploy behind an API Gateway (AWS API Gateway, Kong, etc.)
- Use OAuth 2.0 / JWT tokens
- Implement Spring Security with your authentication provider

**Q: Can I use this with non-Java applications?**

A: Yes! The REST API can be called from any language:
- Python: `requests` library
- Node.js: `axios` or `fetch`
- Go: `net/http`
- .NET: `HttpClient`
- Any language with HTTP support

### Performance Questions

**Q: What is the expected latency?**

A: Typical latencies:
- **Credential retrieval**: 10-50ms (database query)
- **Credential decryption**: 50-200ms (includes KMS call)
- **Credential creation**: 20-100ms (database write)

Latency depends on:
- Database performance
- KMS provider latency (AWS KMS: ~50ms, Azure: ~100ms)
- Network latency

**Q: How many requests can it handle?**

A: Performance depends on:
- **Instance resources**: 2-4 cores, 1-2 GB RAM
- **Database**: PostgreSQL with proper indexing
- **Rate Limiter**: 100 requests/second per instance (configurable)

Typical throughput: **500-1000 requests/second** with 3 instances.

**Q: Does it cache credentials?**

A: No, credentials are not cached for security reasons. Each request:
1. Retrieves encrypted value from database
2. Decrypts using KMS provider
3. Returns plaintext to caller
4. Logs access in audit trail

Caching would reduce security and auditability.

---

## 🤝 Contributing

We welcome contributions! Here's how to get started:

### Quick Start

1. **Fork** the repository
2. **Clone** your fork: `git clone https://github.com/your-username/common-platform-security-vault.git`
3. **Create** a feature branch: `git checkout -b feature/amazing-feature`
4. **Make** your changes
5. **Test** your changes: `mvn test`
6. **Commit** your changes: `git commit -m 'Add amazing feature'`
7. **Push** to your fork: `git push origin feature/amazing-feature`
8. **Open** a Pull Request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/firefly-oss/common-platform-security-vault.git
cd common-platform-security-vault

# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
cd common-platform-security-vault-web
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Write unit tests for new features
- Ensure all tests pass before submitting PR

### Pull Request Guidelines

- **Title**: Clear and descriptive (e.g., "Add support for GCP KMS provider")
- **Description**: Explain what changes were made and why
- **Tests**: Include unit tests for new features
- **Documentation**: Update README.md and docs/ if needed
- **Commits**: Use clear commit messages

---

## 📄 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

### What does this mean?

✅ **You can**:
- Use this software for commercial purposes
- Modify the source code
- Distribute the software
- Use this software privately
- Use patent claims

❌ **You cannot**:
- Hold the authors liable
- Use trademarks without permission

📋 **You must**:
- Include the license and copyright notice
- State significant changes made to the code

---

## 🆘 Support & Community

### Getting Help

| Resource | Link | Description |
|----------|------|-------------|
| **📖 Documentation** | [docs/](./docs) | Comprehensive guides and tutorials |
| **🐛 Issues** | [GitHub Issues](https://github.com/firefly-oss/common-platform-security-vault/issues) | Bug reports and feature requests |
| **💬 Discussions** | [GitHub Discussions](https://github.com/firefly-oss/common-platform-security-vault/discussions) | Questions and community support |
| **📧 Email** | dev@getfirefly.io | Direct support |
| **🌐 Website** | [getfirefly.io](https://getfirefly.io) | Firefly platform information |

### Reporting Security Vulnerabilities

**Do NOT open a public issue for security vulnerabilities!**

Instead, email: **security@getfirefly.io**

We will respond within 48 hours and work with you to address the issue.

---

## 🏦 About Firefly

**Firefly** is an open-source core banking platform designed for modern financial institutions.

### Platform Features

- 🏢 **Multi-tenant Architecture** - Complete tenant isolation with shared infrastructure
- 🔧 **Microservices Design** - Scalable, maintainable, and independently deployable
- 🔌 **Provider Integrations** - Extensive third-party integrations (payment gateways, KYC, etc.)
- 🎨 **Customizable** - Flexible configuration and extensibility
- 🔒 **Enterprise Security** - Bank-grade security features and compliance
- ✅ **Compliance Ready** - PCI DSS, GDPR, SOC 2, ISO 27001 support
- 📊 **Analytics & Reporting** - Built-in analytics and compliance reporting
- 🌍 **Multi-currency** - Support for multiple currencies and exchange rates

### Other Firefly Microservices

- **firefly-account-service** - Account management and transactions
- **firefly-payment-service** - Payment processing and gateway integrations
- **firefly-customer-service** - Customer onboarding and KYC
- **firefly-loan-service** - Loan origination and servicing
- **firefly-card-service** - Card issuance and management
- **firefly-security-vault** - Credential management (this microservice)

Learn more at [getfirefly.io](https://getfirefly.io)

---

## 🎯 Roadmap

### Planned Features

- [ ] **Multi-region support** - Deploy across multiple regions with automatic failover
- [ ] **Credential templates** - Pre-configured templates for common credential types
- [ ] **Webhook notifications** - Real-time notifications for rotation, expiration, etc.
- [ ] **GraphQL API** - Alternative API for more flexible queries
- [ ] **CLI tool** - Command-line interface for credential management
- [ ] **Terraform provider** - Manage credentials via Terraform
- [ ] **Kubernetes operator** - Native Kubernetes integration
- [ ] **LDAP/AD integration** - Sync credentials from LDAP/Active Directory
- [ ] **Secrets scanning** - Detect leaked credentials in code repositories
- [ ] **Compliance reports** - Automated compliance reporting (PCI DSS, SOC 2, etc.)

### Version History

- **v1.0.0** (Current) - Initial release with core features
  - Multi-KMS provider support (AWS, Azure, HashiCorp, Google)
  - AES-256-GCM encryption
  - Resilience patterns (Circuit Breaker, Rate Limiter, Retry)
  - Complete audit trail
  - Automatic rotation
  - REST API + Java SDK

---

**Made with ❤️ by Firefly Software Solutions Inc**
Copyright © 2025 Firefly Software Solutions Inc. All rights reserved.
