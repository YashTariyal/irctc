# 🚂 IRCTC Backend API

A comprehensive Spring Boot application for Indian Railway Catering and Tourism Corporation (IRCTC) backend services, featuring modern architecture, event-driven design, and comprehensive observability.

## 🌟 Features

### 🏗️ **Core Architecture**
- **Spring Boot 3.5.6** with Java 21 (LTS)
- **Event-Driven Architecture** with Apache Kafka
- **Microservices-Ready** with service discovery
- **RESTful API** with comprehensive documentation
- **Real-time Notifications** via WebSocket

### 🔐 **Security & Compliance**
- **Spring Security 6** with OAuth2/JWT support
- **Security Headers** (HSTS, CSP, X-Content-Type-Options)
- **Input Validation** with Jakarta Bean Validation
- **CORS Configuration** for cross-origin requests

### 📊 **Observability & Monitoring**
- **Micrometer Metrics** with Prometheus integration
- **OpenTelemetry Tracing** for distributed tracing
- **Structured JSON Logging** with Log4j2
- **Spring Boot Actuator** for health checks and metrics
- **Performance Monitoring** with AOP timing aspects

### 🗄️ **Data Management**
- **JPA/Hibernate** with H2 (dev) and PostgreSQL (prod)
- **Database Migrations** with Flyway
- **Caching** with Caffeine
- **Connection Pooling** with HikariCP

### 📡 **Event-Driven Features**
- **Kafka Producers** for booking events
- **Real-time Notifications** for booking confirmations
- **Event Sourcing** for audit trails
- **Message Queuing** for async processing
- **Event Publishing** with comprehensive error handling

### 📚 **API Documentation**
- **Enhanced Swagger UI** with rich documentation
- **Interactive API Testing** with pre-filled examples
- **Organized API Groups** for easy navigation
- **Comprehensive Examples** for all endpoints
- **Security Documentation** with JWT authentication

## 🚀 Quick Start

### Prerequisites
- **Java 21** (LTS)
- **Maven 3.8+**
- **Docker** (for Kafka and PostgreSQL)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/irctc-backend.git
   cd irctc-backend
   ```

2. **Start infrastructure services**
   ```bash
   # Start Kafka and PostgreSQL with Docker Compose
   docker-compose up -d
   ```

3. **Run the application**
   ```bash
   # Development mode
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   
   # Or build and run JAR
   ./mvnw clean package -DskipTests
   java -jar target/irctc-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
   ```

4. **Access the application**
   - **API Base URL**: http://localhost:8082
   - **Enhanced Swagger UI**: http://localhost:8082/swagger-ui/index.html
   - **API Groups**: 6 organized groups for different functionalities
   - **H2 Console**: http://localhost:8082/h2-console
   - **Actuator Health**: http://localhost:8082/actuator/health

5. **Quick Test**
   ```bash
   # Test the enhanced Swagger UI
   open http://localhost:8082/swagger-ui/index.html
   
   # Test API health
   curl http://localhost:8082/actuator/health
   
   # Test event producer (create a booking)
   curl -X POST http://localhost:8082/api/bookings \
     -H "Content-Type: application/json" \
     -d '{"user":{"id":1},"train":{"id":1},"passenger":{"id":1},"coach":{"id":1},"seat":{"id":1},"pnrNumber":"TEST123","journeyDate":"2025-10-15","totalFare":5000.00,"status":"CONFIRMED"}'
   ```

## 📚 API Documentation

### 🔗 **Enhanced Swagger UI**
Access the comprehensive interactive API documentation at: http://localhost:8082/swagger-ui/index.html

**🎯 Key Features:**
- **6 Organized API Groups**: User Management, Train Management, Booking Management, Passenger Management, Dashboard, System & Monitoring
- **Rich Documentation**: Detailed descriptions, examples, and validation rules
- **Interactive Testing**: "Try it out" functionality with pre-filled examples
- **Security Integration**: JWT authentication with clear examples
- **Comprehensive Examples**: Success and error response examples for all endpoints

### 📋 **Available Endpoints**

#### 👥 **User Management**
- `POST /api/users/register` - Register new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

#### 🚂 **Train Management**
- `GET /api/trains` - Get all trains
- `GET /api/trains/{id}` - Get train by ID
- `POST /api/trains` - Create new train
- `PUT /api/trains/{id}` - Update train
- `DELETE /api/trains/{id}` - Delete train

#### 🎫 **Booking Management**
- `GET /api/bookings` - Get all bookings
- `GET /api/bookings/{id}` - Get booking by ID
- `POST /api/bookings` - Create new booking
- `PUT /api/bookings/{id}` - Update booking
- `DELETE /api/bookings/{id}` - Cancel booking

#### 👤 **Passenger Management**
- `GET /api/passengers` - Get all passengers
- `GET /api/passengers/{id}` - Get passenger by ID
- `POST /api/passengers` - Create new passenger
- `PUT /api/passengers/{id}` - Update passenger
- `DELETE /api/passengers/{id}` - Delete passenger

#### 📊 **Dashboard & Analytics**
- `GET /dashboard` - Web dashboard
- `GET /api/dashboard/stats` - Dashboard statistics
- `GET /api/dashboard/bookings` - Booking analytics

### 🔧 **Actuator Endpoints**
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /actuator/info` - Application information

