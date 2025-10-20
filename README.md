# 🚂 IRCTC Backend System

A comprehensive Spring Boot-based backend system for Indian Railway Catering and Tourism Corporation (IRCTC) with advanced features including multi-city trip planning, seat selection, loyalty programs, travel insurance, and meal booking.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [Database Schema](#-database-schema)
- [API Documentation](#-api-documentation)
- [Setup & Installation](#-setup--installation)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🚀 Features

### Core Features
- **User Management**: Registration, authentication, profile management
- **Train Management**: Train schedules, routes, and availability
- **Booking System**: Complete booking lifecycle with PNR tracking
- **Payment Integration**: Razorpay payment gateway integration
- **Seat Selection**: Interactive seat selection with visual coach layout
- **Waitlist & RAC**: Advanced waitlist and RAC management
- **Mobile APIs**: Optimized endpoints with pagination

### Advanced Features
- **Multi-City Trip Planning**: Connected journey planning with transfers
- **Dynamic Fare Calculation**: Surge pricing and discount management
- **Loyalty Points System**: Tier-based rewards and redemption
- **Travel Insurance**: Comprehensive insurance booking and management
- **Meal/Catering Booking**: Station-wise meal vendor integration
- **Real-time Notifications**: WebSocket and Kafka-based notifications
- **Dashboard Analytics**: Performance monitoring and insights

### Security & Performance
- **JWT Authentication**: Bearer token-based security
- **Role-based Access Control**: USER, ADMIN, SUPER_ADMIN roles
- **Caching**: Caffeine-based caching for performance
- **Monitoring**: Micrometer metrics and OpenTelemetry tracing
- **Logging**: Structured logging with Log4j2

## 🛠 Technology Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.5.6** - Application framework
- **Spring Security 6** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework

### Database
- **H2** - Development database (in-memory)
- **PostgreSQL** - Production database
- **HikariCP** - Connection pooling

### Messaging & Real-time
- **Apache Kafka** - Event streaming
- **WebSocket** - Real-time notifications

### External Services
- **Razorpay** - Payment gateway
- **OpenTelemetry** - Distributed tracing

### Documentation & Monitoring
- **Swagger/OpenAPI 3** - API documentation
- **Micrometer** - Application metrics
- **Log4j2** - Logging framework

## 🏗 Project Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    IRCTC Backend System                     │
├─────────────────────────────────────────────────────────────┤
│  Presentation Layer (Controllers)                          │
│  ├── AuthenticationController                              │
│  ├── UserController                                        │
│  ├── TrainController                                       │
│  ├── BookingController                                     │
│  ├── PaymentController                                     │
│  ├── SeatSelectionController                               │
│  ├── WaitlistRacController                                 │
│  ├── FareCalculationController                             │
│  ├── LoyaltyController                                     │
│  ├── TravelInsuranceController                             │
│  ├── MealBookingController                                 │
│  ├── TripPlannerController                                 │
│  └── MobileApiController                                   │
├─────────────────────────────────────────────────────────────┤
│  Business Logic Layer (Services)                           │
│  ├── AuthenticationService                                 │
│  ├── UserService                                           │
│  ├── TrainService                                          │
│  ├── BookingService                                        │
│  ├── PaymentService                                        │
│  ├── SeatSelectionService                                  │
│  ├── WaitlistRacService                                    │
│  ├── FareCalculationService                                │
│  ├── LoyaltyService                                        │
│  ├── TravelInsuranceService                                │
│  ├── MealBookingService                                    │
│  └── TripPlannerService                                    │
├─────────────────────────────────────────────────────────────┤
│  Data Access Layer (Repositories)                          │
│  ├── UserRepository                                        │
│  ├── TrainRepository                                       │
│  ├── BookingRepository                                     │
│  ├── PaymentRepository                                     │
│  ├── SeatRepository                                        │
│  ├── WaitlistRepository                                    │
│  ├── RacRepository                                         │
│  ├── FareRuleRepository                                    │
│  ├── LoyaltyAccountRepository                              │
│  ├── TravelInsuranceRepository                             │
│  ├── MealVendorRepository                                  │
│  └── StationRepository                                     │
├─────────────────────────────────────────────────────────────┤
│  Data Layer (Entities)                                     │
│  ├── User, Train, Booking, Payment                         │
│  ├── Seat, Coach, Station                                  │
│  ├── WaitlistEntry, RacEntry                              │
│  ├── FareRule, LoyaltyAccount                              │
│  ├── TravelInsurance, MealVendor                           │
│  └── Supporting Entities                                   │
└─────────────────────────────────────────────────────────────┘
```

## 🗄 Database Schema

### Core Entities

#### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15) UNIQUE,
    date_of_birth TIMESTAMP,
    gender VARCHAR(10),
    id_proof_type VARCHAR(20),
    id_proof_number VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Trains Table
```sql
CREATE TABLE trains (
    id BIGSERIAL PRIMARY KEY,
    train_number VARCHAR(10) UNIQUE NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    source_station_id BIGINT REFERENCES stations(id),
    destination_station_id BIGINT REFERENCES stations(id),
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    total_distance DECIMAL(10,2),
    train_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_running BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Bookings Table
```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    pnr_number VARCHAR(10) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id),
    train_id BIGINT REFERENCES trains(id),
    passenger_id BIGINT REFERENCES passengers(id),
    seat_id BIGINT REFERENCES seats(id),
    coach_id BIGINT REFERENCES coaches(id),
    journey_date DATE NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    total_fare DECIMAL(10,2) NOT NULL,
    base_fare DECIMAL(10,2),
    tatkal_fare DECIMAL(10,2),
    premium_tatkal_fare DECIMAL(10,2),
    quota_type VARCHAR(20) DEFAULT 'GENERAL',
    booking_status VARCHAR(20) DEFAULT 'CONFIRMED',
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    is_tatkal BOOLEAN DEFAULT FALSE,
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(255),
    refund_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Seats Table
```sql
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT REFERENCES coaches(id),
    seat_number VARCHAR(10) NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    berth_type VARCHAR(20),
    is_available BOOLEAN DEFAULT TRUE,
    is_ladies_quota BOOLEAN DEFAULT FALSE,
    is_senior_citizen_quota BOOLEAN DEFAULT FALSE,
    is_handicapped_friendly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Advanced Features Tables

#### Loyalty Accounts Table
```sql
CREATE TABLE loyalty_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) UNIQUE,
    loyalty_number VARCHAR(20) UNIQUE NOT NULL,
    tier VARCHAR(20) DEFAULT 'BRONZE',
    total_points DECIMAL(10,2) DEFAULT 0,
    available_points DECIMAL(10,2) DEFAULT 0,
    redeemed_points DECIMAL(10,2) DEFAULT 0,
    expired_points DECIMAL(10,2) DEFAULT 0,
    total_spent DECIMAL(12,2) DEFAULT 0,
    total_bookings INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    joined_date TIMESTAMP,
    last_activity_date TIMESTAMP,
    points_expiry_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Travel Insurance Table
```sql
CREATE TABLE travel_insurance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    booking_id BIGINT REFERENCES bookings(id),
    provider_id BIGINT REFERENCES insurance_providers(id),
    plan_id BIGINT REFERENCES insurance_plans(id),
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    coverage_amount DECIMAL(10,2) NOT NULL,
    premium_amount DECIMAL(10,2) NOT NULL,
    gst_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    traveler_name VARCHAR(100) NOT NULL,
    traveler_age INTEGER NOT NULL,
    traveler_gender VARCHAR(10) NOT NULL,
    traveler_phone VARCHAR(15),
    traveler_email VARCHAR(100),
    has_pre_existing_medical_condition BOOLEAN DEFAULT FALSE,
    medical_condition_details TEXT,
    claim_status VARCHAR(20) DEFAULT 'NO_CLAIM',
    claim_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Meal Orders Table
```sql
CREATE TABLE meal_orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    booking_id BIGINT REFERENCES bookings(id),
    train_id BIGINT REFERENCES trains(id),
    vendor_id BIGINT REFERENCES meal_vendors(id),
    order_number VARCHAR(20) UNIQUE NOT NULL,
    order_date TIMESTAMP NOT NULL,
    delivery_date DATE NOT NULL,
    delivery_time TIME NOT NULL,
    delivery_station_code VARCHAR(10) NOT NULL,
    delivery_seat_number VARCHAR(10),
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    order_status VARCHAR(20) DEFAULT 'PLACED',
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📚 API Documentation

### Authentication APIs

#### 1. User Registration
```http
POST /api/users/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1990-01-15T00:00:00",
  "gender": "MALE",
  "address": "123 Main Street, City, State"
}
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1990-01-15T00:00:00",
  "gender": "MALE",
  "isVerified": false,
  "isActive": true,
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 2. User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "refreshExpiresIn": 604800000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

### Train Management APIs

#### 3. Get All Trains
```http
GET /api/trains
Authorization: Bearer <access_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "trainNumber": "12001",
    "trainName": "Shatabdi Express",
    "sourceStation": {
      "id": 1,
      "stationCode": "NDLS",
      "stationName": "New Delhi"
    },
    "destinationStation": {
      "id": 2,
      "stationCode": "AGC",
      "stationName": "Agra Cantt"
    },
    "departureTime": "06:00:00",
    "arrivalTime": "08:30:00",
    "totalDistance": 200.5,
    "trainType": "EXPRESS",
    "status": "ACTIVE",
    "isRunning": true
  }
]
```

#### 4. Search Trains
```http
POST /api/trains/search
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "sourceStationCode": "NDLS",
  "destinationStationCode": "MUMB",
  "journeyDate": "2024-01-20",
  "preferredClass": "AC2",
  "numberOfPassengers": 2
}
```

**Response:**
```json
[
  {
    "id": 1,
    "trainNumber": "12951",
    "trainName": "Mumbai Rajdhani",
    "sourceStationCode": "NDLS",
    "destinationStationCode": "MUMB",
    "departureTime": "16:35:00",
    "arrivalTime": "08:30:00",
    "journeyDuration": "15h 55m",
    "totalDistance": 1384.0,
    "trainType": "RAJDHANI",
    "status": "ACTIVE",
    "isRunning": true,
    "availableSeats": 45,
    "startingFare": 2500.00,
    "isTatkalAvailable": true,
    "isPremiumTatkalAvailable": true
  }
]
```

### Booking APIs

#### 5. Create Booking
```http
POST /api/bookings
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "trainId": 1,
  "passengerId": 1,
  "seatId": 5,
  "coachId": 1,
  "journeyDate": "2024-01-20",
  "totalFare": 2500.00,
  "baseFare": 2000.00,
  "quotaType": "GENERAL",
  "isTatkal": false
}
```

**Response:**
```json
{
  "id": 1,
  "pnrNumber": "PNR123456",
  "user": {
    "id": 1,
    "username": "johndoe"
  },
  "train": {
    "id": 1,
    "trainNumber": "12951",
    "trainName": "Mumbai Rajdhani"
  },
  "passenger": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  },
  "seat": {
    "id": 5,
    "seatNumber": "LB-5",
    "seatType": "LOWER_BERTH"
  },
  "coach": {
    "id": 1,
    "coachNumber": "A1",
    "coachType": "AC2"
  },
  "journeyDate": "2024-01-20",
  "bookingDate": "2024-01-15T10:30:00",
  "totalFare": 2500.00,
  "baseFare": 2000.00,
  "quotaType": "GENERAL",
  "bookingStatus": "CONFIRMED",
  "paymentStatus": "PENDING",
  "isTatkal": false,
  "isCancelled": false
}
```

#### 6. Get Booking by PNR
```http
GET /api/bookings/pnr/PNR123456
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "id": 1,
  "pnrNumber": "PNR123456",
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "journeyDate": "2024-01-20",
  "bookingDate": "2024-01-15T10:30:00",
  "status": "CONFIRMED",
  "paymentStatus": "COMPLETED",
  "quotaType": "GENERAL",
  "passengerCount": 1,
  "totalAmount": 2500.00,
  "coachNumber": "A1",
  "seatNumbers": ["LB-5"],
  "berthTypes": ["LOWER_BERTH"],
  "isTatkal": false,
  "isCancelled": false
}
```

### Seat Selection APIs

#### 7. Get Available Seats
```http
GET /api/seat-selection/trains/1/coaches/1/seats?journeyDate=2024-01-20
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "trainId": 1,
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "coachId": 1,
  "coachNumber": "A1",
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "availableSeats": [
    {
      "id": 1,
      "seatNumber": "LB-1",
      "seatType": "LOWER_BERTH",
      "berthType": "LOWER_BERTH",
      "isAvailable": true,
      "isLadiesQuota": false,
      "isSeniorCitizenQuota": false,
      "isHandicappedFriendly": false,
      "fare": 2500.00
    }
  ],
  "totalSeats": 72,
  "availableSeatsCount": 45,
  "selectionStatus": "AVAILABLE",
  "message": "Seats available for selection"
}
```

#### 8. Select Seats
```http
POST /api/seat-selection/select
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "trainId": 1,
  "coachId": 1,
  "journeyDate": "2024-01-20",
  "seatPreferences": [
    {
      "seatId": 1,
      "passengerName": "John Doe",
      "passengerAge": 30,
      "passengerGender": "MALE"
    }
  ]
}
```

**Response:**
```json
{
  "trainId": 1,
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "coachId": 1,
  "coachNumber": "A1",
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "selectedSeats": [
    {
      "id": 1,
      "seatNumber": "LB-1",
      "seatType": "LOWER_BERTH",
      "berthType": "LOWER_BERTH",
      "isAvailable": false,
      "fare": 2500.00
    }
  ],
  "totalFare": 2500.00,
  "totalSeats": 1,
  "selectionStatus": "SELECTED",
  "message": "Seats selected successfully"
}
```

### Payment APIs

#### 9. Create Payment
```http
POST /api/payments/create
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "bookingId": 1,
  "amount": 2500.00,
  "paymentMethod": "RAZORPAY",
  "currency": "INR"
}
```

**Response:**
```json
{
  "id": 1,
  "bookingId": 1,
  "amount": 2500.00,
  "paymentMethod": "RAZORPAY",
  "currency": "INR",
  "status": "PENDING",
  "razorpayOrderId": "order_1234567890",
  "razorpayPaymentId": null,
  "razorpaySignature": null,
  "paymentUrl": "https://checkout.razorpay.com/v1/checkout.js",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### 10. Verify Payment
```http
POST /api/payments/verify
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "razorpayOrderId": "order_1234567890",
  "razorpayPaymentId": "pay_1234567890",
  "razorpaySignature": "signature_1234567890"
}
```

**Response:**
```json
{
  "id": 1,
  "bookingId": 1,
  "amount": 2500.00,
  "paymentMethod": "RAZORPAY",
  "currency": "INR",
  "status": "COMPLETED",
  "razorpayOrderId": "order_1234567890",
  "razorpayPaymentId": "pay_1234567890",
  "razorpaySignature": "signature_1234567890",
  "paymentDate": "2024-01-15T10:35:00",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Waitlist & RAC APIs

#### 11. Add to Waitlist
```http
POST /api/waitlist-rac/add
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "trainId": 1,
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "numberOfPassengers": 2,
  "preferredSeatType": "LOWER_BERTH",
  "preferredBerthType": "LOWER_BERTH",
  "isLadiesQuota": false,
  "isSeniorCitizenQuota": false,
  "isHandicappedFriendly": false
}
```

**Response:**
```json
{
  "waitlistId": 1,
  "pnrNumber": "WL123456",
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "waitlistNumber": 5,
  "status": "WAITLISTED",
  "message": "Successfully added to waitlist",
  "bookingId": 1,
  "userId": 1,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Fare Calculation APIs

#### 12. Calculate Fare
```http
POST /api/fare-calculation/calculate
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "trainId": 1,
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "numberOfPassengers": 2,
  "isLadiesQuota": false,
  "isSeniorCitizenQuota": false,
  "isHandicappedFriendly": false,
  "isTatkal": false,
  "isPremiumTatkal": false,
  "departureTime": "16:35:00",
  "arrivalTime": "08:30:00"
}
```

**Response:**
```json
{
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "coachType": "AC2",
  "journeyDate": "2024-01-20",
  "numberOfPassengers": 2,
  "baseFare": 4000.00,
  "tatkalFare": 0.00,
  "premiumTatkalFare": 0.00,
  "ladiesQuotaDiscount": 0.00,
  "seniorCitizenDiscount": 0.00,
  "handicappedDiscount": 0.00,
  "totalDiscount": 0.00,
  "surgeMultiplier": 1.0,
  "peakHourMultiplier": 1.0,
  "weekendMultiplier": 1.0,
  "festivalMultiplier": 1.0,
  "totalSurgeMultiplier": 1.0,
  "isSurgeActive": false,
  "surgeReason": null,
  "subtotalFare": 4000.00,
  "gstAmount": 720.00,
  "totalFare": 4720.00,
  "finalAmount": 4720.00,
  "currency": "INR",
  "passengerFareDetails": [
    {
      "passengerNumber": 1,
      "baseFare": 2000.00,
      "discounts": 0.00,
      "surcharges": 0.00,
      "gst": 360.00,
      "totalFare": 2360.00
    }
  ]
}
```

### Loyalty System APIs

#### 13. Get Loyalty Account
```http
GET /api/loyalty/account?userId=1
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "userId": 1,
  "username": "johndoe",
  "totalPoints": 5000.00,
  "availablePoints": 4500.00,
  "tier": "SILVER",
  "tierProgress": 75.0,
  "nextTier": "GOLD",
  "pointsToNextTier": 1500.00,
  "totalSpent": 25000.00,
  "totalBookings": 15,
  "lastActivityDate": "2024-01-15T10:30:00",
  "createdAt": "2024-01-01T00:00:00"
}
```

#### 14. Redeem Reward
```http
POST /api/loyalty/rewards/redeem
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "rewardId": 1,
  "userId": 1
}
```

**Response:**
```json
{
  "redemptionId": 1,
  "rewardName": "Travel Voucher",
  "pointsUsed": 1000.00,
  "rewardValue": 500.00,
  "status": "REDEEMED",
  "redemptionDate": "2024-01-15T10:30:00",
  "expiryDate": "2024-04-15T10:30:00"
}
```

### Travel Insurance APIs

#### 15. Get Insurance Quote
```http
POST /api/travel-insurance/quote
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "planId": 1,
  "coverageAmount": 100000.00,
  "travelerAge": 30,
  "hasPreExistingMedicalCondition": false,
  "journeyStartDate": "2024-01-20",
  "journeyEndDate": "2024-01-25"
}
```

**Response:**
```json
{
  "planId": 1,
  "planName": "Basic Travel Insurance",
  "providerName": "TravelGuard India",
  "coverageAmount": 100000.00,
  "travelerAge": 30,
  "journeyStartDate": "2024-01-20",
  "journeyEndDate": "2024-01-25",
  "basePremium": 500.00,
  "ageMultiplier": 1.0,
  "medicalConditionMultiplier": 1.0,
  "totalPremiumBeforeGST": 500.00,
  "gstAmount": 90.00,
  "finalPremium": 590.00,
  "currency": "INR",
  "coverageDetails": {
    "medicalExpenses": true,
    "tripCancellation": true,
    "baggageLoss": true,
    "personalAccident": true,
    "emergencyEvacuation": false,
    "support24x7": true
  }
}
```

#### 16. Purchase Insurance
```http
POST /api/travel-insurance/purchase
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "planId": 1,
  "bookingId": 1,
  "coverageAmount": 100000.00,
  "travelerName": "John Doe",
  "travelerAge": 30,
  "travelerGender": "MALE",
  "travelerPhone": "9876543210",
  "travelerEmail": "john.doe@example.com",
  "hasPreExistingMedicalCondition": false,
  "medicalConditionDetails": null,
  "journeyStartDate": "2024-01-20",
  "journeyEndDate": "2024-01-25"
}
```

**Response:**
```json
{
  "id": 1,
  "policyNumber": "TI202401150001",
  "user": {
    "id": 1,
    "username": "johndoe"
  },
  "booking": {
    "id": 1,
    "pnrNumber": "PNR123456"
  },
  "provider": {
    "id": 1,
    "providerName": "TravelGuard India"
  },
  "plan": {
    "id": 1,
    "planName": "Basic Travel Insurance"
  },
  "coverageAmount": 100000.00,
  "premiumAmount": 500.00,
  "gstAmount": 90.00,
  "totalAmount": 590.00,
  "startDate": "2024-01-20",
  "endDate": "2024-01-25",
  "status": "ACTIVE",
  "travelerName": "John Doe",
  "travelerAge": 30,
  "travelerGender": "MALE",
  "travelerPhone": "9876543210",
  "travelerEmail": "john.doe@example.com",
  "hasPreExistingMedicalCondition": false,
  "claimStatus": "NO_CLAIM",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Meal Booking APIs

#### 17. Get Meal Vendors
```http
GET /api/meal-booking/vendors
Authorization: Bearer <access_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "vendorName": "Railway Catering Services",
    "stationCode": "NDLS",
    "stationName": "New Delhi",
    "contactEmail": "contact@railwaycatering.com",
    "contactPhone": "011-23456789",
    "rating": 4.5,
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

#### 18. Get Menu by Vendor
```http
GET /api/meal-booking/menu/vendor/1
Authorization: Bearer <access_token>
```

**Response:**
```json
[
  {
    "id": 1,
    "vendor": {
      "id": 1,
      "vendorName": "Railway Catering Services"
    },
    "itemName": "Vegetable Biryani",
    "description": "Aromatic basmati rice with mixed vegetables",
    "price": 150.00,
    "category": "MAIN_COURSE",
    "mealType": "LUNCH",
    "isVegetarian": true,
    "isAvailable": true,
    "preparationTimeMinutes": 15,
    "imageUrl": "https://example.com/biryani.jpg",
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

#### 19. Place Meal Order
```http
POST /api/meal-booking/order
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "userId": 1,
  "bookingId": 1,
  "trainId": 1,
  "vendorId": 1,
  "deliveryDate": "2024-01-20",
  "deliveryTime": "12:00:00",
  "deliveryStationCode": "AGC",
  "deliverySeatNumber": "LB-5",
  "specialInstructions": "Less spicy",
  "orderItems": [
    {
      "mealItemId": 1,
      "quantity": 2,
      "pricePerItem": 150.00
    }
  ]
}
```

**Response:**
```json
{
  "orderId": 1,
  "orderNumber": "MO202401150001",
  "userId": 1,
  "username": "johndoe",
  "bookingId": 1,
  "pnrNumber": "PNR123456",
  "trainId": 1,
  "trainNumber": "12951",
  "vendorId": 1,
  "vendorName": "Railway Catering Services",
  "orderDate": "2024-01-15T10:30:00",
  "deliveryDate": "2024-01-20",
  "deliveryTime": "12:00:00",
  "deliveryStationCode": "AGC",
  "deliverySeatNumber": "LB-5",
  "totalAmount": 300.00,
  "paymentStatus": "PENDING",
  "orderStatus": "PLACED",
  "specialInstructions": "Less spicy",
  "orderItems": [
    {
      "id": 1,
      "mealItem": {
        "id": 1,
        "itemName": "Vegetable Biryani",
        "price": 150.00
      },
      "quantity": 2,
      "pricePerItem": 150.00,
      "subtotal": 300.00
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

### Trip Planner APIs

#### 20. Search Multi-City Itineraries
```http
POST /api/trip-planner/search
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "sourceStationCode": "NDLS",
  "destinationStationCode": "MUMB",
  "journeyDate": "2024-01-20",
  "earliestDeparture": "06:00:00",
  "latestArrival": "23:59:59",
  "maxConnections": 1,
  "maxTotalDurationMinutes": 1440,
  "preferFastest": true,
  "allowOvernight": true,
  "preferredTrainTypes": ["RAJDHANI", "EXPRESS"]
}
```

**Response:**
```json
[
  {
    "sourceStationCode": "NDLS",
    "destinationStationCode": "MUMB",
    "journeyDate": "2024-01-20",
    "totalDurationMinutes": 955,
    "totalConnections": 0,
    "estimatedFare": 2500.00,
    "overnight": true,
    "legs": [
      {
        "trainId": 1,
        "trainNumber": "12951",
        "trainName": "Mumbai Rajdhani",
        "fromStationCode": "NDLS",
        "fromStationName": "New Delhi",
        "toStationCode": "MUMB",
        "toStationName": "Mumbai Central",
        "departureTime": "16:35:00",
        "arrivalTime": "08:30:00",
        "durationMinutes": 955,
        "coachType": "AC2",
        "tatkalAvailable": true
      }
    ]
  },
  {
    "sourceStationCode": "NDLS",
    "destinationStationCode": "MUMB",
    "journeyDate": "2024-01-20",
    "totalDurationMinutes": 1200,
    "totalConnections": 1,
    "estimatedFare": 1800.00,
    "overnight": true,
    "legs": [
      {
        "trainId": 2,
        "trainNumber": "12001",
        "trainName": "Shatabdi Express",
        "fromStationCode": "NDLS",
        "fromStationName": "New Delhi",
        "toStationCode": "AGC",
        "toStationName": "Agra Cantt",
        "departureTime": "06:00:00",
        "arrivalTime": "08:30:00",
        "durationMinutes": 150,
        "coachType": "CC",
        "tatkalAvailable": true
      },
      {
        "trainId": 3,
        "trainNumber": "12951",
        "trainName": "Mumbai Rajdhani",
        "fromStationCode": "AGC",
        "fromStationName": "Agra Cantt",
        "toStationCode": "MUMB",
        "toStationName": "Mumbai Central",
        "departureTime": "09:00:00",
        "arrivalTime": "08:30:00",
        "durationMinutes": 1050,
        "coachType": "AC2",
        "tatkalAvailable": true
      }
    ]
  }
]
```

### Mobile API Endpoints

#### 21. Get Mobile Train List (Paginated)
```http
GET /api/mobile/trains?page=0&size=10&sortBy=departureTime&sortDirection=ASC
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "trainNumber": "12951",
      "trainName": "Mumbai Rajdhani",
      "sourceStationCode": "NDLS",
      "sourceStationName": "New Delhi",
      "destinationStationCode": "MUMB",
      "destinationStationName": "Mumbai Central",
      "departureTime": "16:35:00",
      "arrivalTime": "08:30:00",
      "journeyDuration": "15h 55m",
      "totalDistance": 1384.0,
      "trainType": "RAJDHANI",
      "status": "ACTIVE",
      "isRunning": true,
      "availableSeats": 45,
      "startingFare": 2500.00,
      "isTatkalAvailable": true,
      "isPremiumTatkalAvailable": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "sortBy": "departureTime",
  "sortDirection": "ASC"
}
```

#### 22. Get User Bookings (Mobile)
```http
GET /api/mobile/users/1/bookings?page=0&size=5&sortBy=bookingDate&sortDirection=DESC
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "pnrNumber": "PNR123456",
      "trainNumber": "12951",
      "trainName": "Mumbai Rajdhani",
      "journeyDate": "2024-01-20",
      "bookingDate": "2024-01-15T10:30:00",
      "status": "CONFIRMED",
      "paymentStatus": "COMPLETED",
      "quotaType": "GENERAL",
      "passengerCount": 1,
      "totalAmount": 2500.00,
      "coachNumber": "A1",
      "seatNumbers": ["LB-5"],
      "berthTypes": ["LOWER_BERTH"],
      "isTatkal": false,
      "isCancelled": false,
      "cancelledAt": null,
      "cancellationReason": null,
      "refundAmount": null
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 15,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "sortBy": "bookingDate",
  "sortDirection": "DESC"
}
```

## ⚙️ Setup & Installation

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- PostgreSQL 13+ (for production)
- Apache Kafka 2.8+ (optional, for notifications)
- Docker (optional, for containerized deployment)

### Local Development Setup

1. **Clone the repository**
```bash
git clone https://github.com/YashTariyal/irctc.git
cd irctc
```

2. **Configure application properties**
```bash
# Copy development configuration
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties

