# Rate Limiting Implementation

## Current Implementation

✅ **In-Memory Rate Limiting** - Implemented using token bucket algorithm
- Tracks requests per IP address
- Configurable limit per minute
- Automatic token refill
- Standard HTTP 429 responses with retry headers

⚠️ **Limitation**: In-memory implementation only works for single-instance deployments

## Configuration

```yaml
firefly:
  security:
    vault:
      access-control:
        enable-rate-limiting: true
        rate-limit-per-minute: 100  # Requests per minute per client
```

## Response Headers

When rate limiting is active, the following headers are included:

- `X-RateLimit-Limit`: Maximum requests allowed per minute
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Unix timestamp when the limit resets
- `Retry-After`: Seconds to wait before retrying (when limit exceeded)

## Production Implementation with Redis

For distributed deployments, implement Redis-based rate limiting:

### 1. Add Dependencies

Add to `common-platform-security-vault-web/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.7.0</version>
</dependency>
```

### 2. Create Redis Rate Limiting Filter

```java
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "firefly.security.vault.access-control",
    name = "enable-rate-limiting",
    havingValue = "true"
)
public class RedisRateLimitingFilter implements WebFilter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final SecurityVaultProperties properties;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientId = getClientIdentifier(exchange);
        String key = "rate-limit:" + clientId;
        int limit = properties.getAccessControl().getRateLimitPerMinute();
        
        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    // First request in window, set expiration
                    return redisTemplate.expire(key, Duration.ofMinutes(1))
                        .thenReturn(count);
                }
                return Mono.just(count);
            })
            .flatMap(count -> {
                if (count <= limit) {
                    // Within limit
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(limit - count));
                    return chain.filter(exchange);
                } else {
                    // Exceeded limit
                    log.warn("Rate limit exceeded for client: {}", clientId);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                    return exchange.getResponse().setComplete();
                }
            });
    }
    
    private String getClientIdentifier(ServerWebExchange exchange) {
        // Same implementation as in-memory version
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
}
```

### 3. Configure Redis

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 4. Advanced: Bucket4j with Redis

For more sophisticated rate limiting with multiple buckets:

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public ProxyManager<String> proxyManager(ReactiveRedisTemplate<String, byte[]> redisTemplate) {
        return new RedisProxyManager<>(
            redisTemplate,
            Duration.ofMinutes(1)
        );
    }
    
    @Bean
    public BucketConfiguration bucketConfiguration(SecurityVaultProperties properties) {
        int limit = properties.getAccessControl().getRateLimitPerMinute();
        
        return BucketConfiguration.builder()
            .addLimit(Bandwidth.simple(limit, Duration.ofMinutes(1)))
            .build();
    }
}

@Component
public class Bucket4jRateLimitingFilter implements WebFilter {
    
    private final ProxyManager<String> proxyManager;
    private final BucketConfiguration bucketConfiguration;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientId = getClientIdentifier(exchange);
        
        return Mono.fromCallable(() -> 
            proxyManager.builder()
                .build(clientId, bucketConfiguration)
                .tryConsume(1)
        ).flatMap(probe -> {
            if (probe.isConsumed()) {
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
                    String.valueOf(probe.getRemainingTokens()));
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("Retry-After", 
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
                return exchange.getResponse().setComplete();
            }
        });
    }
}
```

## Rate Limiting Strategies

### 1. Per-IP Rate Limiting (Current)
- Limits requests per IP address
- Good for public APIs
- Can be bypassed with multiple IPs

### 2. Per-User Rate Limiting
```java
private String getClientIdentifier(ServerWebExchange exchange) {
    // Get from JWT or session
    return exchange.getPrincipal()
        .map(Principal::getName)
        .block();
}
```

### 3. Per-Service Rate Limiting
```java
private String getClientIdentifier(ServerWebExchange exchange) {
    // Get from custom header
    return exchange.getRequest().getHeaders()
        .getFirst("X-Service-Name");
}
```

### 4. Tiered Rate Limiting
```java
public BucketConfiguration getBucketConfiguration(String tier) {
    return switch (tier) {
        case "premium" -> BucketConfiguration.builder()
            .addLimit(Bandwidth.simple(1000, Duration.ofMinutes(1)))
            .build();
        case "standard" -> BucketConfiguration.builder()
            .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
            .build();
        default -> BucketConfiguration.builder()
            .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
            .build();
    };
}
```

## Monitoring

Add metrics for rate limiting:

```java
@Component
public class RateLimitMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordRateLimitExceeded(String clientId) {
        meterRegistry.counter("vault.rate_limit.exceeded", 
            "client", clientId).increment();
    }
    
    public void recordRateLimitAllowed(String clientId) {
        meterRegistry.counter("vault.rate_limit.allowed", 
            "client", clientId).increment();
    }
}
```

## Testing

```java
@WebFluxTest
class RateLimitingFilterTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldAllowRequestsWithinLimit() {
        for (int i = 0; i < 100; i++) {
            webTestClient.get()
                .uri("/api/v1/credentials")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-RateLimit-Remaining");
        }
    }
    
    @Test
    void shouldRejectRequestsExceedingLimit() {
        for (int i = 0; i < 101; i++) {
            webTestClient.get()
                .uri("/api/v1/credentials")
                .exchange();
        }
        
        webTestClient.get()
            .uri("/api/v1/credentials")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader().exists("Retry-After");
    }
}
```

## Production Checklist

- [ ] Implement Redis-based rate limiting for distributed systems
- [ ] Configure appropriate rate limits per endpoint
- [ ] Add monitoring and alerting for rate limit violations
- [ ] Implement tiered rate limiting for different user types
- [ ] Add circuit breakers for Redis failures
- [ ] Document rate limits in API documentation
- [ ] Test rate limiting under load
- [ ] Configure rate limit bypass for internal services

