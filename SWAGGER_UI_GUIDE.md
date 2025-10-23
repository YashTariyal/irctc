# 📚 IRCTC Microservices - Swagger UI Guide

## 🎯 **Overview**

This guide provides comprehensive information about accessing and using Swagger UI for all IRCTC microservices. Each service now has its own Swagger documentation, and there's a central hub for easy access.

## 🚀 **Quick Access Links**

### **Central Swagger Hub**
- **URL**: http://localhost:8096/swagger-ui.html
- **Description**: Central hub for all microservices API documentation

### **Individual Service Swagger UIs**

| Service | Direct Access | Via API Gateway | Description |
|---------|---------------|-----------------|-------------|
| **User Service** | http://localhost:8091/swagger-ui.html | http://localhost:8090/api/users/swagger-ui.html | User Management & Authentication |
| **Train Service** | http://localhost:8092/swagger-ui.html | http://localhost:8090/api/trains/swagger-ui.html | Train Information & Search |
| **Booking Service** | http://localhost:8093/swagger-ui.html | http://localhost:8090/api/bookings/swagger-ui.html | Ticket Booking & Management |
| **Payment Service** | http://localhost:8094/swagger-ui.html | http://localhost:8090/api/payments/swagger-ui.html | Payment Processing |
| **Notification Service** | http://localhost:8095/swagger-ui.html | http://localhost:8090/api/notifications/swagger-ui.html | Notifications & Alerts |

### **Eureka Dashboard**
- **URL**: http://localhost:8761
- **Description**: Service discovery and registration

## 🔧 **Service Details**

### **1. User Service (Port 8091)**
- **API Documentation**: http://localhost:8091/swagger-ui.html
- **API Docs JSON**: http://localhost:8091/api-docs
- **Endpoints**:
  - `POST /api/users` - Create user
  - `GET /api/users/{username}` - Get user by username
  - `GET /api/users/id/{id}` - Get user by ID
  - `POST /api/users/2fa/generate` - Generate 2FA
  - `POST /api/users/2fa/verify` - Verify 2FA
  - `POST /api/users/password/validate` - Validate password policy

### **2. Train Service (Port 8092)**
- **API Documentation**: http://localhost:8092/swagger-ui.html
- **API Docs JSON**: http://localhost:8092/api-docs
- **Endpoints**:
  - `GET /api/trains` - Get all trains
  - `GET /api/trains/{id}` - Get train by ID
  - `GET /api/trains/number/{trainNumber}` - Get train by number
  - `GET /api/trains/search` - Search trains
  - `POST /api/trains` - Create train
  - `PUT /api/trains/{id}` - Update train
  - `DELETE /api/trains/{id}` - Delete train
  - `PUT /api/trains/availability/{trainNumber}` - Update seat availability

### **3. Booking Service (Port 8093)**
- **API Documentation**: http://localhost:8093/swagger-ui.html
- **API Docs JSON**: http://localhost:8093/api-docs
- **Endpoints**:
  - `GET /api/bookings` - Get all bookings
  - `GET /api/bookings/{id}` - Get booking by ID
  - `GET /api/bookings/pnr/{pnrNumber}` - Get booking by PNR
  - `GET /api/bookings/user/{userId}` - Get bookings by user
  - `POST /api/bookings` - Create booking
  - `PUT /api/bookings/{id}` - Update booking
  - `DELETE /api/bookings/{id}` - Cancel booking

### **4. Payment Service (Port 8094)**
- **API Documentation**: http://localhost:8094/swagger-ui.html
- **API Docs JSON**: http://localhost:8094/api-docs
- **Endpoints**:
  - `GET /api/payments` - Get all payments
  - `GET /api/payments/{id}` - Get payment by ID
  - `GET /api/payments/transaction/{transactionId}` - Get payment by transaction ID
  - `GET /api/payments/booking/{bookingId}` - Get payments by booking
  - `POST /api/payments` - Process payment
  - `PUT /api/payments/refund/{id}` - Refund payment

### **5. Notification Service (Port 8095)**
- **API Documentation**: http://localhost:8095/swagger-ui.html
- **API Docs JSON**: http://localhost:8095/api-docs
- **Endpoints**:
  - `GET /api/notifications` - Get all notifications
  - `GET /api/notifications/{id}` - Get notification by ID
  - `GET /api/notifications/user/{userId}` - Get notifications by user
  - `GET /api/notifications/type/{type}` - Get notifications by type
  - `POST /api/notifications` - Create notification
  - `PUT /api/notifications/{id}` - Update notification
  - `DELETE /api/notifications/{id}` - Delete notification

## 🌐 **Accessing Through Eureka**

### **Service Discovery**
1. **Eureka Dashboard**: http://localhost:8761
2. **Registered Services**:
   - `IRCTC-USER-SERVICE`
   - `IRCTC-TRAIN-SERVICE`
   - `IRCTC-BOOKING-SERVICE`
   - `IRCTC-PAYMENT-SERVICE`
   - `IRCTC-NOTIFICATION-SERVICE`
   - `IRCTC-SWAGGER-HUB`

### **API Gateway Integration**
- **Gateway URL**: http://localhost:8090
- **Routes**:
  - `/api/users/**` → User Service
  - `/api/trains/**` → Train Service
  - `/api/bookings/**` → Booking Service
  - `/api/payments/**` → Payment Service
  - `/api/notifications/**` → Notification Service
  - `/swagger-hub/**` → Swagger Hub

## 🛠️ **Testing APIs**

