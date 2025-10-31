# Production Deployment Guide

## Overview

This guide covers deploying the Firefly Security Vault to production environments with best practices for security, scalability, and reliability.

## Deployment Options

### 1. Docker Container

#### Build Docker Image

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY common-platform-security-vault-web/target/*.jar app.jar

# Create non-root user
RUN addgroup -S firefly && adduser -S firefly -G firefly
USER firefly

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Build and Run

```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t firefly-security-vault:1.0.0 .

# Run container
docker run -d \
  --name firefly-security-vault \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e FIREFLY_SECURITY_VAULT_ENCRYPTION_PROVIDER=AWS_KMS \
  -e FIREFLY_SECURITY_VAULT_ENCRYPTION_MASTER_KEY_ID=arn:aws:kms:us-east-1:123456789012:key/prod-key \
  firefly-security-vault:1.0.0
```

### 2. Kubernetes Deployment

#### Deployment Manifest

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: firefly-security-vault
  namespace: firefly
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
      serviceAccountName: firefly-security-vault
      containers:
      - name: firefly-security-vault
        image: firefly-security-vault:1.0.0
        ports:
        - containerPort: 8081
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: FIREFLY_SECURITY_VAULT_ENCRYPTION_PROVIDER
          value: "AWS_KMS"
        - name: FIREFLY_SECURITY_VAULT_ENCRYPTION_MASTER_KEY_ID
          valueFrom:
            secretKeyRef:
              name: kms-config
              key: master-key-id
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: firefly-security-vault
  namespace: firefly
spec:
  selector:
    app: firefly-security-vault
  ports:
  - port: 80
    targetPort: 8081
    name: http
  type: ClusterIP
```

#### ConfigMap and Secrets

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: firefly-security-vault-config
  namespace: firefly
data:
  application.yaml: |
    firefly:
      security:
        vault:
          encryption:
            provider: AWS_KMS
            aws-kms:
              region: us-east-1
---
apiVersion: v1
kind: Secret
metadata:
  name: kms-config
  namespace: firefly
type: Opaque
stringData:
  master-key-id: arn:aws:kms:us-east-1:123456789012:key/prod-key
```

### 3. AWS ECS/Fargate

#### Task Definition

```json
{
  "family": "firefly-security-vault",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "taskRoleArn": "arn:aws:iam::123456789012:role/firefly-security-vault-task-role",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "firefly-security-vault",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/firefly-security-vault:1.0.0",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "FIREFLY_SECURITY_VAULT_ENCRYPTION_PROVIDER",
          "value": "AWS_KMS"
        }
      ],
      "secrets": [
        {
          "name": "FIREFLY_SECURITY_VAULT_ENCRYPTION_MASTER_KEY_ID",
          "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789012:secret:kms-key-id"
        }
      ],
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      },
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/firefly-security-vault",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

## Database Setup

### PostgreSQL Configuration

#### Production Database

```sql
-- Create database
CREATE DATABASE firefly_security_vault_prod;

-- Create user with strong password
CREATE USER firefly_prod WITH PASSWORD 'STRONG_PASSWORD_HERE';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE firefly_security_vault_prod TO firefly_prod;

-- Connect to database
\c firefly_security_vault_prod

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO firefly_prod;
```

#### Connection Configuration

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://db.example.com:5432/firefly_security_vault_prod
    username: firefly_prod
    password: ${DB_PASSWORD}
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 30m
      validation-query: SELECT 1
```

### Database Migration

Flyway runs automatically on startup. For manual control:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
    out-of-order: false
```

## Environment Configuration

### Production application.yaml

```yaml
spring:
  profiles:
    active: prod
  
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

server:
  port: 8081
  shutdown: graceful

firefly:
  security:
    vault:
      encryption:
        provider: ${KMS_PROVIDER}
        master-key-id: ${KMS_MASTER_KEY_ID}
        
        aws-kms:
          region: ${AWS_REGION}
      
      resilience:
        enabled: true
        circuit-breaker:
          failure-rate-threshold: 50
          wait-duration-in-open-state: 60
        rate-limiter:
          limit-for-period: 100
        retry:
          max-attempts: 3

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.firefly: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

## Security Hardening

### 1. Network Security

- Use HTTPS/TLS for all connections
- Restrict database access to application subnet
- Use VPC/private subnets
- Configure security groups/firewall rules

### 2. Secrets Management

Never hardcode secrets. Use:

- **AWS**: AWS Secrets Manager or Parameter Store
- **Azure**: Azure Key Vault
- **Kubernetes**: Kubernetes Secrets
- **HashiCorp**: Vault

### 3. IAM/RBAC

- Use least privilege principle
- Separate roles for dev/staging/prod
- Enable MFA for administrative access
- Rotate credentials regularly

## Monitoring Setup

### Prometheus Metrics

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'firefly-security-vault'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['firefly-security-vault:8081']
```

### Grafana Dashboard

Import the provided dashboard:
- [Grafana Dashboard JSON](../monitoring/grafana-dashboard.json)

### Alerts

Configure alerts for:
- High error rate
- Circuit breaker open
- Database connection failures
- KMS API errors

## Scaling

### Horizontal Scaling

The application is stateless and can be scaled horizontally:

```bash
# Kubernetes
kubectl scale deployment firefly-security-vault --replicas=5

# Docker Swarm
docker service scale firefly-security-vault=5
```

### Vertical Scaling

Adjust resources based on load:

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

## Backup and Recovery

### Database Backups

```bash
# Automated daily backups
pg_dump -h db.example.com -U firefly_prod firefly_security_vault_prod > backup_$(date +%Y%m%d).sql

# Restore
psql -h db.example.com -U firefly_prod firefly_security_vault_prod < backup_20251031.sql
```

### KMS Key Backup

- AWS KMS: Keys are automatically backed up by AWS
- Azure Key Vault: Enable soft-delete and purge protection
- HashiCorp Vault: Configure auto-unseal and backup policies

## Health Checks

### Liveness Probe

```bash
curl http://localhost:8081/actuator/health/liveness
```

### Readiness Probe

```bash
curl http://localhost:8081/actuator/health/readiness
```

### Custom Health Indicators

The application includes:
- Database connectivity check
- KMS provider health check
- Circuit breaker status

## Troubleshooting

See [Troubleshooting Guide](troubleshooting.md) for common issues and solutions.

## Next Steps

- [Monitoring Guide](monitoring.md)
- [Troubleshooting Guide](troubleshooting.md)
- [Performance Tuning](performance.md)

