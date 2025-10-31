# Development Guide

## Getting Started

This guide will help you set up your development environment and contribute to the Firefly Security Vault.

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **PostgreSQL 14+** (or Docker)
- **Git**
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code

## Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/common-platform-security-vault.git
cd common-platform-security-vault
```

### 2. Start PostgreSQL

#### Using Docker

```bash
docker run -d \
  --name postgres-firefly-dev \
  -e POSTGRES_DB=firefly_vault_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14-alpine
```

#### Using Local PostgreSQL

```sql
CREATE DATABASE firefly_vault_dev;
CREATE USER firefly_dev WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE firefly_vault_dev TO firefly_dev;
```

### 3. Configure Application

Create `common-platform-security-vault-web/src/main/resources/application-dev.yaml`:

```yaml
spring:
  profiles:
    active: dev
  
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/firefly_vault_dev
    username: postgres
    password: postgres

firefly:
  security:
    vault:
      encryption:
        provider: IN_MEMORY
        master-key-id: dev-master-key

logging:
  level:
    root: INFO
    com.firefly: DEBUG
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
cd common-platform-security-vault-web
mvn spring-boot:run -Dspring.profiles.active=dev
```

The application will start on `http://localhost:8081`

## Project Structure

```
common-platform-security-vault/
├── common-platform-security-vault-models/
│   ├── src/main/java/
│   │   └── com/firefly/common/security/vault/models/
│   │       ├── entities/          # JPA/R2DBC entities
│   │       └── repositories/      # R2DBC repositories
│   └── src/main/resources/
│       └── db/migration/          # Flyway migrations
│
├── common-platform-security-vault-interfaces/
│   └── src/main/java/
│       └── com/firefly/common/security/vault/interfaces/
│           └── dtos/              # Request/Response DTOs
│
├── common-platform-security-vault-core/
│   ├── src/main/java/
│   │   └── com/firefly/common/security/vault/core/
│   │       ├── ports/             # Port interfaces
│   │       ├── adapters/          # Adapter implementations
│   │       ├── services/          # Business logic
│   │       ├── config/            # Configuration classes
│   │       ├── health/            # Health indicators
│   │       └── metrics/           # Metrics collectors
│   └── src/test/java/             # Unit tests
│
├── common-platform-security-vault-web/
│   ├── src/main/java/
│   │   └── com/firefly/common/security/vault/web/
│   │       ├── controllers/       # REST controllers
│   │       ├── exception/         # Exception handlers
│   │       └── Application.java   # Main application
│   └── src/test/java/             # Integration tests
│
└── common-platform-security-vault-sdk/
    └── Generated client library
```

## Coding Standards

### Java Style Guide

Follow Google Java Style Guide with these modifications:

- **Indentation**: 4 spaces
- **Line length**: 120 characters
- **Imports**: No wildcard imports

### Naming Conventions

- **Classes**: PascalCase (e.g., `CredentialService`)
- **Methods**: camelCase (e.g., `encryptCredential`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.firefly.common.security.vault`)

### Code Formatting

Use the provided code formatter:

```bash
mvn spotless:apply
```

## Testing

### Unit Tests

Run unit tests:

```bash
mvn test
```

Write unit tests for all business logic:

```java
@Test
@DisplayName("Should encrypt credential successfully")
void shouldEncryptCredential() {
    // Arrange
    String plaintext = "secret-value";
    String keyId = "test-key";
    
    // Act
    StepVerifier.create(credentialEncryptionPort.encryptCredential(plaintext, keyId))
        // Assert
        .expectNextMatches(encrypted -> encrypted != null && !encrypted.equals(plaintext))
        .verifyComplete();
}
```

### Integration Tests

Run integration tests:

```bash
mvn verify
```

Use `@SpringBootTest` for integration tests:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "firefly.security.vault.encryption.provider=IN_MEMORY"
})
class CredentialServiceIntegrationTest {
    @Autowired
    private CredentialService credentialService;
    
    @Test
    void shouldCreateAndRetrieveCredential() {
        // Test with real Spring context
    }
}
```

### Test Coverage

Generate coverage report:

```bash
mvn jacoco:report
```

View report at: `target/site/jacoco/index.html`

**Target**: 80% code coverage

## Building

### Build All Modules

```bash
mvn clean install
```

### Build Specific Module

```bash
mvn clean install -pl common-platform-security-vault-core
```

### Skip Tests

```bash
mvn clean install -DskipTests
```

### Build Docker Image

```bash
mvn clean package -DskipTests
docker build -t firefly-security-vault:dev .
```

## Debugging

### IntelliJ IDEA

1. Create a new **Spring Boot** run configuration
2. Set **Main class**: `com.firefly.common.security.vault.web.Application`
3. Set **VM options**: `-Dspring.profiles.active=dev`
4. Set **Working directory**: `$MODULE_WORKING_DIR$`
5. Click **Debug**

### VS Code

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Firefly Security Vault",
      "request": "launch",
      "mainClass": "com.firefly.common.security.vault.web.Application",
      "projectName": "common-platform-security-vault-web",
      "args": "--spring.profiles.active=dev"
    }
  ]
}
```

## Adding a New KMS Provider

### 1. Create Adapter Class

```java
@Component
@ConditionalOnProperty(name = "firefly.security.vault.encryption.provider", havingValue = "MY_KMS")
@ConditionalOnClass(name = "com.example.MyKmsClient")
public class MyKmsKeyManagementAdapter implements KeyManagementPort {
    
