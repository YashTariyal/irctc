# 🔒 User-Based Rate Limiting Implementation Guide

## 🎯 Overview

The API Gateway now supports **user-based rate limiting** that extracts user IDs from JWT tokens and applies rate limits per authenticated user. This provides fair resource allocation and prevents abuse by individual users.

---

## ✨ Features

### 1. **JWT Token-Based User Identification**
- Extracts user ID from JWT token claims (`sub`, `username`, `user_id`)
- Works with OAuth2AuthenticationFilter that sets `X-User-Id` header
- Falls back to IP-based rate limiting for unauthenticated requests

### 2. **Per-User Rate Limits**
- Each authenticated user has their own rate limit quota
- Different limits for different services based on criticality
- Prevents one user from consuming all available resources

### 3. **Graceful Fallback**
- Unauthenticated requests use IP-based rate limiting
- No breaking changes for existing clients

---

## 🔧 Configuration

### Rate Limiter Key Resolver

The `userKeyResolver` bean extracts user ID in this priority order:

1. **X-User-Id Header** (set by OAuth2AuthenticationFilter)
2. **JWT `sub` claim** (subject/user ID)
3. **JWT `username` claim**
4. **JWT `user_id` claim**
5. **JWT ID (`jti`)** as fallback
6. **IP address** if no authentication

### Current Rate Limits

| Service | Replenish Rate | Burst Capacity | Description |
|---------|---------------|----------------|-------------|
| **Default (Global)** | 30/min | 60 | Global rate limit for all routes |
| **User Service** | 20/min | 40 | User management operations |
| **Train Service** | 20/min | 40 | Train search and information |
| **Booking Service** | 10/min | 20 | Booking operations (critical) |
| **Payment Service** | 5/min | 10 | Payment processing (very critical) |
| **Notification Service** | 15/min | 30 | Notification operations |

**Rate Limit Explanation:**
- **Replenish Rate**: Tokens added per second (converted to per minute for clarity)
- **Burst Capacity**: Maximum tokens available at once
- **Requested Tokens**: Tokens consumed per request (default: 1)

---

## 📋 How It Works

### 1. **Authenticated Request Flow**

```
Client Request
    ↓
[Authorization: Bearer <JWT>]
    ↓
OAuth2AuthenticationFilter
    ↓
Validates JWT & Extracts User ID
    ↓
Sets X-User-Id Header
    ↓
RateLimiterFilter (userKeyResolver)
    ↓
Extracts "user-{userId}" as key
    ↓
Checks Redis for user's rate limit
    ↓
Allow/Deny Request
```

### 2. **Unauthenticated Request Flow**

```
Client Request
    ↓
No Authorization Header
    ↓
RateLimiterFilter (userKeyResolver)
    ↓
No user ID found
    ↓
Falls back to IP-based resolver
    ↓
Uses IP address as key
    ↓
Checks Redis for IP's rate limit
    ↓
Allow/Deny Request
```

---

## 🚀 Usage Examples

### Authenticated Request

```bash
# Request with JWT token
curl -H "Authorization: Bearer <jwt-token>" \
     http://localhost:8090/api/v1/bookings

# Rate limit is applied per user ID extracted from JWT
```

### Unauthenticated Request

```bash
# Request without authentication
curl http://localhost:8090/api/trains

# Rate limit is applied per IP address
```

---

## 🔍 Monitoring

### Check Rate Limit Status

```bash
# Check Redis keys for rate limiting
redis-cli
> KEYS *ratelimit*
> GET ratelimit:user-12345
```

### Rate Limit Headers in Response

When rate limited, the gateway returns:
- **HTTP 429 Too Many Requests**
- Headers:
  - `X-RateLimit-Remaining`: Remaining requests
  - `X-RateLimit-Replenish-After`: Seconds until next token
  - `X-RateLimit-Burst-Capacity`: Maximum burst capacity

---

## ⚙️ Customization

### Adjust Rate Limits per Service

Edit `irctc-api-gateway/src/main/resources/application.yml`:

```yaml
routes:
  - id: booking-service
    filters:
      - name: RequestRateLimiter
        args:
          key-resolver: '#{@userKeyResolver}'
          redis-rate-limiter.replenishRate: 20  # Increase to 20/min
          redis-rate-limiter.burstCapacity: 40  # Increase burst to 40
```

