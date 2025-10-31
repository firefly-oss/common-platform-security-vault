# Firefly Security Vault - Documentation

Welcome to the comprehensive documentation for the Firefly Security Vault service.

## 📚 Documentation Structure

### 🏗️ Architecture

Understand the design and structure of the Security Vault:

- [**Architecture Overview**](architecture/README.md) - High-level architecture and design principles
- [**Hexagonal Architecture**](architecture/hexagonal-architecture.md) - Ports and Adapters pattern implementation
- [**Data Model**](architecture/data-model.md) - Database schema and entity relationships
- [**Design Decisions**](architecture/design-decisions.md) - Key architectural decisions and rationale

### ⚙️ Configuration

Configure the Security Vault for different environments and KMS providers:

- [**Configuration Overview**](configuration/README.md) - Configuration guide and best practices
- [**In-Memory Provider**](configuration/in-memory.md) - Development and testing setup
- [**AWS KMS**](configuration/aws-kms.md) - AWS Key Management Service configuration
- [**Azure Key Vault**](configuration/azure-key-vault.md) - Azure Key Vault configuration
- [**HashiCorp Vault**](configuration/hashicorp-vault.md) - HashiCorp Vault Transit Engine configuration
- [**Google Cloud KMS**](configuration/google-cloud-kms.md) - Google Cloud KMS configuration

### 🚀 Operations

Deploy, monitor, and maintain the Security Vault in production:

- [**Deployment Guide**](operations/deployment.md) - Production deployment strategies
- [**Monitoring & Observability**](operations/monitoring.md) - Metrics, health checks, and alerting
- [**Troubleshooting**](operations/troubleshooting.md) - Common issues and solutions
- [**Backup & Recovery**](operations/backup-recovery.md) - Data backup and disaster recovery
- [**Performance Tuning**](operations/performance.md) - Optimization and scaling

### 🔌 API Reference

Complete API documentation and examples:

- [**API Overview**](api/README.md) - REST API introduction
- [**Credentials API**](api/credentials.md) - Credential management endpoints
- [**Rotation API**](api/rotation.md) - Credential rotation endpoints
- [**Audit API**](api/audit.md) - Audit log endpoints
- [**API Examples**](api/examples.md) - Common use cases and code samples

### 🔒 Security

Security features and best practices:

- [**Security Overview**](security/README.md) - Security architecture and features
- [**Encryption**](security/encryption.md) - Encryption algorithms and key management
- [**Access Control**](security/access-control.md) - Authentication and authorization
- [**Audit & Compliance**](security/audit-compliance.md) - Audit trails and compliance reporting
- [**Best Practices**](security/best-practices.md) - Security recommendations

### 👨‍💻 Development

Guides for developers and contributors:

- [**Development Setup**](development/README.md) - Local development environment setup
- [**Contributing Guide**](development/contributing.md) - How to contribute to the project
- [**Testing Guide**](development/testing.md) - Writing and running tests
- [**Code Style**](development/code-style.md) - Coding standards and conventions
- [**Release Process**](development/release-process.md) - Versioning and release workflow

### 📖 Guides

Step-by-step guides for common tasks:

- [**Quick Start Guide**](guides/quick-start.md) - Get started in 5 minutes
- [**Migration Guide**](guides/migration.md) - Migrating from other secret management solutions
- [**Use Cases**](guides/use-cases.md) - Real-world usage examples
- [**FAQ**](guides/faq.md) - Frequently asked questions

## 🎯 Quick Links

### For Developers

- [Local Development Setup](development/README.md)
- [Running Tests](development/testing.md)
- [API Examples](api/examples.md)

### For DevOps/SRE

- [Production Deployment](operations/deployment.md)
- [Monitoring Setup](operations/monitoring.md)
- [Troubleshooting Guide](operations/troubleshooting.md)

### For Security Teams

- [Security Architecture](security/README.md)
- [Encryption Details](security/encryption.md)
- [Compliance Features](security/audit-compliance.md)

## 📝 Additional Resources

- [GitHub Repository](https://github.com/firefly-oss/common-platform-security-vault)
- [Issue Tracker](https://github.com/firefly-oss/common-platform-security-vault/issues)
- [Firefly Website](https://getfirefly.io)

## 🆘 Getting Help

If you can't find what you're looking for:

1. Check the [FAQ](guides/faq.md)
2. Search [existing issues](https://github.com/firefly-oss/common-platform-security-vault/issues)
3. Contact us at [dev@getfirefly.io](mailto:dev@getfirefly.io)
4. [Open a new issue](https://github.com/firefly-oss/common-platform-security-vault/issues/new)

---

**Made with ❤️ by Firefly Software Solutions Inc**

Copyright © 2025 Firefly Software Solutions Inc. All rights reserved.

**Last Updated**: October 31, 2025
**Version**: 1.0.0-SNAPSHOT