## 🎯 **Event-Driven Architecture**

### 📡 **Kafka Integration**
The application features a comprehensive event-driven architecture with Apache Kafka:

**Event Types:**
- **Booking Events**: `booking-confirmed`, `booking-cancelled`
- **Payment Events**: `payment-confirmed`, `payment-failed`
- **User Events**: `user-registered`, `user-login`

**Event Flow:**
1. **Business Operation** (e.g., booking creation)
2. **Event Generation** (BookingEvent DTO)
3. **Kafka Publishing** (NotificationService)
4. **Event Consumption** (Downstream services)

**Example Event Structure:**
```json
{
  "eventType": "BOOKING_CONFIRMED",
  "bookingId": 123,
  "userId": 1,
  "trainNumber": "12345",
  "pnrNumber": "PNR123456",
  "journeyDate": "2025-10-15",
  "totalFare": 5000.00,
  "timestamp": "2025-10-13T22:30:00Z"
}
```

## 🏗️ **Architecture Overview**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Load Balancer │
│   (React/Vue)   │◄──►│   (Spring)      │◄──►│   (Nginx)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    IRCTC Backend Services                      │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│  User Service   │  Train Service  │ Booking Service │ Passenger │
│                 │                 │                 │ Service   │
└─────────────────┴─────────────────┴─────────────────┴───────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Event-Driven Layer                          │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│  Kafka Topics   │  Event Store    │  Notifications  │ Analytics │
│                 │                 │                 │           │
└─────────────────┴─────────────────┴─────────────────┴───────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Data Layer                                   │
├─────────────────┬─────────────────┬─────────────────┬───────────┤
│  PostgreSQL     │  Redis Cache    │  File Storage   │ Logs      │
│  (Production)   │                 │                 │           │
└─────────────────┴─────────────────┴─────────────────┴───────────┘
```

## 🔧 **Configuration**

### 📁 **Application Profiles**

#### Development (`application-dev.properties`)
```properties
# H2 Database for development
spring.datasource.url=jdbc:h2:mem:irctc_dev
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# Debug logging
logging.level.com.irctc_backend.irctc=DEBUG
```

#### Production (`application-prod.properties`)
```properties
# PostgreSQL for production
spring.datasource.url=jdbc:postgresql://localhost:5432/irctc_prod
spring.jpa.hibernate.ddl-auto=validate

# Security settings
management.endpoints.web.exposure.include=health,info,metrics
```

### 🔐 **Security Configuration**
- **Authentication**: JWT tokens
- **Authorization**: Role-based access control
- **CORS**: Configured for frontend integration
- **Security Headers**: HSTS, CSP, X-Frame-Options

## 📊 **Monitoring & Observability**

### 📈 **Metrics**
- **Micrometer** integration with Prometheus
- **Custom metrics** for business operations
- **JVM metrics** for performance monitoring
- **Database metrics** for query performance

### 🔍 **Tracing**
- **OpenTelemetry** for distributed tracing
- **Request tracing** across services
- **Performance profiling** with timing aspects

### 📝 **Logging**
- **Structured JSON logs** for easy parsing
- **Log levels** configurable per environment
- **Log aggregation** ready for ELK stack
- **Audit trails** for compliance

## 🚀 **Deployment**

### 🐳 **Docker Deployment**
```bash
# Build Docker image
docker build -t irctc-backend .

