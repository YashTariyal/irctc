# IRCTC Backend Database Schema

## Overview
This document describes the complete database structure for the IRCTC (Indian Railway Catering and Tourism Corporation) backend application. The schema is designed to handle all aspects of railway ticket booking, user management, train operations, and payment processing.

## Database Tables

### 1. Users Table
**Purpose**: Store user account information and authentication details

**Key Fields**:
- `id`: Primary key
- `username`: Unique username for login
- `email`: Unique email address
- `password`: Encrypted password
- `first_name`, `last_name`: User's full name
- `phone_number`: Contact number
- `date_of_birth`: Birth date
- `gender`: MALE, FEMALE, OTHER
- `id_proof_type`: AADHAR, PAN, PASSPORT, etc.
- `id_proof_number`: Government ID number
- `is_verified`: Email/phone verification status
- `is_active`: Account status
- `role`: USER, ADMIN, SUPER_ADMIN

**Relationships**:
- One-to-Many with Bookings
- One-to-Many with Passengers

### 2. Stations Table
**Purpose**: Store railway station information

**Key Fields**:
- `id`: Primary key
- `station_code`: Unique station code (e.g., NDLS, BCT)
- `station_name`: Full station name
- `station_type`: JUNCTION, TERMINAL, HALT, STATION
- `city`, `state`, `zone`: Location information
- `latitude`, `longitude`: GPS coordinates
- `platform_count`: Number of platforms
- `is_active`: Station status

**Relationships**:
- One-to-Many with Trains (as source station)
- One-to-Many with Trains (as destination station)

### 3. Trains Table
**Purpose**: Store train information and route details

**Key Fields**:
- `id`: Primary key
- `train_number`: Unique train number (e.g., 12345)
- `train_name`: Train name (e.g., Rajdhani Express)
- `source_station_id`: Starting station
- `destination_station_id`: Ending station
- `departure_time`, `arrival_time`: Schedule times
- `journey_duration`: Duration in minutes
- `total_distance`: Distance in kilometers
- `train_type`: EXPRESS, PASSENGER, SUPERFAST, etc.
- `status`: ACTIVE, INACTIVE, MAINTENANCE, CANCELLED
- `is_running`: Current running status

**Relationships**:
- Many-to-One with Stations (source and destination)
- One-to-Many with TrainSchedules
- One-to-Many with Coaches
- One-to-Many with Bookings

### 4. TrainSchedules Table
**Purpose**: Store train running schedules and days

**Key Fields**:
- `id`: Primary key
- `train_id`: Associated train
- `running_days`: Days of the week train runs
- `effective_from`: Schedule start date
- `effective_until`: Schedule end date
- `is_active`: Schedule status

**Relationships**:
- Many-to-One with Trains

### 5. Coaches Table
**Purpose**: Store coach information and capacity

**Key Fields**:
- `id`: Primary key
- `train_id`: Associated train
- `coach_number`: Coach identifier (e.g., A1, B2)
- `coach_type`: AC_FIRST_CLASS, AC_2_TIER, SLEEPER_CLASS, etc.
- `total_seats`, `available_seats`: Capacity management
- `base_fare`, `ac_fare`, `sleeper_fare`: Different fare types
- `tatkal_fare`: Emergency booking fare
- `ladies_quota`, `senior_citizen_quota`: Special quotas
- `is_active`: Coach status

**Relationships**:
- Many-to-One with Trains
- One-to-Many with Seats
- One-to-Many with Bookings

### 6. Seats Table
**Purpose**: Store individual seat information

**Key Fields**:
- `id`: Primary key
- `coach_id`: Associated coach
- `seat_number`: Seat identifier
- `berth_number`: Berth identifier
- `seat_type`: WINDOW, AISLE, MIDDLE, etc.
- `berth_type`: LOWER, MIDDLE, UPPER, etc.
- `status`: AVAILABLE, BOOKED, RESERVED, etc.
- `is_ladies_quota`, `is_senior_citizen_quota`: Special quotas
- `is_handicapped_friendly`: Accessibility feature

**Relationships**:
- Many-to-One with Coaches
- One-to-Many with Bookings

### 7. Passengers Table
**Purpose**: Store passenger information for bookings

