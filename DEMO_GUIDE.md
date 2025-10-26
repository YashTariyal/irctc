# 🚀 IRCTC Demo Guide

## 🌐 **Live Demo Access**

### **Quick Start Demo**
```bash
# Clone and run the demo
git clone https://github.com/YashTariyal/irctc.git
cd irctc
chmod +x demo-setup.sh
./demo-setup.sh
```

### **Demo URLs**
- **🎨 Frontend Dashboard**: http://localhost:3000
- **🔧 Backend APIs**: http://localhost:8082
- **📚 Swagger Documentation**: http://localhost:8082/swagger-ui.html
- **📊 Performance Dashboard**: http://localhost:8082/dashboard
- **🌐 Eureka Service Registry**: http://localhost:8761

### **Demo Credentials**
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@irctc.com`

## 🎯 **Demo Features**

### **1. User Management**
- ✅ User registration with validation
- ✅ Secure login with JWT authentication
- ✅ Profile management
- ✅ Role-based access control

### **2. Train Search & Booking**
- ✅ Advanced train search with filters
- ✅ Multi-city trip planning
- ✅ Real-time seat availability
- ✅ Interactive seat selection
- ✅ Waitlist and RAC management

### **3. Payment System**
- ✅ Razorpay payment gateway integration
- ✅ Secure transaction processing
- ✅ Refund management
- ✅ Payment history tracking

### **4. Advanced Features**
- ✅ Loyalty points system
- ✅ Travel insurance booking
- ✅ Meal/catering booking
- ✅ Real-time notifications
- ✅ Performance monitoring dashboard

## 🛠️ **Technology Stack Demo**

### **Backend Services**
- **Spring Boot 3.5.6** with Java 21
- **PostgreSQL** database with H2 for development
- **Apache Kafka** for event streaming
- **Redis** for caching
- **JWT** for authentication
- **Swagger/OpenAPI 3** for documentation

### **Frontend**
- **React 19** with TypeScript
- **Material-UI** components
- **Axios** for API calls
- **React Router** for navigation
- **Recharts** for data visualization

### **Microservices Architecture**
- **Eureka Server** for service discovery
- **API Gateway** for routing
- **Docker** containerization
- **Docker Compose** for orchestration

## 📊 **Demo Scenarios**

### **Scenario 1: Complete Booking Flow**
1. Register a new user account
2. Search for trains between Delhi and Mumbai
3. Select seats and book tickets
4. Process payment through Razorpay
5. Receive booking confirmation
6. View booking in "My Bookings"

### **Scenario 2: Advanced Features**
1. Login with existing account
2. Check loyalty points balance
3. Book travel insurance
4. Order meals for journey
5. Plan multi-city trip
6. View performance dashboard

### **Scenario 3: Admin Functions**
1. Login as admin user
2. View all bookings
3. Manage train schedules
4. Monitor system performance
5. Handle user management

## 🔧 **Local Development Setup**

### **Prerequisites**
- Java 21+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL (optional)

### **Backend Setup**
```bash
# Start backend services
./mvnw spring-boot:run

# Or start microservices
./start-microservices.sh
```

### **Frontend Setup**
```bash
cd irctc-frontend
npm install
npm start
```

## 📱 **API Testing**

### **Using Swagger UI**
- Visit: http://localhost:8082/swagger-ui.html
- Test all endpoints interactively
- View request/response schemas
- Authenticate with JWT tokens

### **Using cURL**
```bash
# Get authentication token
TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.accessToken')

# Test API endpoints
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/trains
```

## 🎉 **Demo Highlights**

- **Complete Railway Booking System** with all major features
- **Modern Tech Stack** using latest Java and React versions
- **Microservices Architecture** with service discovery
- **Real-time Features** with WebSocket and Kafka
- **Payment Integration** with Razorpay
- **Performance Monitoring** with custom dashboard
- **Responsive Design** for mobile and desktop
- **Comprehensive Documentation** with Swagger UI

## 🚀 **Production Deployment**

The system is ready for production deployment with:
- Docker containerization
- Database migrations
- Environment configuration
- Security hardening
- Performance optimization
- Monitoring and logging

---

**🎯 Ready to explore? Run `./demo-setup.sh` and start your IRCTC journey!**