# Run with Docker Compose
docker-compose up -d
```

### ☁️ **Cloud Deployment**
- **AWS**: ECS, EKS, or EC2
- **Azure**: Container Instances or AKS
- **GCP**: Cloud Run or GKE
- **Kubernetes**: Helm charts available

### 🔄 **CI/CD Pipeline**
- **GitHub Actions** for automated testing
- **Docker Hub** for image registry
- **ArgoCD** for GitOps deployment

## 🧪 **Testing**

### 🔬 **Test Coverage**
- **Unit Tests**: Service and repository layers
- **Integration Tests**: API endpoints
- **Contract Tests**: API specifications
- **Performance Tests**: Load testing with JMeter

### 🏃‍♂️ **Running Tests**
```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=BookingServiceTest
```

### 🚀 **API Testing Examples**

**Test User Registration:**
```bash
curl -X POST http://localhost:8082/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "securePassword123",
    "phoneNumber": "9876543210",
    "dateOfBirth": "1990-01-01T00:00:00",
    "gender": "MALE",
    "address": "123 Main Street, Mumbai, Maharashtra"
  }'
```

**Test Booking Creation:**
```bash
curl -X POST http://localhost:8082/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "user": {"id": 1},
    "train": {"id": 1},
    "passenger": {"id": 1},
    "coach": {"id": 1},
    "seat": {"id": 1},
    "pnrNumber": "PNR123456",
    "journeyDate": "2025-10-15",
    "bookingDate": "2025-10-13T17:00:00",
    "totalFare": 5000.00,
    "baseFare": 4500.00,
    "convenienceFee": 90.00,
    "gstAmount": 225.00,
    "status": "CONFIRMED",
    "paymentStatus": "PENDING",
    "quotaType": "GENERAL",
    "isTatkal": false,
    "isCancelled": false,
    "bookingSource": "WEB"
  }'
```

## 📦 **Dependencies**

### 🏗️ **Core Dependencies**
- **Spring Boot 3.5.6** - Application framework
- **Spring Security 6** - Security framework
- **Spring Data JPA** - Data access layer
- **Spring Kafka** - Event streaming
- **Hibernate** - ORM framework

### 📊 **Observability Dependencies**
- **Micrometer** - Metrics collection
- **OpenTelemetry** - Distributed tracing
- **Log4j2** - Logging framework
- **Spring Boot Actuator** - Health checks

### 🗄️ **Database Dependencies**
- **H2** - In-memory database (dev)
- **PostgreSQL** - Production database
- **HikariCP** - Connection pooling
- **Flyway** - Database migrations

## 🤝 **Contributing**

### 📋 **Development Guidelines**
1. **Fork** the repository
2. **Create** a feature branch
3. **Write** tests for new features
4. **Follow** code style guidelines
5. **Submit** a pull request

### 🔧 **Code Style**
- **Java**: Google Java Style Guide
- **Comments**: Javadoc for public APIs
- **Tests**: Minimum 80% coverage
- **Commits**: Conventional commit messages

## 📄 **License**

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 👥 **Team**

- **Backend Development**: Spring Boot, Kafka, PostgreSQL
- **DevOps**: Docker, Kubernetes, CI/CD
- **QA**: Testing, Performance, Security

## 📞 **Support**

- **Documentation**: [Wiki](https://github.com/yourusername/irctc-backend/wiki)
- **Issues**: [GitHub Issues](https://github.com/yourusername/irctc-backend/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/irctc-backend/discussions)
- **Email**: support@irctc.com

## 🎯 **Roadmap**

### 🚀 **Upcoming Features**
- [ ] **GraphQL API** for flexible data querying
- [ ] **WebSocket** real-time updates
- [ ] **Mobile API** optimization
- [ ] **AI/ML** integration for recommendations
- [ ] **Microservices** decomposition
- [ ] **Event Sourcing** implementation

### 🔄 **Version History**
- **v1.0.0** - Initial release with core features
- **v1.1.0** - Added event-driven architecture
- **v1.2.0** - Enhanced observability and monitoring
- **v1.3.0** - Security improvements and API documentation
- **v1.4.0** - Enhanced Swagger documentation with comprehensive examples
- **v1.5.0** - Event producer system with Kafka integration

---

## 🏆 **Acknowledgments**

- **Spring Team** for the excellent framework
- **Apache Kafka** for event streaming capabilities
- **OpenTelemetry** for observability standards
- **Community** for contributions and feedback

---

<div align="center">

**🚂 Built with ❤️ for Indian Railways**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.6-blue.svg)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>