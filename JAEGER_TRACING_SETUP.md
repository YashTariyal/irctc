# 🔍 Jaeger Distributed Tracing Setup Guide

## 🎯 Overview

This guide explains how to use Jaeger for distributed tracing in IRCTC microservices. Jaeger provides end-to-end visibility into request flows across all services.

---

## 📋 What is Distributed Tracing?

Distributed tracing tracks requests as they flow through multiple microservices, providing:
- **Request Flow Visualization**: See how requests traverse services
- **Performance Analysis**: Identify bottlenecks and slow operations
- **Error Tracking**: Trace errors across service boundaries
- **Dependency Mapping**: Understand service dependencies

---

## 🏗️ Architecture

### Tracing Flow

```
Client Request
    ↓
API Gateway (creates trace)
    ↓
Service A (adds span)
    ↓
Service B (adds span)
    ↓
Service C (adds span)
    ↓
Jaeger (collects and visualizes)
```

### Components

1. **Jaeger All-in-One**: Complete Jaeger stack (collector, query, UI)
2. **OTLP Exporter**: OpenTelemetry Protocol exporter in each service
3. **Micrometer Tracing**: Spring Boot integration for tracing

---

## 🚀 Quick Start

### 1. Start Jaeger

```bash
# Using Docker Compose
docker-compose up -d jaeger

# Or using Docker directly
docker run -d \
  --name irctc-jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest
```

### 2. Access Jaeger UI

Open your browser and navigate to:
```
http://localhost:16686
```

### 3. Generate Traces

Make requests to your services:
```bash
# Example: Create a booking
curl -X POST http://localhost:8090/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "seatCount": 2
  }'
```

### 4. View Traces

1. Go to Jaeger UI: http://localhost:16686
2. Select service: `irctc-api-gateway` or any service
3. Click "Find Traces"
4. View the trace timeline

---

## 📁 Configuration

### Docker Compose

Jaeger is configured in `docker-compose.yml`:

```yaml
jaeger:
  image: jaegertracing/all-in-one:latest
  container_name: irctc-jaeger
  ports:
    - "16686:16686"  # Jaeger UI
    - "4317:4317"    # OTLP gRPC receiver
    - "4318:4318"    # OTLP HTTP receiver
  environment:
    - COLLECTOR_OTLP_ENABLED=true
    - SPAN_STORAGE_TYPE=badger
```

### Service Configuration

Each service is configured in `application.yml`:

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0  # 100% sampling for development
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
      export:
        enabled: true
```

### Maven Dependencies

All services include tracing dependencies:

```xml
<!-- Tracing: Micrometer + OpenTelemetry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

---

## 🔧 Services Configured

All microservices are configured for tracing:

- ✅ **API Gateway** (`irctc-api-gateway`)
- ✅ **User Service** (`irctc-user-service`)
- ✅ **Train Service** (`irctc-train-service`)
- ✅ **Booking Service** (`irctc-booking-service`)
- ✅ **Payment Service** (`irctc-payment-service`)
- ✅ **Notification Service** (`irctc-notification-service`)

---

## 📊 Using Jaeger UI

### Finding Traces

1. **Service Selection**: Choose a service from the dropdown
2. **Time Range**: Select time range (Last 15 minutes, 1 hour, etc.)
3. **Operation**: Filter by operation name (optional)
4. **Tags**: Add tags for filtering (e.g., `http.status_code=200`)
5. **Click "Find Traces"**

### Viewing Trace Details

1. **Timeline View**: See spans arranged by time
2. **Service Map**: Visualize service dependencies
3. **Span Details**: Click on spans to see:
   - Duration
   - Tags (HTTP method, status code, etc.)
   - Logs
   - Service name

### Trace Example

```
Trace: POST /api/bookings
├── API Gateway (100ms)
│   ├── Authentication (10ms)
│   └── Routing (5ms)
├── Booking Service (500ms)
│   ├── Validate Request (20ms)
│   ├── Check Availability (200ms)
│   │   └── Train Service Call (180ms)
│   ├── Create Booking (150ms)
│   └── Process Payment (100ms)
│       └── Payment Service Call (90ms)
└── Notification Service (50ms)
    └── Send Confirmation (45ms)
```

