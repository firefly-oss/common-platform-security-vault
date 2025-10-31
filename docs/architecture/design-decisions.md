# Architectural Decision Records (ADRs)

## Overview

This document records the key architectural decisions made during the development of the Firefly Security Vault.

## ADR-001: Hexagonal Architecture

**Date**: 2025-10-15  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

We needed an architecture that would:
- Support multiple KMS providers without code changes
- Be highly testable
- Allow easy addition of new features
- Separate business logic from infrastructure

### Decision

Adopt **Hexagonal Architecture** (Ports and Adapters pattern).

### Rationale

**Pros**:
- ✅ Clear separation of concerns
- ✅ Easy to test (mock ports instead of concrete implementations)
- ✅ Flexible - can swap adapters without changing business logic
- ✅ Framework-independent domain layer

**Cons**:
- ❌ More initial complexity
- ❌ More files and interfaces
- ❌ Learning curve for developers unfamiliar with the pattern

### Consequences

- All KMS integrations implemented as adapters
- Business logic isolated in service layer
- Easy to add new KMS providers
- High test coverage achieved

---

## ADR-002: Reactive Programming with Spring WebFlux

**Date**: 2025-10-15  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need to handle high-throughput credential operations efficiently.

### Decision

Use **Spring WebFlux** with **Project Reactor** for reactive programming.

### Rationale

**Pros**:
- ✅ Non-blocking I/O for better resource utilization
- ✅ Handles high concurrency with fewer threads
- ✅ Natural fit for async KMS operations
- ✅ Backpressure support

**Cons**:
- ❌ Steeper learning curve
- ❌ Debugging can be more complex
- ❌ Not all libraries support reactive

### Consequences

- All port methods return `Mono<T>` or `Flux<T>`
- Use R2DBC for reactive database access
- Better performance under high load
- Requires reactive-aware testing (StepVerifier)

---

## ADR-003: Envelope Encryption

**Date**: 2025-10-16  
**Status**: Accepted  
**Decision Makers**: Security Team, Architecture Team

### Context

Direct KMS encryption for every credential would be:
- Expensive (KMS API costs)
- Slow (network latency)
- Limited (KMS request quotas)

### Decision

Implement **envelope encryption** pattern.

### Rationale

**How it works**:
1. Generate unique Data Encryption Key (DEK) per credential
2. Encrypt credential with DEK locally
3. Encrypt DEK with KMS master key
4. Store encrypted credential + encrypted DEK

**Pros**:
- ✅ Reduced KMS API calls (better performance, lower cost)
- ✅ Unique key per credential (better security)
- ✅ Fast local encryption/decryption
- ✅ Master key never leaves KMS

**Cons**:
- ❌ More complex implementation
- ❌ Larger storage footprint

### Consequences

- Implemented in `AesGcmCredentialEncryptionAdapter`
- Storage format: `{encryptedDEK}:{iv}:{ciphertext}:{authTag}`
- Significant cost savings on KMS operations

---

## ADR-004: AES-256-GCM Encryption

**Date**: 2025-10-16  
**Status**: Accepted  
**Decision Makers**: Security Team

### Context

Need a secure, industry-standard encryption algorithm.

### Decision

Use **AES-256-GCM** (Galois/Counter Mode).

### Rationale

**Pros**:
- ✅ Industry standard (NIST approved)
- ✅ Authenticated encryption (detects tampering)
- ✅ Fast (hardware acceleration available)
- ✅ 256-bit key size (strong security)
- ✅ 128-bit authentication tag

**Cons**:
- ❌ IV must never be reused with same key
- ❌ Requires careful implementation

### Consequences

- Generate unique 12-byte IV per encryption
- Use SecureRandom for IV generation
- Store IV alongside ciphertext
- Validate authentication tag on decryption

---

## ADR-005: Conditional Bean Loading

**Date**: 2025-10-17  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Don't want to force users to include all KMS SDKs if they only use one provider.

### Decision

Use **Spring's conditional bean loading** with optional dependencies.

### Rationale

**Implementation**:
```java
@Component
@ConditionalOnProperty(name = "firefly.security.vault.encryption.provider", havingValue = "AWS_KMS")
@ConditionalOnClass(name = "software.amazon.awssdk.services.kms.KmsAsyncClient")
public class AwsKmsKeyManagementAdapter implements KeyManagementPort {
    // ...
}
```

