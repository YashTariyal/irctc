# 🗄️ IRCTC Database Schema Documentation

This document provides a comprehensive overview of the database schema for the IRCTC Backend System, including entity relationships, constraints, and data flow.

## 📊 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              IRCTC Database Schema                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │    Users    │    │   Trains    │    │  Stations   │    │   Coaches   │     │
│  │             │    │             │    │             │    │             │     │
│  │ id (PK)     │    │ id (PK)     │    │ id (PK)     │    │ id (PK)     │     │
│  │ username    │    │ trainNumber │    │ stationCode │    │ coachNumber │     │
│  │ email       │    │ trainName   │    │ stationName │    │ coachType   │     │
│  │ password    │    │ sourceId(FK)│    │ city        │    │ trainId(FK) │     │
│  │ firstName   │    │ destId(FK)  │    │ state       │    │ totalSeats  │     │
│  │ lastName    │    │ departure   │    │ pincode     │    │ isActive    │     │
│  │ phoneNumber │    │ arrival     │    │ isActive    │    │             │     │
│  │ role        │    │ distance    │    │             │    │             │     │
│  │ isActive    │    │ trainType   │    │             │    │             │     │
│  │ createdAt   │    │ status      │    │             │    │             │     │
│  │ updatedAt   │    │ createdAt   │    │             │    │             │     │
│  │             │    │ updatedAt   │    │             │    │             │     │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘     │
│         │                    │                │                    │           │
│         │                    │                │                    │           │
│         │ 1:N                │ 1:N            │ 1:N                │ 1:N       │
│         │                    │                │                    │           │
│         ▼                    ▼                ▼                    ▼           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │  Bookings   │    │   Seats     │    │ TrainSched  │    │ Passengers  │     │
│  │             │    │             │    │             │    │             │     │
│  │ id (PK)     │    │ id (PK)     │    │ id (PK)     │    │ id (PK)     │     │
│  │ pnrNumber   │    │ seatNumber  │    │ trainId(FK) │    │ firstName   │     │
│  │ userId(FK)  │    │ seatType    │    │ stationId   │    │ lastName    │     │
│  │ trainId(FK) │    │ berthType   │    │ arrivalTime │    │ age         │     │
│  │ passengerId │    │ coachId(FK) │    │ departure   │    │ gender      │     │
│  │ seatId(FK)  │    │ isAvailable │    │ haltTime    │    │ userId(FK)  │     │
│  │ coachId(FK) │    │ isLadiesQ   │    │ dayNumber   │    │ passengerType│    │
│  │ journeyDate │    │ isSeniorQ   │    │ distance    │    │ idProofType │     │
│  │ totalFare   │    │ isHandicap  │    │             │    │ idProofNum  │     │
│  │ status      │    │ createdAt   │    │             │    │ isSenior    │     │
│  │ paymentStat │    │ updatedAt   │    │             │    │ isLadies    │     │
│  │ isTatkal    │    │             │    │             │    │ isHandicap  │     │
│  │ createdAt   │    │             │    │             │    │ createdAt   │     │
│  │ updatedAt   │    │             │    │             │    │ updatedAt   │     │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘     │
│         │                    │                │                    │           │
│         │                    │                │                    │           │
│         │ 1:N                │ 1:N            │ 1:N                │ 1:N       │
│         │                    │                │                    │           │
│         ▼                    ▼                ▼                    ▼           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │  Payments   │    │ Waitlist    │    │   RAC       │    │ LoyaltyAcc  │     │
│  │             │    │             │    │             │    │             │     │
│  │ id (PK)     │    │ id (PK)     │    │ id (PK)     │    │ id (PK)     │     │
│  │ bookingId   │    │ bookingId   │    │ bookingId   │    │ userId(FK)  │     │
│  │ amount      │    │ userId(FK)  │    │ userId(FK)  │    │ loyaltyNum  │     │
│  │ method      │    │ trainId(FK) │    │ trainId(FK) │    │ tier        │     │
│  │ currency    │    │ coachType   │    │ coachId(FK) │    │ totalPoints │     │
│  │ status      │    │ journeyDate │    │ seatId(FK)  │    │ availPoints │     │
│  │ razorpayId  │    │ waitlistNum │    │ journeyDate │    │ redeemedPts │     │
│  │ paymentId   │    │ status      │    │ racNumber   │    │ totalSpent  │     │
│  │ signature   │    │ createdAt   │    │ status      │    │ totalBooks  │     │
│  │ createdAt   │    │ updatedAt   │    │ createdAt   │    │ isActive    │     │
│  │ updatedAt   │    │             │    │ updatedAt   │    │ joinedDate  │     │
│  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘     │
│                                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ FareRules   │    │ Rewards     │    │ RewardRed   │    │ TravelIns   │     │
│  │             │    │             │    │             │    │             │     │
│  │ id (PK)     │    │ id (PK)     │    │ id (PK)     │    │ id (PK)     │     │
│  │ trainId(FK) │    │ name        │    │ loyaltyId   │    │ userId(FK)  │     │
│  │ coachType   │    │ description │    │ rewardId(FK)│    │ bookingId   │     │
│  │ baseFare    │    │ category    │    │ redemption  │    │ providerId  │     │
│  │ distance    │    │ pointsReq   │    │ pointsUsed  │    │ planId(FK)  │     │
│  │ tatkalFare  │    │ cashValue   │    │ rewardValue │    │ policyNum   │     │
│  │ premiumTat  │    │ discount%   │    │ status      │    │ coverageAmt │     │
│  │ ladiesDisc  │    │ maxDiscount │    │ redemption  │    │ premiumAmt  │     │
│  │ seniorDisc  │    │ validityDays│    │ Date        │    │ gstAmount   │     │
│  │ handicapDisc│    │ minTier     │    │ expiryDate  │    │ totalAmount │     │
│  │ surgeMult   │    │ isActive    │    │ createdAt   │    │ startDate   │     │
│  │ peakMult    │    │ isFeatured  │    │ updatedAt   │    │ endDate     │     │
│  │ weekendMult │    │ redemption  │    │             │    │ status      │     │
│  │ festivalMult│    │ Limit       │    │             │    │ travelerName│     │
│  │ isActive    │    │ redemption  │    │             │    │ travelerAge │     │
│  │ validFrom   │    │ Count       │    │             │    │ travelerGen │     │
│  │ validUntil  │    │ createdAt   │    │             │    │ travelerPh  │     │
│  │ createdAt   │    │ updatedAt   │    │             │    │ travelerEm  │     │
│  │ updatedAt   │    │             │    │             │    │ hasPreExist │     │
│  └─────────────┘    └─────────────┘    └─────────────┘    │ medCondDet  │     │
│                                                           │ claimStatus │     │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    │ claimDet    │     │
│  │ MealVendors │    │ MealItems   │    │ MealOrders  │    │ createdAt   │     │
│  │             │    │             │    │             │    │ updatedAt   │     │
│  │ id (PK)     │    │ id (PK)     │    │ id (PK)     │    └─────────────┘     │
│  │ vendorName  │    │ vendorId(FK)│    │ userId(FK)  │                        │
│  │ stationCode │    │ itemName    │    │ bookingId   │                        │
│  │ stationName │    │ description │    │ trainId(FK) │                        │
│  │ contactEmail│    │ price       │    │ vendorId(FK)│                        │
│  │ contactPhone│    │ category    │    │ orderNumber │                        │
│  │ rating      │    │ mealType    │    │ orderDate   │                        │
│  │ isActive    │    │ isVegetarian│    │ deliveryDate│                        │
│  │ createdAt   │    │ isAvailable │    │ deliveryTime│                        │
│  │ updatedAt   │    │ prepTime    │    │ deliveryStn │                        │
│  └─────────────┘    │ imageUrl    │    │ deliverySeat│                        │
│         │           │ createdAt   │    │ totalAmount │                        │
│         │ 1:N       │ updatedAt   │    │ paymentStat │                        │
│         │           └─────────────┘    │ orderStatus │                        │
│         │                   │          │ specialInst │                        │
│         │                   │ 1:N      │ createdAt   │                        │
│         │                   │          │ updatedAt   │                        │
│         │                   ▼          └─────────────┘                        │
│         │           ┌─────────────┐              │                             │
│         │           │MealOrderItem│              │ 1:N                         │
│         │           │             │              │                             │
│         │           │ id (PK)     │              ▼                             │
│         │           │ orderId(FK) │    ┌─────────────┐                        │
│         │           │ mealItemId  │    │InsuranceProv│                        │
│         │           │ quantity    │    │             │                        │
│         │           │ pricePerItem│    │ id (PK)     │                        │
│         │           │ subtotal    │    │ providerName│                        │
│         │           │ createdAt   │    │ companyName │                        │
│         │           │ updatedAt   │    │ description │                        │
│         │           └─────────────┘    │ contactEmail│                        │
│         │                             │ contactPhone│                        │
│         │                             │ websiteUrl  │                        │
│         │                             │ logoUrl     │                        │
│         │                             │ basePremium │                        │
│         │                             │ minCoverage │                        │
│         │                             │ maxCoverage │                        │
│         │                             │ claimRatio  │                        │
│         │                             │ avgSettle   │                        │
│         │                             │ isActive    │                        │
│         │                             │ isFeatured  │                        │
│         │                             │ rating      │                        │
│         │                             │ totalPolicies│                       │
│         │                             │ totalClaims │                        │
│         │                             │ createdAt   │                        │
│         │                             │ updatedAt   │                        │
│         │                             └─────────────┘                        │
│         │                                       │                             │
│         │                                       │ 1:N                         │
│         │                                       │                             │
│         │                                       ▼                             │
│         │                             ┌─────────────┐                        │
│         │                             │InsurancePlan│                        │
│         │                             │             │                        │
│         │                             │ id (PK)     │                        │
│         │                             │ providerId  │                        │
│         │                             │ planName    │                        │
│         │                             │ description │                        │
│         │                             │ planType    │                        │
│         │                             │ premiumRate │                        │
│         │                             │ minCoverage │                        │
│         │                             │ maxCoverage │                        │
│         │                             │ minPremium  │                        │
│         │                             │ maxPremium  │                        │
│         │                             │ coverageDur │                        │
│         │                             │ ageMin      │                        │
│         │                             │ ageMax      │                        │
│         │                             │ isActive    │                        │
│         │                             │ isFeatured  │                        │
│         │                             │ popularity  │                        │
│         │                             │ coversMed   │                        │
│         │                             │ coversTrip  │                        │
│         │                             │ coversBag   │                        │
│         │                             │ coversAcc   │                        │
│         │                             │ coversEvac  │                        │
│         │                             │ covers24x7  │                        │
│         │                             │ medCoverage │                        │
│         │                             │ tripCoverage│                        │
│         │                             │ bagCoverage │                        │
│         │                             │ accCoverage │                        │
│         │                             │ deductible  │                        │
│         │                             │ createdAt   │                        │
│         │                             │ updatedAt   │                        │
│         │                             └─────────────┘                        │
│         │                                                                     │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## 📋 Table Definitions

