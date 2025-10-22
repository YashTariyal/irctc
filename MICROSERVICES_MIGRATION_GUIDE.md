# 🏗️ IRCTC Microservices Migration Guide

## 📋 Overview

This guide outlines the step-by-step migration from a monolithic IRCTC application to a microservices architecture.

## 🎯 Migration Strategy: Strangler Fig Pattern

### **Phase 1: Infrastructure Setup** ✅
- [x] Eureka Server (Service Discovery)
- [x] API Gateway (Central Routing)
- [x] User Service (First Microservice)
- [x] Docker Compose (Infrastructure)

### **Phase 2: Service Extraction** (Next Steps)
- [ ] Train Service
- [ ] Booking Service
- [ ] Payment Service
- [ ] Notification Service

## 🚀 Quick Start

### **1. Start Infrastructure Services**
```bash
# Start PostgreSQL and Redis
docker-compose up postgres redis -d

# Start Eureka Server
cd irctc-eureka-server
./mvnw spring-boot:run

# Start API Gateway
cd irctc-api-gateway
./mvnw spring-boot:run

# Start User Service
cd irctc-user-service
./mvnw spring-boot:run
```

### **2. Verify Services**
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081

## 🔧 Service Architecture

### **Current Services:**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Eureka Server │    │   API Gateway   │    │  User Service   │
│   Port: 8761     │    │   Port: 8080    │    │   Port: 8081    │
│                 │    │                 │    │                 │
│ • Service Discovery│  │ • Route Requests│   │ • Authentication│
│ • Health Monitoring│  │ • Load Balancing│   │ • User Management│
│ • Service Registry│  │ • Circuit Breaker│   │ • 2FA & Security│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### **Future Services:**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Train Service  │    │ Booking Service │    │ Payment Service │
│   Port: 8082    │    │   Port: 8083    │    │   Port: 8084    │
│                 │    │                 │    │                 │
│ • Train Schedules│   │ • Reservations  │    │ • Payment Gateway│
│ • Route Management│   │ • Seat Selection│    │ • Transaction Mgmt│
│ • Station Info   │   │ • Booking History│   │ • Refunds       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📊 API Endpoints

### **User Service Endpoints:**
```
POST   /api/users                    # Create user
GET    /api/users/{id}               # Get user by ID
GET    /api/users/username/{username} # Get user by username
PUT    /api/users/{id}               # Update user
POST   /api/users/{id}/change-password # Change password
POST   /api/users/{id}/toggle-status  # Toggle user status
POST   /api/users/{id}/toggle-2fa      # Toggle 2FA
POST   /api/users/{id}/generate-otp    # Generate 2FA OTP
POST   /api/users/{id}/verify-otp     # Verify 2FA OTP
GET    /api/users                     # Get all users (Admin)
GET    /api/users/role/{role}        # Get users by role (Admin)
DELETE /api/users/{id}               # Delete user (Admin)
```

### **API Gateway Routes:**
```
/api/users/**     → User Service (8081)
/api/trains/**    → Train Service (8082) [Future]
/api/bookings/**  → Booking Service (8083) [Future]
/api/payments/**  → Payment Service (8084) [Future]
```

## 🔄 Migration Process

### **Step 1: Extract User Service** ✅
1. **Create User Service**: ✅
   - User entity and repository
   - User service and controller
   - Authentication and 2FA
   - Password policy enforcement

2. **Update API Gateway**: ✅
   - Route `/api/users/**` to User Service
   - Load balancing configuration
   - Circuit breaker setup

3. **Update Monolith**: (Next Step)
   - Remove user management code
   - Update authentication to use User Service
   - Implement service-to-service communication

### **Step 2: Extract Train Service** (Next)
1. **Create Train Service**:
   - Train entity and repository
   - Train service and controller
   - Route management
   - Schedule management

2. **Update API Gateway**:
   - Route `/api/trains/**` to Train Service
   - Add circuit breaker for Train Service

3. **Update Monolith**:
   - Remove train management code
   - Update train-related APIs to use Train Service

### **Step 3: Extract Booking Service** (Future)
1. **Create Booking Service**:
   - Booking entity and repository
   - Booking service and controller
   - Seat selection logic
   - Booking history

2. **Service Communication**:
   - User Service → Booking Service
   - Train Service → Booking Service
   - Event-driven communication

## 🛠️ Technical Implementation

### **Service Communication:**
```java
// Synchronous: REST APIs
@FeignClient(name = "irctc-user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    User getUserById(@PathVariable Long id);
}

// Asynchronous: Event-driven
@EventListener
public void handleUserRegistered(UserRegisteredEvent event) {
    // Send welcome email
    notificationService.sendWelcomeEmail(event.getUser());
}
```

### **Database per Service:**
```yaml
User Service:     irctc_user_db
Train Service:    irctc_train_db
Booking Service:  irctc_booking_db
Payment Service: irctc_payment_db
```

### **Configuration Management:**
```yaml
# Centralized configuration
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/irctc/config-repo
```

## 📈 Benefits of Migration

### **🚀 Scalability:**
- Scale individual services based on demand
- Train service can handle peak booking times
- User service can scale during registration spikes

### **🔧 Technology Flexibility:**
- Use Python for Analytics Service
- Use Node.js for Notification Service
- Use Go for high-performance Train Service

### **👥 Team Autonomy:**
- Independent development teams
- Separate deployment cycles
- Technology stack freedom

## ⚠️ Challenges and Solutions

### **🔀 Complexity:**
- **Challenge**: Network latency between services
- **Solution**: Implement caching and connection pooling

### **🛠️ Operational Overhead:**
- **Challenge**: Multiple deployments
- **Solution**: CI/CD pipelines and containerization

### **🔍 Monitoring:**
- **Challenge**: Debugging distributed systems
- **Solution**: Centralized logging and distributed tracing

## 🎯 Next Steps

1. **Test Current Setup**: Verify all services are running
2. **Extract Train Service**: Move train management logic
3. **Implement Service Communication**: Add Feign clients
4. **Add Monitoring**: Implement distributed tracing
5. **Performance Testing**: Load test the microservices

## 📚 Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Microservices Patterns](https://microservices.io/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Eureka Server Guide](https://spring.io/guides/gs/service-registration-and-discovery/)

---

**🚀 Ready to start the migration? Let's begin with testing the current setup!**