**Pros**:
- ✅ Only load beans for configured provider
- ✅ Smaller application footprint
- ✅ Faster startup time
- ✅ No unnecessary dependencies

**Cons**:
- ❌ More complex configuration
- ❌ Potential runtime errors if SDK missing

### Consequences

- All KMS adapters use `@ConditionalOnProperty`
- All KMS SDKs marked as `<optional>true</optional>` in pom.xml
- Clear error messages if SDK missing

---

## ADR-006: Resilience4j for Resilience Patterns

**Date**: 2025-10-18  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need to handle KMS failures gracefully and prevent cascading failures.

### Decision

Use **Resilience4j** for Circuit Breaker, Rate Limiter, and Retry patterns.

### Rationale

**Pros**:
- ✅ Lightweight library
- ✅ Reactive support (works with Project Reactor)
- ✅ Comprehensive metrics
- ✅ Easy configuration

**Cons**:
- ❌ Additional dependency
- ❌ Adds complexity

### Consequences

- Implemented `ResilientKeyManagementAdapter` as decorator
- Circuit breaker opens after 50% failure rate
- Rate limiter set to 100 requests/second
- Retry with exponential backoff (3 attempts)

---

## ADR-007: R2DBC for Database Access

**Date**: 2025-10-15  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need reactive database access to match Spring WebFlux.

### Decision

Use **R2DBC** (Reactive Relational Database Connectivity) with PostgreSQL.

### Rationale

**Pros**:
- ✅ Reactive, non-blocking database access
- ✅ Consistent with Spring WebFlux
- ✅ Better resource utilization
- ✅ PostgreSQL support

**Cons**:
- ❌ Less mature than JDBC
- ❌ Limited ORM features
- ❌ Smaller ecosystem

### Consequences

- All repositories extend `ReactiveCrudRepository`
- Use Flyway for database migrations
- Manual entity mapping (no JPA)

---

## ADR-008: Flyway for Database Migrations

**Date**: 2025-10-16  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need version-controlled database schema management.

### Decision

Use **Flyway** for database migrations.

### Rationale

**Pros**:
- ✅ Version-controlled migrations
- ✅ Automatic execution on startup
- ✅ Rollback support
- ✅ Works with R2DBC

**Cons**:
- ❌ Migrations run synchronously (blocking)

### Consequences

- Migration files in `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Automatic execution on application startup

---

## ADR-009: Multi-Module Maven Structure

**Date**: 2025-10-15  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need clear separation of concerns and reusable components.

### Decision

Use **multi-module Maven** structure.

### Rationale

**Modules**:
- `models` - Entities and repositories
- `interfaces` - DTOs and API contracts
- `core` - Business logic and adapters
- `web` - REST controllers
- `sdk` - Client library

**Pros**:
- ✅ Clear separation of concerns
- ✅ Reusable modules
- ✅ Independent versioning
- ✅ Smaller artifacts

**Cons**:
- ❌ More complex build
- ❌ Dependency management overhead

### Consequences

- Each module has its own pom.xml
- Parent pom manages versions
- Build all modules: `mvn clean install`

---

## ADR-010: Micrometer for Metrics

**Date**: 2025-10-18  
**Status**: Accepted  
**Decision Makers**: Architecture Team

### Context

Need comprehensive metrics for monitoring and alerting.

### Decision

Use **Micrometer** with **Spring Boot Actuator**.

### Rationale

**Pros**:
- ✅ Vendor-neutral metrics facade
- ✅ Supports Prometheus, Grafana, etc.
- ✅ Built-in Spring Boot integration
- ✅ Custom metrics support

**Cons**:
- ❌ Additional dependency

### Consequences

- Metrics exposed at `/actuator/prometheus`
- Custom metrics in `KeyManagementMetrics`
- Resilience4j metrics automatically exported

---

## Future Decisions

### Under Consideration

1. **GraphQL API** - Alternative to REST
2. **gRPC Support** - For high-performance inter-service communication
3. **Event Sourcing** - For complete audit trail
4. **CQRS** - Separate read/write models
5. **Kubernetes Operator** - For automated deployment and management

### Rejected

1. **JPA/Hibernate** - Rejected in favor of R2DBC for reactive support
2. **MongoDB** - Rejected in favor of PostgreSQL for ACID guarantees
3. **Synchronous API** - Rejected in favor of reactive for better performance

---

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Reactive Manifesto](https://www.reactivemanifesto.org/)
- [NIST Encryption Standards](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

