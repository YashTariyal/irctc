# API Response Caching Implementation Guide

## Overview

Comprehensive API response caching implementation using Spring Cache with Redis backend. This provides significant performance improvements by reducing database load and improving response times.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Request   │───▶│  Service Layer  │───▶│   Cache Check   │
│                 │    │                 │    │   (Redis)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
                              │ Cache Miss             │ Cache Hit
                              ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Database      │    │  Return Cached  │
                       │   Query         │    │  Response        │
                       └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │  Store in Cache │
                       │  (Redis)        │
                       └─────────────────┘
```

## Implementation Details

### 1. Spring Cache Configuration

Each service has a `CacheConfig` class that:
- Enables caching with `@EnableCaching`
- Configures Redis as the cache backend
- Sets up cache-specific TTL values
- Configures serialization (JSON)

### 2. Cache Annotations

#### @Cacheable
- Caches method results
- Only executes method if cache miss
- Example: `@Cacheable(value = "bookings", key = "#id")`

#### @CacheEvict
- Removes entries from cache
- Used on create/update/delete operations
- Example: `@CacheEvict(value = "bookings", key = "#id")`

#### @CachePut
- Updates cache with new value
- Always executes method
- Example: `@CachePut(value = "users", key = "#userId")`

## Service-Specific Caching

### Booking Service

**Cached Operations:**
- `getBookingById(Long id)` → Cache: `bookings`, TTL: 15 minutes
- `getBookingByPnr(String pnrNumber)` → Cache: `bookings-by-pnr`, TTL: 30 minutes
- `getBookingsByUserId(Long userId)` → Cache: `bookings-by-user`, TTL: 10 minutes

**Cache Eviction:**
- `createBooking()` → Evicts `bookings-by-user` for the user
- `updateBooking()` → Evicts `bookings`, `bookings-by-pnr`, `bookings-by-user`
- `cancelBooking()` → Evicts all booking caches
- `deleteBooking()` → Evicts all booking caches

### Train Service

**Cached Operations:**
- `getAllTrains()` → Cache: `all-trains`, TTL: 10 minutes
- `getTrainById(Long id)` → Cache: `trains`, TTL: 30 minutes
- `getTrainByNumber(String trainNumber)` → Cache: `trains-by-number`, TTL: 1 hour
- `searchTrains(String source, String destination)` → Cache: `train-search`, TTL: 15 minutes

**Cache Eviction:**
- `createTrain()` → Evicts `all-trains`, `train-search`
- `updateTrain()` → Evicts all train caches for the train
- `deleteTrain()` → Evicts all train caches for the train

### User Service

**Cached Operations:**
- `getUserById(Long id)` → Cache: `users`, TTL: 20 minutes
- `getUserByEmail(String email)` → Cache: `users-by-email`, TTL: 15 minutes
- `getUserByUsername(String username)` → Cache: `users`, TTL: 20 minutes
- `getOrCreate(Long userId)` (NotificationPreferences) → Cache: `user-prefs`, TTL: 30 minutes

**Cache Eviction:**
- `createUser()` → Evicts `users`, `users-by-email`
- `updateUser()` → Evicts `users`, `users-by-email` for the user
- `deleteUser()` → Evicts `users`, `users-by-email` for the user
- `update()` (NotificationPreferences) → Updates `user-prefs` cache

### Payment Service

**Cached Operations:**
- `getPaymentById(Long id)` → Cache: `payments`, TTL: 10 minutes
- `getPaymentByTransactionId(String transactionId)` → Cache: `payments-by-transaction`, TTL: 30 minutes
- `getPaymentsByBookingId(Long bookingId)` → Cache: `payments-by-booking`, TTL: 15 minutes

**Cache Eviction:**
- `processPayment()` → Evicts `payments-by-booking` for the booking
- `refundPayment()` → Evicts all payment caches for the payment

## Cache Management API

### Get Cache Statistics
```http
GET /api/cache/stats
```

**Response:**
```json
{
  "enabled": true,
  "caches": {
    "bookings": {"name": "bookings"},
    "bookings-by-pnr": {"name": "bookings-by-pnr"},
    "bookings-by-user": {"name": "bookings-by-user"}
  },
  "totalCaches": 3
}
```

### Clear Specific Cache
```http
DELETE /api/cache/{cacheName}
```

**Example:**
```http
DELETE /api/cache/bookings
```

**Response:**
```json
{
  "status": "success",
  "message": "Cache 'bookings' cleared successfully"
}
```

### Clear All Caches
```http
DELETE /api/cache
```

**Response:**
```json
{
  "status": "success",
  "message": "Cleared 3 cache(s)",
  "clearedCount": 3
}
```

### Evict Specific Key
```http
DELETE /api/cache/{cacheName}/{key}
```

**Example:**
```http
DELETE /api/cache/bookings/123
```

## Configuration

### Application Properties

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  cache:
    type: redis
    redis:
      time-to-live: 900000  # 15 minutes default (service-specific)
```