---

## 🎯 Best Practices

### 1. **Sampling Configuration**

**Development**:
```yaml
sampling:
  probability: 1.0  # 100% - capture all traces
```

**Production**:
```yaml
sampling:
  probability: 0.1  # 10% - reduce overhead
```

### 2. **Custom Spans**

Add custom spans for important operations:

```java
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@Service
public class BookingService {
    
    private final Tracer tracer;
    
    public Booking createBooking(BookingRequest request) {
        Span span = tracer.nextSpan()
            .name("create-booking")
            .tag("user.id", request.getUserId())
            .tag("train.id", request.getTrainId())
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Business logic
            return bookingRepository.save(booking);
        } finally {
            span.end();
        }
    }
}
```

### 3. **Tag Important Information**

Add tags to spans for better filtering:

```java
span.tag("booking.id", booking.getId());
span.tag("payment.status", payment.getStatus());
span.tag("user.role", user.getRole());
```

### 4. **Error Tracking**

Errors are automatically captured in traces:

```java
try {
    // Operation
} catch (Exception e) {
    span.tag("error", true);
    span.tag("error.message", e.getMessage());
    throw e;
}
```

---

## 🔍 Common Use Cases

### 1. **Performance Analysis**

**Problem**: Slow booking creation

**Solution**:
1. Search for traces with operation `POST /api/bookings`
2. Sort by duration (longest first)
3. Identify slow spans
4. Analyze service dependencies

### 2. **Error Investigation**

**Problem**: Payment failures

**Solution**:
1. Filter by tag: `error=true`
2. Filter by service: `payment-service`
3. View error messages in span details
4. Trace back to root cause

### 3. **Service Dependency Mapping**

**Problem**: Understand service interactions

**Solution**:
1. Go to "Dependencies" tab in Jaeger UI
2. View service dependency graph
3. Identify critical paths
4. Plan for service isolation

---

## 🐛 Troubleshooting

### No Traces Appearing

**Check**:
1. Jaeger is running: `docker ps | grep jaeger`
2. Services are configured: Check `application.yml`
3. Tracing is enabled: `management.tracing.enabled=true`
4. OTLP endpoint is correct: `http://localhost:4318/v1/traces`

### Traces Missing Spans

**Check**:
1. Service dependencies are correct
2. Feign clients have tracing enabled
3. Custom spans are properly closed

### High Memory Usage

**Solution**:
1. Reduce sampling rate
2. Use span storage limits
3. Configure retention policies

---

## 📊 Integration with Other Tools

### Prometheus Integration

Jaeger metrics can be exported to Prometheus:

```yaml
# In prometheus.yml
scrape_configs:
  - job_name: 'jaeger'
    static_configs:
      - targets: ['jaeger:14269']
```

### Grafana Integration

View Jaeger traces in Grafana:

1. Install Jaeger data source plugin
2. Configure Jaeger URL: `http://jaeger:16686`
3. Create dashboard with trace panels

---

## 🔄 Production Considerations

### 1. **Storage Backend**

For production, use persistent storage:

```yaml
jaeger:
  environment:
    - SPAN_STORAGE_TYPE=elasticsearch
    - ES_SERVER_URLS=http://elasticsearch:9200
```

### 2. **Sampling Strategy**

Use adaptive sampling:

```yaml
sampling:
  probability: 0.1  # 10% base rate
  # Or use head-based sampling in Jaeger
```

### 3. **High Availability**

Deploy Jaeger in HA mode:
- Separate collector, query, and storage
- Use load balancers
- Configure replication

---

## 📚 Additional Resources

- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [OpenTelemetry](https://opentelemetry.io/)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)
- [Spring Boot Observability](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.tracing)

---

## 🎯 Next Steps

1. **Custom Instrumentation**: Add custom spans for business operations
2. **Alerting**: Set up alerts for slow traces
3. **Service Map**: Use dependency graph for architecture decisions
4. **Performance Baselines**: Establish performance SLAs

---

*Last Updated: November 2025*

