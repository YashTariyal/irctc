# 📊 IRCTC Analytics Service - Revenue Analytics Dashboard

## Overview

The Analytics Service provides comprehensive revenue analytics and business intelligence for the IRCTC microservices system. It aggregates data from multiple services (Booking, Payment, Train, User) to provide insights into revenue trends, booking patterns, route performance, user segmentation, and forecasting.

## Features

### 1. Revenue Trends
- Daily, weekly, and monthly revenue analysis
- Revenue growth rate calculation
- Average revenue per period
- Historical comparison with previous periods

### 2. Booking Analytics
- Booking trends over time
- Cancellation rates and refund analytics
- Confirmation rates
- Waitlist analysis
- Average booking value

### 3. Route Performance
- Most profitable routes
- Popular routes analysis
- Route occupancy rates
- Revenue per route

### 4. User Segmentation
- User behavior analysis by segments (VIP, Regular, New, Inactive)
- Revenue contribution by segment
- Average bookings per segment
- Segment distribution

### 5. Forecasting
- Revenue forecasting (linear regression)
- Booking count forecasting
- Confidence intervals
- Multiple forecast methods support

## API Endpoints

### Revenue Trends
```
GET /api/analytics/revenue?period={daily|weekly|monthly}&startDate={date}&endDate={date}
```

### Booking Trends
```
GET /api/analytics/bookings/trends?startDate={date}&endDate={date}
```

### Route Performance
```
GET /api/analytics/routes/performance?startDate={date}&endDate={date}
```

### User Segmentation
```
GET /api/analytics/users/segmentation
```

### Forecast
```
GET /api/analytics/forecast?forecastType={revenue|bookings}&days={number}
```

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Cloud** (Eureka, OpenFeign)
- **Redis** (Caching)
- **Resilience4j** (Circuit Breaker)
- **Swagger/OpenAPI 3** (API Documentation)

## Service Dependencies

The Analytics Service communicates with:
- **Booking Service** - For booking data
- **Payment Service** - For payment and refund data
- **Train Service** - For route information
- **User Service** - For user data

## Configuration

### Port
- Default: **8096**

### Eureka Service Name
- **IRCTC-ANALYTICS-SERVICE**

### Redis Cache
- Default TTL: 5 minutes
- Cache keys: revenue-trends, booking-trends, route-performance, user-segmentation, forecast

## Running the Service

```bash
cd irctc-analytics-service
./mvnw spring-boot:run
```

Or via Docker:
```bash
docker-compose up irctc-analytics-service
```

## Swagger Documentation

Once the service is running, access Swagger UI at:
```
http://localhost:8096/swagger-ui/index.html
```

## API Gateway Integration

The service is registered with the API Gateway and accessible via:
```
http://localhost:8090/api/analytics/*
```

## Circuit Breaker

The service uses Resilience4j Circuit Breaker for fault tolerance:
- **Booking Service**: 50% failure threshold, 10s wait duration
- **Payment Service**: 50% failure threshold, 10s wait duration
- **Train Service**: 50% failure threshold, 10s wait duration
- **User Service**: 50% failure threshold, 10s wait duration

## Caching Strategy

- Revenue trends: Cached for 5 minutes
- Booking trends: Cached for 5 minutes
- Route performance: Cached for 5 minutes
- User segmentation: Cached for 5 minutes
- Forecast: Cached for 5 minutes

Cache keys include date ranges and parameters to ensure accurate data.

## Future Enhancements

1. **Advanced Forecasting**: ML-based forecasting with seasonal patterns
2. **Real-time Analytics**: WebSocket-based real-time dashboard updates
3. **Custom Reports**: User-defined report generation
4. **Export Functionality**: CSV/PDF export of analytics
5. **Dashboard UI**: React-based analytics dashboard
6. **Alerting**: Automated alerts for revenue anomalies

## Notes

- The service uses Feign clients with fallback mechanisms for resilience
- All analytics calculations are performed server-side for performance
- Date filtering is currently done client-side; production should have dedicated endpoints in source services
- Forecasting uses simple linear regression; can be enhanced with ML models