**Key Fields**:
- `id`: Primary key
- `user_id`: Associated user account
- `first_name`, `last_name`: Passenger name
- `age`: Passenger age
- `gender`: MALE, FEMALE, OTHER
- `passenger_type`: ADULT, CHILD, INFANT, SENIOR_CITIZEN
- `id_proof_type`, `id_proof_number`: Identity verification
- `is_senior_citizen`, `is_ladies_quota`, `is_handicapped`: Special categories

**Relationships**:
- Many-to-One with Users
- One-to-Many with Bookings

### 8. Bookings Table
**Purpose**: Store ticket booking information

**Key Fields**:
- `id`: Primary key
- `pnr_number`: Unique PNR (Passenger Name Record)
- `user_id`: Booking user
- `train_id`: Selected train
- `passenger_id`: Passenger details
- `seat_id`, `coach_id`: Seat and coach assignment
- `journey_date`: Travel date
- `booking_date`: Booking timestamp
- `total_fare`, `base_fare`, `tatkal_fare`: Fare breakdown
- `convenience_fee`, `gst_amount`: Additional charges
- `status`: CONFIRMED, WAITLIST, RAC, CANCELLED, COMPLETED
- `payment_status`: PENDING, COMPLETED, FAILED, etc.
- `quota_type`: GENERAL, LADIES, SENIOR_CITIZEN, etc.
- `is_tatkal`: Emergency booking flag
- `is_cancelled`: Cancellation status
- `refund_amount`: Refund details
- `booking_source`: WEB, MOBILE_APP, COUNTER, etc.

**Relationships**:
- Many-to-One with Users, Trains, Passengers, Seats, Coaches
- One-to-Many with Payments

### 9. Payments Table
**Purpose**: Store payment transaction information

**Key Fields**:
- `id`: Primary key
- `booking_id`: Associated booking
- `transaction_id`: Unique transaction identifier
- `amount`: Payment amount
- `payment_method`: CREDIT_CARD, DEBIT_CARD, UPI, etc.
- `status`: PENDING, COMPLETED, FAILED, REFUNDED, etc.
- `gateway_response`: Payment gateway response
- `gateway_transaction_id`: Gateway transaction ID
- `payment_date`: Payment timestamp
- `failure_reason`: Payment failure details
- `refund_amount`, `refund_date`, `refund_reason`: Refund information

**Relationships**:
- Many-to-One with Bookings

## Database Relationships Summary

```
Users (1) ←→ (N) Bookings
Users (1) ←→ (N) Passengers
Stations (1) ←→ (N) Trains (as source)
Stations (1) ←→ (N) Trains (as destination)
Trains (1) ←→ (N) TrainSchedules
Trains (1) ←→ (N) Coaches
Trains (1) ←→ (N) Bookings
Coaches (1) ←→ (N) Seats
Coaches (1) ←→ (N) Bookings
Seats (1) ←→ (N) Bookings
Passengers (1) ←→ (N) Bookings
Bookings (1) ←→ (N) Payments
```

## Key Features Supported

1. **User Management**: Registration, authentication, profile management
2. **Train Operations**: Train schedules, routes, coach management
3. **Seat Management**: Individual seat tracking and availability
4. **Booking System**: Ticket booking with PNR generation
5. **Quota Management**: Ladies, senior citizen, handicapped quotas
6. **Payment Processing**: Multiple payment methods and transaction tracking
7. **Cancellation & Refunds**: Booking cancellation and refund processing
8. **Special Categories**: Tatkal booking, different passenger types
9. **Audit Trail**: Creation and update timestamps for all entities

## Indexes and Performance Considerations

**Recommended Indexes**:
- `users(username)`, `users(email)`, `users(phone_number)`
- `trains(train_number)`, `trains(source_station_id)`, `trains(destination_station_id)`
- `stations(station_code)`, `stations(city)`, `stations(state)`
- `bookings(pnr_number)`, `bookings(user_id)`, `bookings(journey_date)`
- `payments(transaction_id)`, `payments(booking_id)`
- `seats(coach_id, status)`, `seats(seat_number)`

This schema provides a robust foundation for a comprehensive railway reservation system with support for all major IRCTC features. 