# 📊 Analytics Service Testing Guide

## Quick Start

### 1. Start Required Services

The Analytics Service depends on these services:
- Eureka Server (Port 8761)
- Booking Service (Port 8093)
- Payment Service (Port 8094)
- Train Service (Port 8092)
- User Service (Port 8091)
- Redis (Port 6379) - Optional but recommended for caching

```bash
# Option 1: Start all services using the startup script
./start-microservices.sh

# Option 2: Start services individually
# Terminal 1 - Eureka
cd irctc-eureka-server && ./mvnw spring-boot:run

# Terminal 2 - Booking Service
cd irctc-booking-service && ./mvnw spring-boot:run

# Terminal 3 - Payment Service
cd irctc-payment-service && ./mvnw spring-boot:run

# Terminal 4 - Train Service
cd irctc-train-service && ./mvnw spring-boot:run

# Terminal 5 - User Service
cd irctc-user-service && ./mvnw spring-boot:run

# Terminal 6 - API Gateway (Optional)
cd irctc-api-gateway && ./mvnw spring-boot:run
```

### 2. Start Analytics Service

```bash
cd irctc-analytics-service
./mvnw spring-boot:run
```

Wait for the service to start (30-60 seconds). You should see:
```
✅ IRCTC Analytics Service started successfully!
📊 Port: 8096
📱 Service: Revenue Analytics Dashboard
```

### 3. Run Tests

```bash
# Make test script executable (if not already)
chmod +x test-analytics-service.sh

# Run comprehensive tests
./test-analytics-service.sh
```

## Manual Testing

### Test Service Health

```bash
curl http://localhost:8096/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Test Revenue Trends

```bash
# Daily revenue trends
curl http://localhost:8096/api/analytics/revenue?period=daily

# Weekly revenue trends
curl http://localhost:8096/api/analytics/revenue?period=weekly

# Monthly revenue trends with date range
curl "http://localhost:8096/api/analytics/revenue?period=monthly&startDate=2025-01-01&endDate=2025-01-31"
```

### Test Booking Analytics

```bash
# Booking trends
curl http://localhost:8096/api/analytics/bookings/trends

# Booking trends with date range
curl "http://localhost:8096/api/analytics/bookings/trends?startDate=2025-01-01&endDate=2025-01-31"
```

### Test Route Performance

```bash
# Route performance
curl http://localhost:8096/api/analytics/routes/performance

# Route performance with date range
curl "http://localhost:8096/api/analytics/routes/performance?startDate=2025-01-01&endDate=2025-01-31"
```

### Test User Segmentation

```bash
curl http://localhost:8096/api/analytics/users/segmentation
```

### Test Forecasting

```bash
# Revenue forecast
curl "http://localhost:8096/api/analytics/forecast?forecastType=revenue&days=30"

# Booking forecast
curl "http://localhost:8096/api/analytics/forecast?forecastType=bookings&days=30"
```

## Testing via API Gateway

If API Gateway is running, test through it:

```bash
# Revenue trends via gateway
curl http://localhost:8090/api/analytics/revenue?period=daily

# Booking trends via gateway
curl http://localhost:8090/api/analytics/bookings/trends

# Route performance via gateway
curl http://localhost:8090/api/analytics/routes/performance

# User segmentation via gateway
curl http://localhost:8090/api/analytics/users/segmentation

# Forecast via gateway
curl "http://localhost:8090/api/analytics/forecast?forecastType=revenue&days=30"
```

## Swagger UI

Access the interactive API documentation:

```
http://localhost:8096/swagger-ui/index.html
```

## Eureka Dashboard

Check service registration:

```
http://localhost:8761
```

Look for `IRCTC-ANALYTICS-SERVICE` in the service list.

## Troubleshooting

### Service Not Starting

1. Check if port 8096 is available:
   ```bash
   lsof -i :8096
   ```

2. Check for compilation errors:
   ```bash
   cd irctc-analytics-service
   mvn clean compile
   ```

3. Check service logs for errors

### Service Not Registering with Eureka

1. Verify Eureka Server is running:
   ```bash
   curl http://localhost:8761/eureka/apps
   ```

2. Check Eureka configuration in `application.yml`

3. Verify service name matches: `irctc-analytics-service`

### Endpoints Returning Empty Data

1. Ensure dependent services are running (Booking, Payment, Train, User)
2. Check if services have data (bookings, payments, etc.)
3. Verify Feign client configurations
4. Check service logs for circuit breaker activations

### Circuit Breaker Issues

If services are down, the analytics service will use fallback methods and return empty data. This is expected behavior for resilience.

## Expected Test Results

When all services are running with data:

- ✅ All health checks pass
- ✅ All analytics endpoints return 200 status
- ✅ Responses contain valid JSON
- ✅ Data points are populated (if source services have data)
- ✅ API Gateway routes work correctly
- ✅ Swagger UI is accessible
- ✅ Service is registered with Eureka

## Next Steps

1. ✅ Run comprehensive test suite
2. ✅ Verify all endpoints work
3. ✅ Test with real data from other services
4. ✅ Integrate with frontend dashboard
5. ✅ Monitor performance and caching