# Edit the configuration file
nano src/main/resources/application-dev.properties
```

3. **Set up the database**
```bash
# For development (H2 in-memory database)
# No setup required - H2 starts automatically

# For production (PostgreSQL)
createdb irctc
psql irctc < database/schema.sql
```

4. **Build and run the application**
```bash
# Build the project
./mvnw clean package

# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the JAR file
java -jar target/irctc-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

5. **Access the application**
- **API Base URL**: http://localhost:8082
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **Dashboard**: http://localhost:8082/dashboard
- **Health Check**: http://localhost:8082/actuator/health

### Docker Setup

1. **Build Docker image**
```bash
docker build -t irctc-backend .
```

2. **Run with Docker Compose**
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f irctc-backend
```

## 🔧 Configuration

### Application Properties

#### Database Configuration
```properties
# Development (H2)
spring.datasource.url=jdbc:h2:mem:irctc_dev
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Production (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/irctc
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

#### JWT Configuration
```properties
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 hours
jwt.refresh-expiration=604800000  # 7 days
```

#### Payment Gateway Configuration
```properties
payment.razorpay.key-id=your_razorpay_key_id
payment.razorpay.key-secret=your_razorpay_key_secret
payment.razorpay.webhook-secret=your_webhook_secret
```

#### Kafka Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

### Environment Variables

```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=irctc
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

# JWT
export JWT_SECRET=your-secret-key-here

# Payment Gateway
export RAZORPAY_KEY_ID=your_razorpay_key_id
export RAZORPAY_KEY_SECRET=your_razorpay_key_secret

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 🧪 Testing

### Unit Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=BookingServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
./mvnw verify

# Run with specific profile
./mvnw verify -Dspring.profiles.active=test
```

### API Testing

#### Using cURL
```bash
# Get authentication token
TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  jq -r '.accessToken')

# Test API endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/trains
```

#### Using Postman
1. Import the Postman collection from `docs/postman/IRCTC_API_Collection.json`
2. Set up environment variables for base URL and tokens
3. Run the collection tests

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/trains

# Using JMeter
jmeter -n -t docs/jmeter/IRCTC_Load_Test.jmx -l results.jtl
```

## 🚀 Deployment

### Production Deployment

#### 1. Build for Production
```bash
# Build with production profile
./mvnw clean package -Pprod

# Create production JAR
./mvnw spring-boot:repackage -Pprod
```

#### 2. Database Migration
```bash
# Run database migrations
./mvnw flyway:migrate -Pprod
```

#### 3. Deploy to Server
```bash
# Copy JAR to server
scp target/irctc-0.0.1-SNAPSHOT.jar user@server:/opt/irctc/

# Start application
java -jar -Dspring.profiles.active=prod \
  -Xms512m -Xmx2g \
  irctc-0.0.1-SNAPSHOT.jar
```

#### 4. Nginx Configuration
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        proxy_pass http://localhost:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Docker Deployment

#### 1. Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app
COPY target/irctc-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. Docker Compose
```yaml
version: '3.8'
services:
  irctc-backend:
    build: .
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PASSWORD=postgres123
    depends_on:
      - postgres
      - kafka

  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: irctc
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres123
    volumes:
      - postgres_data:/var/lib/postgresql/data

  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

volumes:
  postgres_data:
```

### Kubernetes Deployment

#### 1. Deployment YAML
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: irctc-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: irctc-backend
  template:
    metadata:
      labels:
        app: irctc-backend
    spec:
      containers:
      - name: irctc-backend
        image: irctc-backend:latest
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          value: "postgres-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

#### 2. Service YAML
```yaml
apiVersion: v1
kind: Service
metadata:
  name: irctc-backend-service
spec:
  selector:
    app: irctc-backend
  ports:
  - port: 80
    targetPort: 8082
  type: LoadBalancer
```

## 📊 Monitoring & Logging

### Application Metrics
- **Micrometer**: Built-in metrics collection
- **Prometheus**: Metrics endpoint at `/actuator/prometheus`
- **Grafana**: Dashboard for visualization

### Health Checks
- **Health Endpoint**: `/actuator/health`
- **Info Endpoint**: `/actuator/info`
- **Metrics Endpoint**: `/actuator/metrics`

### Logging Configuration
```xml
<!-- log4j2.xml -->
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <RollingFile name="FileAppender" fileName="logs/irctc-application.log"
                     filePattern="logs/irctc-application-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
                <SizeBasedTriggeringPolicy size="10MB"/>
            </Policies>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Logger name="com.irctc_backend.irctc" level="INFO"/>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="FileAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

## 🔒 Security

### Authentication & Authorization
- **JWT Bearer Tokens**: Stateless authentication
- **Role-based Access Control**: USER, ADMIN, SUPER_ADMIN
- **Password Encryption**: BCrypt hashing
- **CORS Configuration**: Configurable cross-origin policies

### API Security
- **Input Validation**: Bean validation with Jakarta Validation
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Protection**: Content Security Policy headers
- **Rate Limiting**: Configurable request rate limits

### Data Protection
- **Sensitive Data Encryption**: PII encryption at rest
- **Audit Logging**: Comprehensive audit trails
- **Data Anonymization**: GDPR compliance features

## 🤝 Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Standards
- **Java Code Style**: Google Java Style Guide
- **Documentation**: Javadoc for public APIs
- **Testing**: Minimum 80% code coverage
- **Commits**: Conventional commit messages

### Pull Request Guidelines
- Clear description of changes
- Reference to related issues
- Updated documentation
- Passing tests
- Code review approval

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

### Documentation
- **API Documentation**: Available at `/swagger-ui.html`
- **User Guide**: See `docs/user-guide.md`
- **Developer Guide**: See `docs/developer-guide.md`

### Contact
- **Email**: support@irctc-backend.com
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions

### Community
- **Slack**: #irctc-backend
- **Discord**: IRCTC Backend Community
- **Stack Overflow**: Tag `irctc-backend`

---

## 🎯 Roadmap

### Upcoming Features
- [ ] Real-time train tracking
- [ ] Mobile app integration
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] AI-powered recommendations
- [ ] Blockchain-based ticketing
- [ ] IoT integration for train monitoring

### Performance Improvements
- [ ] Redis caching layer
- [ ] Database query optimization
- [ ] Microservices architecture
- [ ] Event-driven architecture
- [ ] GraphQL API support

---

**Built with ❤️ by the IRCTC Development Team**