### Cache TTL Configuration

**Booking Service:**
- Default: 15 minutes
- PNR lookups: 30 minutes
- User bookings: 10 minutes

**Train Service:**
- Default: 30 minutes
- Train by number: 1 hour
- Search results: 15 minutes
- All trains: 10 minutes

**User Service:**
- Default: 20 minutes
- By email: 15 minutes
- Preferences: 30 minutes

**Payment Service:**
- Default: 10 minutes
- By transaction ID: 30 minutes
- By booking ID: 15 minutes

## Cache Key Strategy

### Simple Keys
- `#id` - Direct parameter value
- `#userId` - Method parameter

### Composite Keys
- `#source + ':' + #destination` - String concatenation
- Custom key generator for complex scenarios

### Conditional Caching
- `unless = "#result.isEmpty()"` - Don't cache empty results
- `condition = "#result != null"` - Only cache if result is not null

## Benefits

1. **Performance**
   - Reduced database load (up to 80% reduction)
   - Faster API response times (50-90% improvement)
   - Better scalability

2. **Cost Efficiency**
   - Lower database costs
   - Reduced infrastructure requirements
   - Better resource utilization

3. **User Experience**
   - Faster page loads
   - Better responsiveness
   - Improved system reliability

## Monitoring

### Cache Metrics (Prometheus)

- `cache.gets` - Cache get operations
- `cache.puts` - Cache put operations
- `cache.evictions` - Cache evictions
- `cache.hits` - Cache hits
- `cache.misses` - Cache misses
- `cache.size` - Cache size

### Cache Hit Rate Calculation

```
Cache Hit Rate = (Cache Hits / (Cache Hits + Cache Misses)) * 100
```

Target: > 70% hit rate for read-heavy operations

## Best Practices

1. **TTL Selection**
   - Frequently changing data: 5-10 minutes
   - Moderately changing data: 15-30 minutes
   - Stable data: 1+ hours

2. **Cache Eviction**
   - Always evict on updates
   - Evict related caches (e.g., user bookings when booking created)
   - Use `allEntries = false` for targeted eviction

3. **Cache Keys**
   - Use meaningful key patterns
   - Include all relevant parameters
   - Avoid collisions

4. **Null Handling**
   - Don't cache null values (`disableCachingNullValues`)
   - Use `unless` conditions to skip caching empty results

5. **Cache Warming**
   - Pre-populate frequently accessed data
   - Use scheduled tasks for cache warming
   - Monitor cache hit rates

## Troubleshooting

### Cache Not Working
1. Check Redis connection
2. Verify `@EnableCaching` is present
3. Check cache configuration
4. Review cache annotations

### Stale Data
1. Verify cache eviction on updates
2. Check TTL values
3. Review cache key strategy
4. Monitor cache invalidation

### High Memory Usage
1. Review TTL values
2. Check cache sizes
3. Implement cache size limits
4. Monitor Redis memory

## Testing

### Test Cache Hit
```bash
# First request - cache miss
curl http://localhost:8093/api/bookings/1

# Second request - cache hit (faster)
curl http://localhost:8093/api/bookings/1
```

### Test Cache Eviction
```bash
# Get booking (cached)
curl http://localhost:8093/api/bookings/1

# Update booking (evicts cache)
curl -X PUT http://localhost:8093/api/bookings/1 -d '{"status":"CANCELLED"}'

# Get booking again (cache miss, fresh from DB)
curl http://localhost:8093/api/bookings/1
```

### Test Cache Management
```bash
# Get cache stats
curl http://localhost:8093/api/cache/stats

# Clear specific cache
curl -X DELETE http://localhost:8093/api/cache/bookings

# Clear all caches
curl -X DELETE http://localhost:8093/api/cache
```

## Files Created/Modified

### New Files
- `CacheConfig.java` (Booking, Train, User, Payment services)
- `CacheManagementController.java` (Booking, Train services)
- `SimpleUserService.java` (User service)

### Modified Files
- `pom.xml` - Added `spring-boot-starter-cache` dependency
- `application.yml` - Added cache configuration
- Service classes - Added cache annotations

## Performance Impact

### Expected Improvements
- **Database Load**: 60-80% reduction
- **Response Time**: 50-90% improvement for cached endpoints
- **Throughput**: 2-5x increase for read operations
- **Cache Hit Rate**: 70-90% for frequently accessed data

## Conclusion

API Response Caching provides significant performance improvements with minimal code changes. The Spring Cache abstraction makes it easy to add caching to any service method, and Redis provides a robust, scalable cache backend.

