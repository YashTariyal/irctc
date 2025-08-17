# IRCTC Backend System

A comprehensive Railway Reservation System backend built with Spring Boot, featuring user management, train operations, booking system, and payment processing.

## 🚀 Features

### ✅ **Core Functionality**
- **User Management**: Registration, authentication, profile management
- **Train Operations**: Train schedules, routes, coach management
- **Booking System**: Ticket booking with PNR generation
- **Seat Management**: Individual seat tracking and availability
- **Payment Processing**: Multiple payment methods and transaction tracking
- **Quota Management**: Ladies, senior citizen, handicapped quotas
- **Cancellation & Refunds**: Booking cancellation and refund processing

### ✅ **Technical Features**
- **RESTful APIs**: Complete REST API with proper HTTP methods
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with password encryption
- **Validation**: Input validation and error handling
- **Documentation**: Comprehensive API documentation
- **Sample Data**: Pre-loaded test data for development

## 🏗️ Architecture

### **Database Schema**
- **9 Main Entities**: Users, Stations, Trains, Coaches, Seats, Passengers, Bookings, Payments, TrainSchedules
- **Relationships**: Proper foreign key relationships between all entities
- **Indexes**: Optimized database indexes for performance

### **Application Layers**
- **Controllers**: REST API endpoints
- **Services**: Business logic layer
- **Repositories**: Data access layer
- **Entities**: JPA entities with proper annotations

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Database**: PostgreSQL 15
- **ORM**: Hibernate/JPA
- **Security**: Spring Security
- **Build Tool**: Maven
- **Java Version**: 17
- **Documentation**: Markdown

## 📋 Prerequisites

- Java 17 or higher
- PostgreSQL 15 or higher
- Maven 3.6+

## 🚀 Quick Start

### 1. **Clone and Setup**
```bash
git clone <repository-url>
cd irctc
```

### 2. **Database Setup**
```sql
-- Create database
CREATE DATABASE irctc;

-- Create user (optional)
CREATE USER irctc_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE irctc TO irctc_user;
```

### 3. **Configure Application**
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/irctc
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. **Run Application**
```bash
./mvnw spring-boot:run
```

### 5. **Access Application**
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **API Base URL**: http://localhost:8080/api

## 📊 Sample Data

The application comes with pre-loaded sample data:

### **Users**
- **Admin**: username: `admin`, password: `admin123`
- **John Doe**: username: `john_doe`, password: `password123`
- **Jane Smith**: username: `jane_smith`, password: `password123`

### **Stations**
- NDLS - New Delhi
- BCT - Mumbai Central
- SBC - Bangalore City
- MAS - Chennai Central
- HWH - Howrah Junction

### **Trains**
- 12345 - Rajdhani Express (New Delhi → Mumbai)
- 12019 - Shatabdi Express (New Delhi → Bangalore)
- 12213 - Duronto Express (Mumbai → Chennai)

### **Coaches**
- AC First Class (A1) - 20 seats
- AC 2 Tier (B1) - 50 seats
- Sleeper Class (S1) - 72 seats

## 🔌 API Endpoints

### **User Management**
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### **Train Management**
- `POST /api/trains` - Create train
- `GET /api/trains` - Get all trains
- `GET /api/trains/active` - Get active trains
- `GET /api/trains/route` - Get trains between stations
- `PUT /api/trains/{id}` - Update train
- `DELETE /api/trains/{id}` - Delete train

### **Booking Management**
- `POST /api/bookings` - Create booking
- `GET /api/bookings` - Get all bookings
- `GET /api/bookings/pnr/{pnr}` - Get booking by PNR
- `GET /api/bookings/user/{userId}` - Get user bookings
- `PUT /api/bookings/{id}/cancel` - Cancel booking

## 🧪 Testing the API

### **Register a User**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "9876543213"
  }'
```

### **Login**
```bash
curl -X POST "http://localhost:8080/api/users/login?username=testuser&password=password123"
```

### **Get Trains (with authentication)**
```bash
curl -X GET http://localhost:8080/api/trains \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/irctc_backend/irctc/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/            # Business Logic
│   │   ├── repository/         # Data Access
│   │   ├── entity/            # JPA Entities
│   │   ├── config/            # Configuration
│   │   └── IrctcApplication.java
│   └── resources/
│       └── application.properties
└── test/                      # Test files
```

## 🔐 Security

- **Authentication**: Basic authentication for API endpoints
- **Password Encryption**: BCrypt password hashing
- **Public Endpoints**: Registration and login
- **Protected Endpoints**: All other operations require authentication

## 📈 Performance Features

- **Database Indexes**: Optimized for common queries
- **Connection Pooling**: HikariCP for database connections
- **Lazy Loading**: JPA lazy loading for relationships
- **Caching**: Spring Boot caching support

## 🚀 Deployment

### **Development**
```bash
./mvnw spring-boot:run
```

### **Production**
```bash
./mvnw clean package
java -jar target/irctc-0.0.1-SNAPSHOT.jar
```

### **Docker** (Future Enhancement)
```bash
docker build -t irctc-backend .
docker run -p 8080:8080 irctc-backend
```

## 📚 Documentation

- **API Documentation**: `API_DOCUMENTATION.md`
- **Database Schema**: `DATABASE_SCHEMA.md`
- **Entity Documentation**: Inline JPA annotations

## 🔧 Configuration

### **Application Properties**
```properties
# Server
server.port=8080
spring.application.name=irctc

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/irctc
spring.datasource.username=postgres
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Security
spring.security.user.name=admin
spring.security.user.password=admin123
```

## 🐛 Troubleshooting

### **Common Issues**

1. **Database Connection Error**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **Port Already in Use**
   - Change port in `application.properties`
   - Kill existing process: `lsof -ti:8080 | xargs kill -9`

3. **Compilation Errors**
   - Ensure Java 17 is installed
   - Run `./mvnw clean compile`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the API documentation

---

**🎉 Congratulations!** Your IRCTC backend system is now ready for development and testing. 