# 🚀 IRCTC Microservices Architecture

## 📋 Overview

This project implements a complete microservices architecture for the IRCTC (Indian Railway Catering and Tourism Corporation) system, migrating from a monolithic application to a distributed microservices ecosystem.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Eureka Server │    │   API Gateway   │    │   User Service  │
│   Port: 8761    │    │   Port: 8090    │    │   Port: 8091    │
│   ✅ UP         │    │   ✅ UP         │    │   ✅ UP         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────┼─────────────┐
                    │             │             │
         ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
         │  Train Service  │ │ Booking Service │ │ Payment Service │
         │   Port: 8092    │ │   Port: 8093    │ │   Port: 8094    │
         │   ✅ UP         │ │   ✅ UP         │ │   ✅ UP         │
         └─────────────────┘ └─────────────────┘ └─────────────────┘
                    │             │             │
                    └─────────────┼─────────────┘
                                 │
                    ┌─────────────────┐
                    │Notification Svc │
                    │   Port: 8095    │
                    │   ✅ UP         │
                    └─────────────────┘
```

## 🛠️ Services

### 1. **Eureka Server** (Port 8761)
- **Purpose**: Service Discovery and Registration
- **Technology**: Spring Cloud Netflix Eureka
- **Features**: Service registry, health monitoring, load balancing

### 2. **API Gateway** (Port 8090)
- **Purpose**: Central routing and load balancing
- **Technology**: Spring Cloud Gateway
- **Features**: Route management, CORS handling, circuit breaker

### 3. **User Service** (Port 8091)
- **Purpose**: User management and authentication
- **Technology**: Spring Boot + JWT
- **Features**: User CRUD, authentication, 2FA, password policies

### 4. **Train Service** (Port 8092)
- **Purpose**: Train information and search
- **Technology**: Spring Boot + JPA
- **Features**: Train search, schedules, availability, routes

### 5. **Booking Service** (Port 8093)
- **Purpose**: Ticket booking and management
- **Technology**: Spring Boot + JPA
- **Features**: Booking CRUD, seat selection, cancellation

### 6. **Payment Service** (Port 8094)
- **Purpose**: Payment processing
- **Technology**: Spring Boot + JPA
- **Features**: Payment processing, refunds, transaction history

### 7. **Notification Service** (Port 8095)
- **Purpose**: Notifications and alerts
- **Technology**: Spring Boot + JPA
- **Features**: Email, SMS, push notifications, templates

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Git

### 1. Start All Services
```bash
# Make script executable
chmod +x start-microservices.sh

# Start all services
./start-microservices.sh
```

### 2. Manual Start (Individual Services)
```bash
# Eureka Server
cd irctc-eureka-server && ./mvnw spring-boot:run

# API Gateway
cd irctc-api-gateway && ./mvnw spring-boot:run

# User Service
cd irctc-user-service && ./mvnw spring-boot:run

# Train Service
cd irctc-train-service && ./mvnw spring-boot:run

# Booking Service
cd irctc-booking-service && ./mvnw spring-boot:run

# Payment Service
cd irctc-payment-service && ./mvnw spring-boot:run