    private final MyKmsClient kmsClient;
    
    public MyKmsKeyManagementAdapter(SecurityVaultProperties properties) {
        this.kmsClient = MyKmsClient.builder()
            .endpoint(properties.getEncryption().getMyKms().getEndpoint())
            .build();
    }
    
    @Override
    public Mono<byte[]> encrypt(byte[] plaintext, String keyId, Map<String, String> context) {
        return Mono.fromCallable(() -> {
            // Implement encryption
            return kmsClient.encrypt(plaintext, keyId);
        });
    }
    
    // Implement other methods...
}
```

### 2. Add Configuration Properties

```java
@Data
public static class MyKmsProperties {
    private String endpoint;
    private String apiKey;
}
```

### 3. Add Maven Dependency

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-kms-sdk</artifactId>
    <version>1.0.0</version>
    <optional>true</optional>
</dependency>
```

### 4. Write Tests

```java
@Test
void shouldEncryptWithMyKms() {
    MyKmsKeyManagementAdapter adapter = new MyKmsKeyManagementAdapter(properties);
    
    StepVerifier.create(adapter.encrypt("test".getBytes(), "key-id", Map.of()))
        .expectNextMatches(encrypted -> encrypted != null)
        .verifyComplete();
}
```

### 5. Update Documentation

Add configuration guide to `docs/configuration/my-kms.md`

## Git Workflow

### Branch Naming

- **Feature**: `feature/add-new-kms-provider`
- **Bug Fix**: `bugfix/fix-encryption-error`
- **Hotfix**: `hotfix/security-patch`

### Commit Messages

Follow Conventional Commits:

```
feat: add Google Cloud KMS provider
fix: resolve concurrent modification in InMemoryAdapter
docs: update AWS KMS configuration guide
test: add integration tests for credential rotation
refactor: extract encryption logic to separate class
```

### Pull Request Process

1. Create feature branch from `main`
2. Make changes and commit
3. Push branch and create PR
4. Ensure CI passes
5. Request review
6. Address feedback
7. Merge when approved

## Continuous Integration

### GitHub Actions

The project uses GitHub Actions for CI:

```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn clean verify
```

## Troubleshooting

### Build Failures

**Error**: `Cannot find symbol`

**Solution**: Run `mvn clean install` to rebuild all modules

### Test Failures

**Error**: `Connection refused to PostgreSQL`

**Solution**: Ensure PostgreSQL is running on port 5432

### IDE Issues

**Error**: `Cannot resolve symbol`

**Solution**: Reimport Maven project in your IDE

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC Documentation](https://r2dbc.io/spec/1.0.0.RELEASE/spec/html/)
- [Resilience4j](https://resilience4j.readme.io/)

## Next Steps

- [Contributing Guide](contributing.md)
- [Testing Guide](testing.md)
- [Code Style](code-style.md)
- [Release Process](release-process.md)

