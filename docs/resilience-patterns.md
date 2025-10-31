# Resilience Patterns

## Overview

The Security Vault implements enterprise-grade resilience patterns using **Resilience4j** to ensure high availability and fault tolerance when interacting with external Key Management Systems (KMS).

## Implemented Patterns

### 1. Retry Pattern

**Purpose**: Automatically retry failed operations to handle transient failures.

**Configuration**:
```java
RetryConfig config = RetryConfig.custom()
    .maxAttempts(3)
    .intervalFunction(IntervalFunction.ofExponentialBackoff(
        Duration.ofSeconds(1), 2.0))
    .retryExceptions(
        RuntimeException.class,
        Exception.class
    )
    .build();
```

**Behavior**:
- **Max Attempts**: 3 (1 original + 2 retries)
- **Backoff Strategy**: Exponential backoff with multiplier 2.0
  - 1st retry: after 1 second
  - 2nd retry: after 2 seconds (1 * 2.0)
- **Retry On**: RuntimeException, Exception
- **Use Cases**: Network timeouts, temporary KMS unavailability, rate limiting errors

**Example**:
```
Attempt 1: Fails with network timeout
Wait 1 second...
Attempt 2: Fails with network timeout
Wait 2 seconds...
Attempt 3: Succeeds ✓
```

### 2. Circuit Breaker Pattern

**Purpose**: Prevent cascading failures by stopping requests to a failing service.

**Configuration**:
```java
CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .failureRateThreshold(50.0f)
    .waitDurationInOpenState(Duration.ofSeconds(60))
    .slidingWindowSize(10)
    .minimumNumberOfCalls(5)
    .permittedNumberOfCallsInHalfOpenState(3)
    .automaticTransitionFromOpenToHalfOpenEnabled(true)
    .build();
```

**States**:
1. **CLOSED** (Normal): All requests pass through
2. **OPEN** (Failing): All requests fail immediately without calling KMS
3. **HALF_OPEN** (Testing): Limited requests allowed to test if service recovered

**Behavior**:
- **Failure Rate Threshold**: 50% - Opens circuit if 50% of calls fail
- **Sliding Window**: Last 10 calls
- **Minimum Calls**: 5 calls before calculating failure rate
- **Wait Duration**: 60 seconds in OPEN state before transitioning to HALF_OPEN
- **Half-Open Calls**: 3 test calls allowed in HALF_OPEN state

**State Transitions**:
```
CLOSED --[50% failures]--> OPEN
OPEN --[60 seconds]--> HALF_OPEN
HALF_OPEN --[3 successes]--> CLOSED
HALF_OPEN --[1 failure]--> OPEN
```

### 3. Rate Limiter Pattern

**Purpose**: Protect against excessive API calls to KMS providers.

**Configuration**:
```java
RateLimiterConfig config = RateLimiterConfig.custom()
    .limitForPeriod(100)
    .limitRefreshPeriod(Duration.ofSeconds(1))
    .timeoutDuration(Duration.ofSeconds(5))
    .build();
```

**Behavior**:
- **Limit**: 100 calls per second
- **Refresh Period**: 1 second
- **Timeout**: 5 seconds (wait time for permission)
- **Use Cases**: Prevent hitting KMS provider rate limits, control costs

## Pattern Application Order

The resilience patterns are applied in the following order:

```java
delegate.encrypt(plaintext, keyId, encryptionContext)
    .transformDeferred(RetryOperator.of(retry))           // 1. Retry first
    .transformDeferred(RateLimiterOperator.of(rateLimiter)) // 2. Then rate limit
    .transformDeferred(CircuitBreakerOperator.of(circuitBreaker)) // 3. Finally circuit breaker
```

**Why this order?**

1. **Retry First**: Handles transient failures before other patterns
2. **Rate Limiter Second**: Ensures we don't exceed rate limits even with retries
3. **Circuit Breaker Last**: Prevents cascading failures at the system level

## Usage Example