# Notification Service
cd irctc-notification-service && ./mvnw spring-boot:run
```

## 🔍 Service Endpoints

### Eureka Server
- **Dashboard**: http://localhost:8761
- **Health**: http://localhost:8761/actuator/health

### API Gateway
- **Health**: http://localhost:8090/actuator/health
- **Routes**: All services accessible through gateway

### User Service
- **Base URL**: http://localhost:8091/api/users
- **Health**: http://localhost:8091/actuator/health
- **Endpoints**:
  - `GET /api/users` - Get all users
  - `POST /api/users` - Create user
  - `GET /api/users/{id}` - Get user by ID
  - `GET /api/users/username/{username}` - Get user by username

### Train Service
- **Base URL**: http://localhost:8092/api/trains
- **Health**: http://localhost:8092/actuator/health
- **Endpoints**:
  - `GET /api/trains` - Get all trains
  - `GET /api/trains/search?source={source}&destination={destination}` - Search trains
  - `POST /api/trains` - Create train
  - `GET /api/trains/{id}` - Get train by ID

### Booking Service
- **Base URL**: http://localhost:8093/api/bookings
- **Health**: http://localhost:8093/actuator/health
- **Endpoints**:
  - `GET /api/bookings` - Get all bookings
  - `POST /api/bookings` - Create booking
  - `GET /api/bookings/user/{userId}` - Get user bookings
  - `POST /api/bookings/{bookingId}/cancel` - Cancel booking

### Payment Service
- **Base URL**: http://localhost:8094/api/payments
- **Health**: http://localhost:8094/actuator/health
- **Endpoints**:
  - `GET /api/payments` - Get all payments
  - `POST /api/payments` - Create payment
  - `POST /api/payments/{paymentId}/process` - Process payment
  - `POST /api/payments/{paymentId}/refund` - Process refund

### Notification Service
- **Base URL**: http://localhost:8095/api/notifications
- **Health**: http://localhost:8095/actuator/health
- **Endpoints**:
  - `GET /api/notifications` - Get all notifications
  - `POST /api/notifications` - Create notification
  - `GET /api/notifications/user/{userId}` - Get user notifications
  - `POST /api/notifications/{notificationId}/send` - Send notification

## 🧪 Testing

### 1. Health Checks
```bash
# Check all services
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8090/actuator/health # API Gateway
curl http://localhost:8091/actuator/health # User Service
curl http://localhost:8092/actuator/health # Train Service
curl http://localhost:8093/actuator/health # Booking Service
curl http://localhost:8094/actuator/health # Payment Service
curl http://localhost:8095/actuator/health # Notification Service
```

### 2. Service Discovery
```bash
# Check registered services
curl http://localhost:8761/eureka/apps
```

### 3. API Gateway Routes
```bash
# Test routing through API Gateway
curl http://localhost:8090/api/users
curl http://localhost:8090/api/trains
curl http://localhost:8090/api/bookings
curl http://localhost:8090/api/payments
curl http://localhost:8090/api/notifications
```

## 📊 Monitoring

### Service Logs
```bash
# View service logs
tail -f /tmp/eureka-server.log
tail -f /tmp/api-gateway.log
tail -f /tmp/user-service.log
tail -f /tmp/train-service.log
tail -f /tmp/booking-service.log
tail -f /tmp/payment-service.log
tail -f /tmp/notification-service.log
```

### Eureka Dashboard
- **URL**: http://localhost:8761
- **Features**: Service registry, health status, instance details

## 🛑 Stopping Services

### Stop All Services
```bash
pkill -f spring-boot:run
```

### Stop Individual Service
```bash
# Find process ID
ps aux | grep spring-boot:run

# Kill specific process
kill -9 <PID>
```

## 🔧 Configuration

### Database
- **Type**: H2 In-Memory Database
- **Purpose**: Development and testing
- **Production**: Replace with PostgreSQL/MySQL

### Service Discovery
- **Type**: Netflix Eureka
- **Port**: 8761
- **Features**: Auto-registration, health monitoring

### API Gateway
- **Type**: Spring Cloud Gateway
- **Port**: 8090
- **Features**: Load balancing, CORS, routing

## 📈 Benefits

### 1. **Scalability**
- Independent scaling of services
- Load balancing across instances
- Resource optimization

### 2. **Maintainability**
- Service isolation
- Independent deployment
- Technology diversity

### 3. **Reliability**
- Fault isolation
- Circuit breaker patterns
- Health monitoring

### 4. **Development**
- Team independence
- Faster development cycles
- Technology flexibility

## 🚧 Future Enhancements

### 1. **Infrastructure**
- Docker containerization
- Kubernetes orchestration
- CI/CD pipelines

### 2. **Monitoring**
- Prometheus metrics
- Grafana dashboards
- Distributed tracing

### 3. **Security**
- OAuth 2.0 / OpenID Connect
- API rate limiting
- Security headers

### 4. **Data Management**
- Database per service
- Event sourcing
- CQRS patterns

## 📝 API Documentation

### User Service APIs
- **Authentication**: JWT-based
- **Endpoints**: CRUD operations for users
- **Features**: 2FA, password policies

### Train Service APIs
- **Search**: Route-based train search
- **Filtering**: By type, class, amenities
- **Availability**: Real-time seat availability

### Booking Service APIs
- **Booking**: Complete booking workflow
- **Management**: Cancellation, modification
- **History**: User booking history

### Payment Service APIs
- **Processing**: Multiple payment methods
- **Refunds**: Automated refund processing
- **History**: Transaction tracking

### Notification Service APIs
- **Channels**: Email, SMS, Push
- **Templates**: Dynamic content generation
- **Delivery**: Reliable message delivery

## 🎯 Success Metrics

- ✅ **Service Discovery**: All services registered
- ✅ **API Gateway**: Central routing working
- ✅ **Health Checks**: All services healthy
- ✅ **Database**: H2 in-memory databases
- ✅ **Logging**: Centralized logging
- ✅ **Monitoring**: Service health monitoring

## 📞 Support

For issues and questions:
1. Check service logs
2. Verify service registration in Eureka
3. Test individual service endpoints
4. Check API Gateway routing

---

**🎉 IRCTC Microservices Architecture - Complete Implementation!**