### Change Default Global Rate Limit

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            key-resolver: '#{@userKeyResolver}'
            redis-rate-limiter.replenishRate: 50  # 50 requests/min
            redis-rate-limiter.burstCapacity: 100  # Burst of 100
```

### Use Different Key Resolver

```yaml
# Use IP-based for specific route
- name: RequestRateLimiter
  args:
    key-resolver: '#{@ipKeyResolver}'

# Use API key-based
- name: RequestRateLimiter
  args:
    key-resolver: '#{@apiKeyResolver}'
```

---

## 🧪 Testing

### Test User-Based Rate Limiting

```bash
# 1. Get JWT token (from login endpoint)
TOKEN=$(curl -X POST http://localhost:8090/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}' | jq -r '.token')

# 2. Make multiple requests with same token
for i in {1..25}; do
  curl -H "Authorization: Bearer $TOKEN" \
       http://localhost:8090/api/v1/bookings
  echo "Request $i"
done

# Should see 429 after rate limit exceeded
```

### Test IP-Based Fallback

```bash
# Make requests without authentication
for i in {1..35}; do
  curl http://localhost:8090/api/trains
  echo "Request $i"
done

# Should see 429 after IP rate limit exceeded
```

---

## 🔐 Security Considerations

### 1. **Token Validation**
- JWT tokens are validated before rate limiting
- Invalid tokens are rejected before rate limit check
- Expired tokens cannot bypass rate limits

### 2. **User ID Extraction**
- User ID is extracted from validated JWT claims
- No user information is logged in rate limit keys
- Rate limit keys are prefixed with "user-" for identification

### 3. **IP Fallback**
- Unauthenticated requests use IP-based limiting
- Prevents abuse from unauthenticated clients
- IP addresses are hashed for privacy (optional)

---

## 🐛 Troubleshooting

### Rate Limiting Not Working

1. **Check Redis Connection**
   ```bash
   redis-cli ping
   # Should return: PONG
   ```

2. **Verify JWT Decoder**
   - Check if `JwtDecoder` bean is configured
   - Verify OAuth2 issuer URI is correct

3. **Check Logs**
   ```bash
   tail -f logs/irctc-api-gateway.log | grep -i "rate"
   ```

### User ID Not Extracted

1. **Verify JWT Claims**
   - Check if JWT contains `sub`, `username`, or `user_id` claim
   - Verify token is valid and not expired

2. **Check Headers**
   ```bash
   curl -v -H "Authorization: Bearer <token>" \
        http://localhost:8090/api/v1/bookings
   # Look for X-User-Id header in response
   ```

### Rate Limits Too Strict/Loose

- Adjust `replenishRate` and `burstCapacity` in `application.yml`
- Monitor actual usage patterns
- Consider different limits for different user roles

---

## 📊 Benefits

1. **Fair Resource Allocation**
   - Each user gets equal quota
   - Prevents single user from monopolizing resources

2. **Better API Management**
   - Per-user quotas enable better API usage tracking
   - Can implement tiered rate limits (free/premium users)

3. **Security**
   - Prevents abuse by authenticated users
   - Works alongside IP-based limiting for unauthenticated requests

4. **Scalability**
   - Redis-based storage scales horizontally
   - No single point of failure

---

## 🔄 Migration from IP-Based

If you're migrating from IP-based to user-based:

1. **Gradual Rollout**
   - Start with non-critical routes
   - Monitor rate limit rejections
   - Adjust limits based on usage

2. **Hybrid Approach**
   - Use user-based for authenticated routes
   - Keep IP-based for public routes
   - Configure per route as needed

3. **Monitoring**
   - Track rate limit rejections
   - Monitor Redis memory usage
   - Adjust limits based on metrics

---

## 📚 Related Documentation

- [Spring Cloud Gateway Rate Limiting](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestratelimiter-gatewayfilter-factory)
- [Redis Rate Limiter](https://github.com/spring-cloud/spring-cloud-gateway/blob/main/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/ratelimit/RedisRateLimiter.java)
- [OAuth2 JWT Token Claims](https://www.oauth.com/oauth2-servers/access-tokens/access-token-response/)

---

*Last Updated: November 2025*