```java
@Service
public class MyService {
    
    private final KeyManagementPort keyManagement;
    
    public Mono<EncryptionResult> encryptData(byte[] data) {
        // All resilience patterns are automatically applied
        return keyManagement.encrypt(data, "my-key-id", "context");
    }
}
```

## Monitoring

### Metrics

All resilience patterns expose metrics through Micrometer:

```java
// Circuit Breaker Metrics
CircuitBreaker.Metrics cbMetrics = circuitBreaker.getMetrics();
cbMetrics.getNumberOfSuccessfulCalls();
cbMetrics.getNumberOfFailedCalls();
cbMetrics.getFailureRate();

// Rate Limiter Metrics
RateLimiter.Metrics rlMetrics = rateLimiter.getMetrics();
rlMetrics.getAvailablePermissions();
rlMetrics.getNumberOfWaitingThreads();

// Retry Metrics
Retry.Metrics retryMetrics = retry.getMetrics();
retryMetrics.getNumberOfSuccessfulCallsWithRetryAttempt();
retryMetrics.getNumberOfFailedCallsWithRetryAttempt();
```

### Event Listeners

The configuration includes event listeners for monitoring:

```java
// Circuit Breaker Events
circuitBreaker.getEventPublisher()
    .onStateTransition(event ->
        log.warn("Circuit Breaker state transition: {} -> {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState())
    );

// Retry Events
retry.getEventPublisher()
    .onRetry(event ->
        log.warn("Retry attempt {} for KMS operation: {}",
            event.getNumberOfRetryAttempts(),
            event.getLastThrowable().getMessage())
    );

// Rate Limiter Events
rateLimiter.getEventPublisher()
    .onFailure(event ->
        log.warn("Rate limiter: Call rejected - too many requests")
    );
```

## Testing Resilience Patterns

### Unit Tests

The resilience patterns are tested in `ResilientKeyManagementAdapterTest`:

```java
@Test
void shouldHandleErrorsWithResiliencePatterns() {
    // Simulate error
    when(delegateAdapter.encrypt(plaintext, keyId, context))
        .thenReturn(Mono.error(new RuntimeException("Error")));

    // Verify error is propagated after resilience patterns
    StepVerifier.create(resilientAdapter.encrypt(plaintext, keyId, context))
        .expectError(RuntimeException.class)
        .verify();
}
```

### Integration Tests

For real retry behavior testing, use integration tests with actual KMS providers or test doubles that support multiple invocations.

## Best Practices

1. **Don't Retry on Client Errors**: Only retry on transient failures (network, timeout, 5xx errors)
2. **Use Exponential Backoff**: Prevents overwhelming the failing service
3. **Monitor Circuit Breaker State**: Alert when circuit opens
4. **Set Appropriate Timeouts**: Balance between user experience and retry attempts
5. **Test Failure Scenarios**: Regularly test circuit breaker and retry behavior

## Configuration Properties

You can customize resilience patterns through application properties:

```yaml
resilience4j:
  retry:
    instances:
      kms-operations:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2.0
        
  circuitbreaker:
    instances:
      kms-operations:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        sliding-window-size: 10
        
  ratelimiter:
    instances:
      kms-operations:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 5s
```

## Troubleshooting

### Circuit Breaker Stuck in OPEN State

**Symptoms**: All requests fail immediately with `CallNotPermittedException`

**Solutions**:
1. Check KMS provider health
2. Review logs for root cause of failures
3. Manually reset circuit breaker if needed
4. Adjust `waitDurationInOpenState` if too long

### Too Many Retries

**Symptoms**: High latency, excessive API calls

**Solutions**:
1. Reduce `maxAttempts`
2. Increase `intervalFunction` backoff
3. Add specific exceptions to `ignoreExceptions`

### Rate Limiter Rejecting Calls

**Symptoms**: `RequestNotPermitted` exceptions

**Solutions**:
1. Increase `limitForPeriod`
2. Reduce concurrent requests
3. Implement request queuing
4. Scale horizontally

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Retry Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/retry)
- [Rate Limiting Pattern](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)