### Core Tables

#### 1. Users Table
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
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    id_proof_type VARCHAR(20) CHECK (id_proof_type IN ('AADHAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'VOTER_ID')),
    id_proof_number VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN', 'SUPER_ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_role ON users(role);
```

#### 2. Stations Table
```sql
CREATE TABLE stations (
    id BIGSERIAL PRIMARY KEY,
    station_code VARCHAR(10) UNIQUE NOT NULL,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_stations_code ON stations(station_code);
CREATE INDEX idx_stations_city ON stations(city);
CREATE INDEX idx_stations_state ON stations(state);
```

#### 3. Trains Table
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
    train_type VARCHAR(20) CHECK (train_type IN ('RAJDHANI', 'SHATABDI', 'EXPRESS', 'MAIL', 'PASSENGER', 'SUPERFAST')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    is_running BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_trains_number ON trains(train_number);
CREATE INDEX idx_trains_source ON trains(source_station_id);
CREATE INDEX idx_trains_destination ON trains(destination_station_id);
CREATE INDEX idx_trains_type ON trains(train_type);
CREATE INDEX idx_trains_status ON trains(status);
```

#### 4. Coaches Table
```sql
CREATE TABLE coaches (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT REFERENCES trains(id),
    coach_number VARCHAR(10) NOT NULL,
    coach_type VARCHAR(20) NOT NULL CHECK (coach_type IN ('AC1', 'AC2', 'AC3', 'CC', 'SL', '3A', '2A', '1A', 'GENERAL')),
    total_seats INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(train_id, coach_number)
);

-- Indexes
CREATE INDEX idx_coaches_train ON coaches(train_id);
CREATE INDEX idx_coaches_type ON coaches(coach_type);
```

#### 5. Seats Table
```sql
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    coach_id BIGINT REFERENCES coaches(id),
    seat_number VARCHAR(10) NOT NULL,
    seat_type VARCHAR(20) NOT NULL CHECK (seat_type IN ('LOWER_BERTH', 'MIDDLE_BERTH', 'UPPER_BERTH', 'SIDE_LOWER', 'SIDE_UPPER', 'WINDOW', 'AISLE')),
    berth_type VARCHAR(20) CHECK (berth_type IN ('LOWER_BERTH', 'MIDDLE_BERTH', 'UPPER_BERTH', 'SIDE_LOWER', 'SIDE_UPPER')),
    is_available BOOLEAN DEFAULT TRUE,
    is_ladies_quota BOOLEAN DEFAULT FALSE,
    is_senior_citizen_quota BOOLEAN DEFAULT FALSE,
    is_handicapped_friendly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coach_id, seat_number)
);

-- Indexes
CREATE INDEX idx_seats_coach ON seats(coach_id);
CREATE INDEX idx_seats_available ON seats(is_available);
CREATE INDEX idx_seats_type ON seats(seat_type);
```

#### 6. Passengers Table
```sql
CREATE TABLE passengers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL CHECK (age > 0 AND age < 120),
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    passenger_type VARCHAR(20) DEFAULT 'ADULT' CHECK (passenger_type IN ('ADULT', 'CHILD', 'INFANT', 'SENIOR_CITIZEN')),
    id_proof_type VARCHAR(20) CHECK (id_proof_type IN ('AADHAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'VOTER_ID')),
    id_proof_number VARCHAR(50),
    is_senior_citizen BOOLEAN DEFAULT FALSE,
    is_ladies_quota BOOLEAN DEFAULT FALSE,
    is_handicapped BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_passengers_user ON passengers(user_id);
CREATE INDEX idx_passengers_age ON passengers(age);
CREATE INDEX idx_passengers_type ON passengers(passenger_type);
```

#### 7. Bookings Table
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
    total_fare DECIMAL(10,2) NOT NULL CHECK (total_fare >= 0),
    base_fare DECIMAL(10,2) CHECK (base_fare >= 0),
    tatkal_fare DECIMAL(10,2) DEFAULT 0 CHECK (tatkal_fare >= 0),
    premium_tatkal_fare DECIMAL(10,2) DEFAULT 0 CHECK (premium_tatkal_fare >= 0),
    quota_type VARCHAR(20) DEFAULT 'GENERAL' CHECK (quota_type IN ('GENERAL', 'TATKAL', 'PREMIUM_TATKAL', 'LADIES', 'SENIOR_CITIZEN', 'HANDICAPPED')),
    booking_status VARCHAR(20) DEFAULT 'CONFIRMED' CHECK (booking_status IN ('CONFIRMED', 'RAC', 'WAITLISTED', 'CANCELLED')),
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    is_tatkal BOOLEAN DEFAULT FALSE,
    is_cancelled BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(255),
    refund_amount DECIMAL(10,2) DEFAULT 0 CHECK (refund_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_bookings_pnr ON bookings(pnr_number);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_train ON bookings(train_id);
CREATE INDEX idx_bookings_journey_date ON bookings(journey_date);
CREATE INDEX idx_bookings_status ON bookings(booking_status);
CREATE INDEX idx_bookings_payment ON bookings(payment_status);
```

#### 8. Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('RAZORPAY', 'UPI', 'NET_BANKING', 'CARD', 'WALLET')),
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(255),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_razorpay_order ON payments(razorpay_order_id);
```

### Advanced Features Tables

#### 9. Fare Rules Table
```sql
CREATE TABLE fare_rules (
    id BIGSERIAL PRIMARY KEY,
    train_id BIGINT REFERENCES trains(id),
    coach_type VARCHAR(20) NOT NULL,
    base_fare DECIMAL(10,2) NOT NULL CHECK (base_fare >= 0),
    distance_km DECIMAL(10,2) NOT NULL CHECK (distance_km > 0),
    tatkal_fare DECIMAL(10,2) DEFAULT 0 CHECK (tatkal_fare >= 0),
    premium_tatkal_fare DECIMAL(10,2) DEFAULT 0 CHECK (premium_tatkal_fare >= 0),
    ladies_quota_discount DECIMAL(5,2) DEFAULT 0 CHECK (ladies_quota_discount >= 0 AND ladies_quota_discount <= 100),
    senior_citizen_discount DECIMAL(5,2) DEFAULT 0 CHECK (senior_citizen_discount >= 0 AND senior_citizen_discount <= 100),
    handicapped_discount DECIMAL(5,2) DEFAULT 0 CHECK (handicapped_discount >= 0 AND handicapped_discount <= 100),
    surge_multiplier DECIMAL(3,2) DEFAULT 1.0 CHECK (surge_multiplier >= 1.0),
    peak_hour_multiplier DECIMAL(3,2) DEFAULT 1.0 CHECK (peak_hour_multiplier >= 1.0),
    weekend_multiplier DECIMAL(3,2) DEFAULT 1.0 CHECK (weekend_multiplier >= 1.0),
    festival_multiplier DECIMAL(3,2) DEFAULT 1.0 CHECK (festival_multiplier >= 1.0),
    is_active BOOLEAN DEFAULT TRUE,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_fare_rules_train ON fare_rules(train_id);
CREATE INDEX idx_fare_rules_coach_type ON fare_rules(coach_type);
CREATE INDEX idx_fare_rules_active ON fare_rules(is_active);
CREATE INDEX idx_fare_rules_validity ON fare_rules(valid_from, valid_until);
```

#### 10. Loyalty Accounts Table
```sql
CREATE TABLE loyalty_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) UNIQUE,
    loyalty_number VARCHAR(20) UNIQUE NOT NULL,
    tier VARCHAR(20) DEFAULT 'BRONZE' CHECK (tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND')),
    total_points DECIMAL(10,2) DEFAULT 0 CHECK (total_points >= 0),
    available_points DECIMAL(10,2) DEFAULT 0 CHECK (available_points >= 0),
    redeemed_points DECIMAL(10,2) DEFAULT 0 CHECK (redeemed_points >= 0),
    expired_points DECIMAL(10,2) DEFAULT 0 CHECK (expired_points >= 0),
    total_spent DECIMAL(12,2) DEFAULT 0 CHECK (total_spent >= 0),
    total_bookings INTEGER DEFAULT 0 CHECK (total_bookings >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    joined_date TIMESTAMP,
    last_activity_date TIMESTAMP,
    points_expiry_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_loyalty_user ON loyalty_accounts(user_id);
CREATE INDEX idx_loyalty_number ON loyalty_accounts(loyalty_number);
CREATE INDEX idx_loyalty_tier ON loyalty_accounts(tier);
CREATE INDEX idx_loyalty_active ON loyalty_accounts(is_active);
```

#### 11. Loyalty Transactions Table
```sql
CREATE TABLE loyalty_transactions (
    id BIGSERIAL PRIMARY KEY,
    loyalty_account_id BIGINT REFERENCES loyalty_accounts(id),
    booking_id BIGINT REFERENCES bookings(id),
    points_amount DECIMAL(10,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('EARNED', 'REDEEMED', 'EXPIRED', 'ADJUSTED')),
    description VARCHAR(255),
    transaction_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_loyalty_trans_account ON loyalty_transactions(loyalty_account_id);
CREATE INDEX idx_loyalty_trans_booking ON loyalty_transactions(booking_id);
CREATE INDEX idx_loyalty_trans_type ON loyalty_transactions(transaction_type);
CREATE INDEX idx_loyalty_trans_date ON loyalty_transactions(transaction_date);
```

#### 12. Rewards Table
```sql
CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL CHECK (category IN ('TRAVEL_VOUCHER', 'CASHBACK', 'UPGRADE', 'MEAL_VOUCHER', 'LOUNGE_ACCESS', 'PRIORITY_BOOKING', 'BONUS_POINTS', 'MERCHANDISE')),
    points_required DECIMAL(10,2) NOT NULL CHECK (points_required > 0),
    cash_value DECIMAL(10,2),
    discount_percentage DECIMAL(5,2),
    max_discount_amount DECIMAL(10,2),
    validity_days INTEGER NOT NULL CHECK (validity_days > 0),
    min_tier_required VARCHAR(20) DEFAULT 'BRONZE' CHECK (min_tier_required IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND')),
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    redemption_limit INTEGER DEFAULT -1 CHECK (redemption_limit = -1 OR redemption_limit > 0),
    redemption_count INTEGER DEFAULT 0 CHECK (redemption_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_rewards_category ON rewards(category);
CREATE INDEX idx_rewards_tier ON rewards(min_tier_required);
CREATE INDEX idx_rewards_active ON rewards(is_active);
CREATE INDEX idx_rewards_featured ON rewards(is_featured);
```

#### 13. Reward Redemptions Table
```sql
CREATE TABLE reward_redemptions (
    id BIGSERIAL PRIMARY KEY,
    loyalty_account_id BIGINT REFERENCES loyalty_accounts(id),
    reward_id BIGINT REFERENCES rewards(id),
    redemption_date TIMESTAMP NOT NULL,
    points_used DECIMAL(10,2) NOT NULL CHECK (points_used > 0),
    reward_value DECIMAL(10,2) NOT NULL CHECK (reward_value > 0),
    status VARCHAR(20) DEFAULT 'REDEEMED' CHECK (status IN ('REDEEMED', 'USED', 'EXPIRED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_reward_red_loyalty ON reward_redemptions(loyalty_account_id);
CREATE INDEX idx_reward_red_reward ON reward_redemptions(reward_id);
CREATE INDEX idx_reward_red_date ON reward_redemptions(redemption_date);
CREATE INDEX idx_reward_red_status ON reward_redemptions(status);
```

#### 14. Insurance Providers Table
```sql
CREATE TABLE insurance_providers (
    id BIGSERIAL PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    description TEXT,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    website_url VARCHAR(255),
    logo_url VARCHAR(255),
    base_premium_rate DECIMAL(5,4) NOT NULL CHECK (base_premium_rate > 0),
    min_coverage_amount DECIMAL(10,2) NOT NULL CHECK (min_coverage_amount > 0),
    max_coverage_amount DECIMAL(10,2) NOT NULL CHECK (max_coverage_amount > 0),
    claim_settlement_ratio DECIMAL(5,2) CHECK (claim_settlement_ratio >= 0 AND claim_settlement_ratio <= 100),
    average_settlement_days INTEGER CHECK (average_settlement_days > 0),
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5),
    total_policies_sold INTEGER DEFAULT 0 CHECK (total_policies_sold >= 0),
    total_claims_processed INTEGER DEFAULT 0 CHECK (total_claims_processed >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_insurance_providers_active ON insurance_providers(is_active);
CREATE INDEX idx_insurance_providers_featured ON insurance_providers(is_featured);
CREATE INDEX idx_insurance_providers_rating ON insurance_providers(rating);
```

#### 15. Insurance Plans Table
```sql
CREATE TABLE insurance_plans (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT REFERENCES insurance_providers(id),
    plan_name VARCHAR(100) NOT NULL,
    description TEXT,
    plan_type VARCHAR(30) NOT NULL CHECK (plan_type IN ('BASIC', 'STANDARD', 'PREMIUM', 'COMPREHENSIVE')),
    premium_rate DECIMAL(5,4) NOT NULL CHECK (premium_rate > 0),
    min_coverage_amount DECIMAL(10,2) NOT NULL CHECK (min_coverage_amount > 0),
    max_coverage_amount DECIMAL(10,2) NOT NULL CHECK (max_coverage_amount > 0),
    min_premium DECIMAL(10,2) CHECK (min_premium > 0),
    max_premium DECIMAL(10,2) CHECK (max_premium > 0),
    coverage_duration_days INTEGER NOT NULL CHECK (coverage_duration_days > 0),
    age_min INTEGER CHECK (age_min > 0),
    age_max INTEGER CHECK (age_max > 0),
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    popularity_score INTEGER DEFAULT 0 CHECK (popularity_score >= 0),
    covers_medical_expenses BOOLEAN DEFAULT FALSE,
    covers_trip_cancellation BOOLEAN DEFAULT FALSE,
    covers_baggage_loss BOOLEAN DEFAULT FALSE,
    covers_personal_accident BOOLEAN DEFAULT FALSE,
    covers_emergency_evacuation BOOLEAN DEFAULT FALSE,
    covers_24x7_support BOOLEAN DEFAULT FALSE,
    medical_coverage_limit DECIMAL(10,2),
    trip_cancellation_limit DECIMAL(10,2),
    baggage_coverage_limit DECIMAL(10,2),
    personal_accident_limit DECIMAL(10,2),
    deductible_amount DECIMAL(10,2) DEFAULT 0 CHECK (deductible_amount >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_insurance_plans_provider ON insurance_plans(provider_id);
CREATE INDEX idx_insurance_plans_type ON insurance_plans(plan_type);
CREATE INDEX idx_insurance_plans_active ON insurance_plans(is_active);
CREATE INDEX idx_insurance_plans_featured ON insurance_plans(is_featured);
```

#### 16. Travel Insurance Table
```sql
CREATE TABLE travel_insurance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    booking_id BIGINT REFERENCES bookings(id),
    provider_id BIGINT REFERENCES insurance_providers(id),
    plan_id BIGINT REFERENCES insurance_plans(id),
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    coverage_amount DECIMAL(10,2) NOT NULL CHECK (coverage_amount > 0),
    premium_amount DECIMAL(10,2) NOT NULL CHECK (premium_amount > 0),
    gst_amount DECIMAL(10,2) NOT NULL CHECK (gst_amount >= 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'CLAIMED')),
    traveler_name VARCHAR(100) NOT NULL,
    traveler_age INTEGER NOT NULL CHECK (traveler_age > 0 AND traveler_age < 120),
    traveler_gender VARCHAR(10) CHECK (traveler_gender IN ('MALE', 'FEMALE', 'OTHER')),
    traveler_phone VARCHAR(15),
    traveler_email VARCHAR(100),
    has_pre_existing_medical_condition BOOLEAN DEFAULT FALSE,
    medical_condition_details TEXT,
    claim_status VARCHAR(20) DEFAULT 'NO_CLAIM' CHECK (claim_status IN ('NO_CLAIM', 'CLAIM_PENDING', 'CLAIM_APPROVED', 'CLAIM_REJECTED')),
    claim_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_travel_insurance_user ON travel_insurance(user_id);
CREATE INDEX idx_travel_insurance_booking ON travel_insurance(booking_id);
CREATE INDEX idx_travel_insurance_provider ON travel_insurance(provider_id);
CREATE INDEX idx_travel_insurance_plan ON travel_insurance(plan_id);
CREATE INDEX idx_travel_insurance_policy ON travel_insurance(policy_number);
CREATE INDEX idx_travel_insurance_status ON travel_insurance(status);
```

#### 17. Meal Vendors Table
```sql
CREATE TABLE meal_vendors (
    id BIGSERIAL PRIMARY KEY,
    vendor_name VARCHAR(100) NOT NULL,
    station_code VARCHAR(10) NOT NULL,
    station_name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_meal_vendors_station ON meal_vendors(station_code);
CREATE INDEX idx_meal_vendors_active ON meal_vendors(is_active);
CREATE INDEX idx_meal_vendors_rating ON meal_vendors(rating);
```

#### 18. Meal Items Table
```sql
CREATE TABLE meal_items (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT REFERENCES meal_vendors(id),
    item_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    category VARCHAR(30) NOT NULL CHECK (category IN ('STARTER', 'MAIN_COURSE', 'DESSERT', 'BEVERAGE', 'SNACKS', 'COMBO')),
    meal_type VARCHAR(20) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS')),
    is_vegetarian BOOLEAN DEFAULT TRUE,
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time_minutes INTEGER CHECK (preparation_time_minutes > 0),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_meal_items_vendor ON meal_items(vendor_id);
CREATE INDEX idx_meal_items_category ON meal_items(category);
CREATE INDEX idx_meal_items_meal_type ON meal_items(meal_type);
CREATE INDEX idx_meal_items_vegetarian ON meal_items(is_vegetarian);
CREATE INDEX idx_meal_items_available ON meal_items(is_available);
```

#### 19. Meal Orders Table
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
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    order_status VARCHAR(20) DEFAULT 'PLACED' CHECK (order_status IN ('PLACED', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED')),
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_meal_orders_user ON meal_orders(user_id);
CREATE INDEX idx_meal_orders_booking ON meal_orders(booking_id);
CREATE INDEX idx_meal_orders_train ON meal_orders(train_id);
CREATE INDEX idx_meal_orders_vendor ON meal_orders(vendor_id);
CREATE INDEX idx_meal_orders_number ON meal_orders(order_number);
CREATE INDEX idx_meal_orders_delivery ON meal_orders(delivery_date, delivery_station_code);
CREATE INDEX idx_meal_orders_status ON meal_orders(order_status);
```

#### 20. Meal Order Items Table
```sql
CREATE TABLE meal_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES meal_orders(id),
    meal_item_id BIGINT REFERENCES meal_items(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_per_item DECIMAL(10,2) NOT NULL CHECK (price_per_item > 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_meal_order_items_order ON meal_order_items(order_id);
CREATE INDEX idx_meal_order_items_meal ON meal_order_items(meal_item_id);
```

#### 21. Waitlist Entries Table
```sql
CREATE TABLE waitlist_entries (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    user_id BIGINT REFERENCES users(id),
    train_id BIGINT REFERENCES trains(id),
    coach_type VARCHAR(20) NOT NULL,
    journey_date DATE NOT NULL,
    waitlist_number INTEGER NOT NULL CHECK (waitlist_number > 0),
    status VARCHAR(20) DEFAULT 'WAITLISTED' CHECK (status IN ('WAITLISTED', 'RAC', 'CONFIRMED', 'CANCELLED')),
    requested_seat_type VARCHAR(20),
    requested_berth_type VARCHAR(20),
    is_ladies_quota BOOLEAN DEFAULT FALSE,
    is_senior_citizen_quota BOOLEAN DEFAULT FALSE,
    is_handicapped_friendly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_waitlist_booking ON waitlist_entries(booking_id);
CREATE INDEX idx_waitlist_user ON waitlist_entries(user_id);
CREATE INDEX idx_waitlist_train ON waitlist_entries(train_id);
CREATE INDEX idx_waitlist_journey ON waitlist_entries(journey_date);
CREATE INDEX idx_waitlist_number ON waitlist_entries(waitlist_number);
CREATE INDEX idx_waitlist_status ON waitlist_entries(status);
```

#### 22. RAC Entries Table
```sql
CREATE TABLE rac_entries (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    user_id BIGINT REFERENCES users(id),
    train_id BIGINT REFERENCES trains(id),
    coach_id BIGINT REFERENCES coaches(id),
    seat_id BIGINT REFERENCES seats(id),
    journey_date DATE NOT NULL,
    rac_number INTEGER NOT NULL CHECK (rac_number > 0),
    status VARCHAR(20) DEFAULT 'RAC' CHECK (status IN ('RAC', 'CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_rac_booking ON rac_entries(booking_id);
CREATE INDEX idx_rac_user ON rac_entries(user_id);
CREATE INDEX idx_rac_train ON rac_entries(train_id);
CREATE INDEX idx_rac_coach ON rac_entries(coach_id);
CREATE INDEX idx_rac_journey ON rac_entries(journey_date);
CREATE INDEX idx_rac_number ON rac_entries(rac_number);
CREATE INDEX idx_rac_status ON rac_entries(status);
```

## 🔗 Foreign Key Relationships

### Primary Relationships
1. **Users → Bookings** (1:N)
   - `bookings.user_id` → `users.id`

2. **Trains → Coaches** (1:N)
   - `coaches.train_id` → `trains.id`

3. **Coaches → Seats** (1:N)
   - `seats.coach_id` → `coaches.id`

4. **Bookings → Payments** (1:N)
   - `payments.booking_id` → `bookings.id`

5. **Users → Passengers** (1:N)
   - `passengers.user_id` → `users.id`

6. **Bookings → Waitlist Entries** (1:1)
   - `waitlist_entries.booking_id` → `bookings.id`

7. **Bookings → RAC Entries** (1:1)
   - `rac_entries.booking_id` → `bookings.id`

### Advanced Feature Relationships
1. **Users → Loyalty Accounts** (1:1)
   - `loyalty_accounts.user_id` → `users.id`

2. **Loyalty Accounts → Loyalty Transactions** (1:N)
   - `loyalty_transactions.loyalty_account_id` → `loyalty_accounts.id`

3. **Rewards → Reward Redemptions** (1:N)
   - `reward_redemptions.reward_id` → `rewards.id`

4. **Insurance Providers → Insurance Plans** (1:N)
   - `insurance_plans.provider_id` → `insurance_providers.id`

5. **Bookings → Travel Insurance** (1:N)
   - `travel_insurance.booking_id` → `bookings.id`

6. **Meal Vendors → Meal Items** (1:N)
   - `meal_items.vendor_id` → `meal_vendors.id`

7. **Meal Orders → Meal Order Items** (1:N)
   - `meal_order_items.order_id` → `meal_orders.id`

## 📊 Data Flow Diagrams

### Booking Flow
```
User Registration → Train Search → Seat Selection → Booking Creation → Payment → Confirmation
       ↓                ↓              ↓              ↓              ↓           ↓
   users table    trains table   seats table   bookings table  payments table  notifications
```

### Loyalty Flow
```
Booking Completion → Points Calculation → Loyalty Account Update → Tier Check → Reward Eligibility
       ↓                    ↓                    ↓                  ↓              ↓
  bookings table    loyalty_transactions  loyalty_accounts    tier_upgrade   rewards table
```

### Insurance Flow
```
Booking Creation → Insurance Quote → Plan Selection → Policy Purchase → Coverage Activation
       ↓                ↓               ↓              ↓                  ↓
  bookings table  insurance_plans  insurance_providers  travel_insurance  policy_activation
```

### Meal Booking Flow
```
Train Booking → Station Selection → Vendor Selection → Menu Selection → Order Placement → Delivery
       ↓              ↓                ↓               ↓              ↓              ↓
  bookings table  stations table  meal_vendors  meal_items  meal_orders  meal_order_items
```

## 🔍 Query Optimization

### Common Queries and Indexes

#### 1. Train Search by Route and Date
```sql
-- Query
SELECT t.*, s1.station_name as source_name, s2.station_name as dest_name
FROM trains t
JOIN stations s1 ON t.source_station_id = s1.id
JOIN stations s2 ON t.destination_station_id = s2.id
WHERE s1.station_code = 'NDLS' 
  AND s2.station_code = 'MUMB'
  AND t.is_running = true;

-- Indexes
CREATE INDEX idx_trains_route ON trains(source_station_id, destination_station_id, is_running);
```

#### 2. Available Seats Query
```sql
-- Query
SELECT s.*, c.coach_type, c.coach_number
FROM seats s
JOIN coaches c ON s.coach_id = c.id
JOIN trains t ON c.train_id = t.id
WHERE t.id = ? 
  AND s.is_available = true
  AND s.id NOT IN (
    SELECT seat_id FROM bookings 
    WHERE journey_date = ? AND booking_status = 'CONFIRMED'
  );

-- Indexes
CREATE INDEX idx_seats_availability ON seats(coach_id, is_available);
CREATE INDEX idx_bookings_journey_seat ON bookings(journey_date, seat_id, booking_status);
```

#### 3. User Booking History
```sql
-- Query
SELECT b.*, t.train_name, t.train_number, p.first_name, p.last_name
FROM bookings b
JOIN trains t ON b.train_id = t.id
JOIN passengers p ON b.passenger_id = p.id
WHERE b.user_id = ?
ORDER BY b.booking_date DESC
LIMIT ? OFFSET ?;

-- Indexes
CREATE INDEX idx_bookings_user_date ON bookings(user_id, booking_date DESC);
```

#### 4. Loyalty Points Calculation
```sql
-- Query
SELECT la.*, 
       (SELECT SUM(lt.points_amount) 
        FROM loyalty_transactions lt 
        WHERE lt.loyalty_account_id = la.id 
          AND lt.transaction_type = 'EARNED'
          AND lt.transaction_date >= ?) as recent_earnings
FROM loyalty_accounts la
WHERE la.user_id = ?;

-- Indexes
CREATE INDEX idx_loyalty_trans_account_type_date ON loyalty_transactions(loyalty_account_id, transaction_type, transaction_date);
```

## 🛡️ Data Integrity Constraints

### Check Constraints
1. **Age Validation**: All age fields must be between 0 and 120
2. **Percentage Validation**: All percentage fields must be between 0 and 100
3. **Amount Validation**: All monetary amounts must be non-negative
4. **Rating Validation**: All ratings must be between 0 and 5
5. **Date Validation**: End dates must be after start dates

### Unique Constraints
1. **Username**: Must be unique across all users
2. **Email**: Must be unique across all users
3. **Phone Number**: Must be unique across all users
4. **PNR Number**: Must be unique across all bookings
5. **Train Number**: Must be unique across all trains
6. **Station Code**: Must be unique across all stations

### Referential Integrity
1. **Cascade Deletes**: When a user is deleted, their bookings are also deleted
2. **Restrict Deletes**: Cannot delete a train if it has active bookings
3. **Set Null**: When a seat is deleted, booking seat_id is set to null
4. **Cascade Updates**: When a user ID changes, all related records are updated

## 📈 Performance Considerations

### Partitioning Strategy
1. **Bookings Table**: Partition by journey_date (monthly partitions)
2. **Loyalty Transactions**: Partition by transaction_date (monthly partitions)
3. **Payments**: Partition by created_at (monthly partitions)

### Archiving Strategy
1. **Old Bookings**: Archive bookings older than 2 years
2. **Completed Payments**: Archive payments older than 1 year
3. **Expired Loyalty Points**: Archive expired transactions

### Caching Strategy
1. **Station Data**: Cache frequently accessed station information
2. **Train Schedules**: Cache train schedules for popular routes
3. **Fare Rules**: Cache fare calculation rules
4. **User Sessions**: Cache active user sessions

---

This comprehensive database schema documentation provides a complete overview of the IRCTC Backend System's data structure, relationships, and optimization strategies. The schema is designed to handle high-volume transactions while maintaining data integrity and performance.