### **1. Using Swagger UI**
1. Navigate to any service's Swagger UI
2. Click "Try it out" on any endpoint
3. Fill in required parameters
4. Click "Execute" to test the API

### **2. Using cURL Examples**

#### **User Service**
```bash
# Create a user
curl -X POST http://localhost:8091/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com","roles":["USER"]}'

# Get user by username
curl http://localhost:8091/api/users/testuser
```

#### **Train Service**
```bash
# Get all trains
curl http://localhost:8092/api/trains

# Search trains
curl "http://localhost:8092/api/trains/search?source=Delhi&destination=Mumbai"
```

#### **Booking Service**
```bash
# Get all bookings
curl http://localhost:8093/api/bookings

# Create a booking
curl -X POST http://localhost:8093/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"trainId":1,"totalFare":500.0,"passengers":[]}'
```

## 🔍 **Swagger UI Features**

### **Interactive Documentation**
- **Try it out**: Test APIs directly from the browser
- **Request/Response Examples**: See sample data
- **Schema Information**: Understand data structures
- **Authentication**: Configure API keys/tokens

### **API Documentation**
- **Endpoint Descriptions**: Detailed information about each endpoint
- **Parameter Details**: Required/optional parameters
- **Response Codes**: HTTP status codes and meanings
- **Data Models**: Entity structures and relationships

### **Testing Capabilities**
- **Live API Testing**: Execute requests against running services
- **Response Validation**: Verify API responses
- **Error Handling**: Test error scenarios
- **Performance Testing**: Measure response times

## 🚀 **Starting Services with Swagger**

### **Start All Services**
```bash
# Start Eureka Server
cd irctc-eureka-server && ./mvnw spring-boot:run &

# Start API Gateway
cd irctc-api-gateway && ./mvnw spring-boot:run &

# Start User Service
cd irctc-user-service && ./mvnw spring-boot:run &

# Start Train Service
cd irctc-train-service && ./mvnw spring-boot:run &

# Start Booking Service
cd irctc-booking-service && ./mvnw spring-boot:run &

# Start Payment Service
cd irctc-payment-service && ./mvnw spring-boot:run &

# Start Notification Service
cd irctc-notification-service && ./mvnw spring-boot:run &

# Start Swagger Hub
cd irctc-swagger-hub && ./mvnw spring-boot:run &
```

### **Verify Services**
```bash
# Check Eureka Dashboard
curl http://localhost:8761

# Check API Gateway
curl http://localhost:8090/actuator/health

# Check Swagger Hub
curl http://localhost:8096/actuator/health
```

## 📊 **Monitoring and Health Checks**

### **Service Health**
- **Eureka**: http://localhost:8761/actuator/health
- **API Gateway**: http://localhost:8090/actuator/health
- **User Service**: http://localhost:8091/actuator/health
- **Train Service**: http://localhost:8092/actuator/health
- **Booking Service**: http://localhost:8093/actuator/health
- **Payment Service**: http://localhost:8094/actuator/health
- **Notification Service**: http://localhost:8095/actuator/health
- **Swagger Hub**: http://localhost:8096/actuator/health

### **Metrics and Monitoring**
- **Prometheus Metrics**: Available on all services
- **Health Indicators**: Database, disk space, memory
- **Service Discovery**: Automatic registration/deregistration

## 🎯 **Best Practices**

### **API Testing**
1. **Start with Health Checks**: Verify services are running
2. **Test Individual Services**: Use direct URLs first
3. **Test Through Gateway**: Verify routing works
4. **Test Error Scenarios**: Invalid data, missing parameters
5. **Test Authentication**: If implemented

### **Documentation**
1. **Use Descriptive Names**: Clear endpoint names
2. **Add Examples**: Request/response examples
3. **Document Errors**: Common error scenarios
4. **Version APIs**: Use versioning for changes
5. **Keep Updated**: Regular documentation updates

## 🔧 **Troubleshooting**

### **Common Issues**
1. **Service Not Found**: Check Eureka registration
2. **Gateway Timeout**: Check service health
3. **CORS Issues**: Check gateway CORS configuration
4. **Authentication Errors**: Verify security configuration

### **Debug Steps**
1. **Check Service Logs**: Look for error messages
2. **Verify Ports**: Ensure no port conflicts
3. **Check Dependencies**: Verify all services are running
4. **Test Direct Access**: Bypass gateway if needed

## 📈 **Next Steps**

### **Enhancements**
- **API Versioning**: Implement versioning strategy
- **Rate Limiting**: Add rate limiting to APIs
- **Authentication**: Implement JWT authentication
- **API Analytics**: Track API usage and performance
- **Documentation**: Add more detailed examples

### **Integration**
- **Frontend Integration**: Connect with React frontend
- **Mobile APIs**: Optimize for mobile applications
- **Third-party Integration**: Connect with external services
- **Monitoring**: Implement comprehensive monitoring

---

## 🎉 **Summary**

You now have a complete Swagger UI setup for all IRCTC microservices:

✅ **Individual Swagger UIs** for each service  
✅ **Central Swagger Hub** for easy access  
✅ **Eureka Integration** for service discovery  
✅ **API Gateway Routing** for unified access  
✅ **Comprehensive Documentation** for all endpoints  
✅ **Interactive Testing** capabilities  
✅ **Health Monitoring** and metrics  

**Access your APIs**: http://localhost:8096/swagger-ui.html  
**Eureka Dashboard**: http://localhost:8761  
**API Gateway**: http://localhost:8090  

Happy API testing! 🚀📚