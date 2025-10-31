# Architecture Overview

## Introduction

The Firefly Security Vault is built using **Hexagonal Architecture** (also known as Ports and Adapters pattern), which provides a clean separation between business logic and infrastructure concerns. This architectural style makes the system highly testable, maintainable, and adaptable to changing requirements.

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                           │
│                      (REST API Controllers)                          │
│                                                                       │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │ Credential │  │  Rotation  │  │   Audit    │  │   Share    │   │
│  │ Controller │  │ Controller │  │ Controller │  │ Controller │   │
│  └────────────┘  └────────────┘  └────────────┘  └────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        Application Layer                             │
│                      (Service Orchestration)                         │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │              Business Services                                 │ │
│  │  • CredentialServiceImpl                                       │ │
│  │  • CredentialRotationServiceImpl                               │ │
│  │  • CredentialAccessLogServiceImpl                              │ │
│  │  • CredentialShareServiceImpl                                  │ │
│  └────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│                          Domain Layer                                │
│                    (Business Logic & Ports)                          │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Ports (Interfaces)                        │   │
│  │                                                               │   │
│  │  ┌─────────────────────┐    ┌──────────────────────────┐   │   │
│  │  │ KeyManagementPort   │    │ CredentialEncryptionPort │   │   │
│  │  │                     │    │                          │   │   │
│  │  │ • encrypt()         │    │ • encryptCredential()    │   │   │
│  │  │ • decrypt()         │    │ • decryptCredential()    │   │   │
│  │  │ • generateDataKey() │    │ • rotateCredential()     │   │   │
│  │  │ • rotateKey()       │    │ • generateKey()          │   │   │
│  │  │ • validateKey()     │    │ • validateKey()          │   │   │
│  │  └─────────────────────┘    └──────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                            │
│                    (Adapters & Integrations)                         │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Key Management Adapters                         │   │
│  │                                                               │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │   │
│  │  │   AWS KMS    │  │    Azure     │  │  HashiCorp   │      │   │
│  │  │   Adapter    │  │   Adapter    │  │   Adapter    │      │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘      │   │
│  │                                                               │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │   │
│  │  │   Google     │  │  In-Memory   │  │  Resilient   │      │   │
│  │  │   Adapter    │  │   Adapter    │  │   Wrapper    │      │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Credential Encryption Adapter                   │   │
│  │                                                               │   │
│  │  ┌──────────────────────────────────────────────────────┐   │   │
│  │  │      AesGcmCredentialEncryptionAdapter               │   │   │
│  │  │      (AES-256-GCM with Envelope Encryption)          │   │   │
│  │  └──────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                  Data Persistence                            │   │
│  │                                                               │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │   │
│  │  │  PostgreSQL  │  │   Flyway     │  │    R2DBC     │      │   │
│  │  │  Repositories│  │  Migrations  │  │  Reactive    │      │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              Observability & Resilience                      │   │
│  │                                                               │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │   │
│  │  │  Micrometer  │  │    Health    │  │ Resilience4j │      │   │
│  │  │   Metrics    │  │  Indicators  │  │   Patterns   │      │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘      │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

## Key Architectural Principles

### 1. Hexagonal Architecture (Ports and Adapters)

The system is organized into distinct layers:

- **Domain Layer**: Contains business logic and port interfaces
- **Application Layer**: Orchestrates use cases and coordinates domain objects
- **Infrastructure Layer**: Implements ports with concrete adapters
- **Presentation Layer**: Exposes REST API endpoints

### 2. Dependency Inversion

Dependencies point inward toward the domain:

```
Infrastructure → Application → Domain
Presentation   → Application → Domain
```

The domain layer has no dependencies on external frameworks or libraries.

### 3. Separation of Concerns

Each layer has a specific responsibility:

| Layer | Responsibility | Examples |
|-------|---------------|----------|
| **Presentation** | HTTP handling, request/response mapping | REST Controllers |
| **Application** | Use case orchestration, transaction management | Service implementations |
| **Domain** | Business rules, domain logic | Ports, domain models |
| **Infrastructure** | External integrations, data persistence | Adapters, repositories |

## Module Structure

The project is organized into Maven modules:

```
common-platform-security-vault/
├── common-platform-security-vault-models/
│   ├── Domain entities (Credential, CredentialType, etc.)
│   ├── R2DBC repositories
│   └── Flyway database migrations
│
├── common-platform-security-vault-interfaces/
│   ├── DTOs (Data Transfer Objects)
│   ├── Request/Response models
│   └── API contracts
│
├── common-platform-security-vault-core/
│   ├── ports/              # Domain interfaces
│   ├── adapters/           # Infrastructure implementations
│   ├── services/           # Business logic
│   ├── config/             # Configuration classes
│   ├── health/             # Health indicators
│   └── metrics/            # Metrics collectors
│
├── common-platform-security-vault-web/
│   ├── controllers/        # REST API endpoints
│   ├── exception/          # Exception handlers
│   └── Application.java    # Spring Boot application
│
└── common-platform-security-vault-sdk/
    └── Generated client library
```

## Technology Stack

### Core Framework

- **Java 21** - Modern Java with virtual threads and pattern matching
- **Spring Boot 3.2.2** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Project Reactor** - Reactive programming library

### Data Layer

- **PostgreSQL 14+** - Relational database
- **R2DBC** - Reactive database connectivity
- **Flyway** - Database migration tool

### Security & Encryption

- **AWS KMS SDK 2.20.0** - AWS Key Management Service
- **Azure Key Vault SDK 4.6.0** - Azure Key Vault
- **HashiCorp Vault SDK 6.1.0** - HashiCorp Vault
- **Google Cloud KMS SDK 2.20.0** - Google Cloud KMS
- **Bouncy Castle** - Cryptographic library

### Resilience & Observability

- **Resilience4j 2.1.0** - Circuit breaker, rate limiter, retry
- **Micrometer** - Metrics collection
- **Spring Boot Actuator** - Health checks and monitoring

### Development Tools

- **Lombok** - Boilerplate reduction
- **MapStruct** - Object mapping
- **JUnit 5** - Testing framework
- **AssertJ** - Fluent assertions

## Design Patterns

### 1. Ports and Adapters (Hexagonal Architecture)

**Purpose**: Decouple business logic from infrastructure concerns

**Implementation**:
- `KeyManagementPort` - Port interface
- `AwsKmsKeyManagementAdapter` - AWS adapter
- `AzureKeyVaultKeyManagementAdapter` - Azure adapter

### 2. Decorator Pattern

**Purpose**: Add resilience capabilities without modifying core adapters

**Implementation**:
- `ResilientKeyManagementAdapter` wraps any `KeyManagementPort` implementation
- Adds circuit breaker, rate limiter, and retry logic

### 3. Strategy Pattern

**Purpose**: Select KMS provider at runtime based on configuration

**Implementation**:
- `@ConditionalOnProperty` annotations
- Spring's dependency injection selects the appropriate adapter

### 4. Repository Pattern

**Purpose**: Abstract data access logic

**Implementation**:
- R2DBC repositories for each entity
- Reactive Mono/Flux return types

### 5. DTO Pattern

**Purpose**: Separate API contracts from domain models

**Implementation**:
- Request/Response DTOs in `interfaces` module
- MapStruct for entity-DTO mapping

## Next Steps

- [Hexagonal Architecture Details](hexagonal-architecture.md)
- [Data Model](data-model.md)
- [Design Decisions](design-decisions.md)

