# 📋 IRCTC API Payload Examples

This document provides comprehensive examples of all API request and response payloads for the IRCTC Backend System.

## 🔐 Authentication APIs

### 1. User Registration
**Endpoint:** `POST /api/users/register`

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1990-01-15T00:00:00",
  "gender": "MALE",
  "address": "123 Main Street, City, State 12345"
}
```

**Response (201 Created):**
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

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA1MzI0NjAwLCJleHAiOjE3MDU0MTA2MDB9.example",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzA1MzI0NjAwLCJleHAiOjE3MDU4Mjg2MDB9.example",
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

## 🚂 Train Management APIs

### 3. Get All Trains
**Endpoint:** `GET /api/trains`

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
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
    "isRunning": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

### 4. Search Trains
**Endpoint:** `POST /api/trains/search`

**Request:**
```json
{
  "sourceStationCode": "NDLS",
  "destinationStationCode": "MUMB",
  "journeyDate": "2024-01-20",
  "preferredClass": "AC2",
  "numberOfPassengers": 2
}
```

**Response (200 OK):**
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

## 🎫 Booking APIs

### 5. Create Booking
**Endpoint:** `POST /api/bookings`

**Request:**
```json
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

**Response (201 Created):**
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

### 6. Get Booking by PNR
**Endpoint:** `GET /api/bookings/pnr/{pnrNumber}`

**Response (200 OK):**
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

## 🪑 Seat Selection APIs

### 7. Get Available Seats
**Endpoint:** `GET /api/seat-selection/trains/{trainId}/coaches/{coachId}/seats`

**Query Parameters:**
- `journeyDate`: 2024-01-20

**Response (200 OK):**
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
    },
    {
      "id": 2,
      "seatNumber": "MB-2",
      "seatType": "MIDDLE_BERTH",
      "berthType": "MIDDLE_BERTH",
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

### 8. Select Seats
**Endpoint:** `POST /api/seat-selection/select`

**Request:**
```json
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

**Response (200 OK):**
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

## 💳 Payment APIs

### 9. Create Payment
**Endpoint:** `POST /api/payments/create`

**Request:**
```json
{
  "bookingId": 1,
  "amount": 2500.00,
  "paymentMethod": "RAZORPAY",
  "currency": "INR"
}
```

**Response (200 OK):**
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

### 10. Verify Payment
**Endpoint:** `POST /api/payments/verify`

**Request:**
```json
{
  "razorpayOrderId": "order_1234567890",
  "razorpayPaymentId": "pay_1234567890",
  "razorpaySignature": "signature_1234567890"
}
```

**Response (200 OK):**
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

## ⏳ Waitlist & RAC APIs

### 11. Add to Waitlist
**Endpoint:** `POST /api/waitlist-rac/add`

**Request:**
```json
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

**Response (200 OK):**
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

### 12. Get Waitlist Status
**Endpoint:** `GET /api/waitlist-rac/{waitlistId}/status`

**Response (200 OK):**
```json
{
  "waitlistId": 1,
  "pnrNumber": "WL123456",
  "trainNumber": "12951",
  "trainName": "Mumbai Rajdhani",
  "waitlistNumber": 5,
  "status": "WAITLISTED",
  "currentPosition": 3,
  "chancesOfConfirmation": "HIGH",
  "message": "Your waitlist position has improved"
}
```

## 💰 Fare Calculation APIs

### 13. Calculate Fare
**Endpoint:** `POST /api/fare-calculation/calculate`

**Request:**
```json
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

**Response (200 OK):**
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
    },
    {
      "passengerNumber": 2,
      "baseFare": 2000.00,
      "discounts": 0.00,
      "surcharges": 0.00,
      "gst": 360.00,
      "totalFare": 2360.00
    }
  ]
}
```

### 14. Get Available Discounts
**Endpoint:** `GET /api/fare-calculation/discounts`

**Response (200 OK):**
```json
{
  "availableDiscounts": [
    {
      "discountType": "LADIES_QUOTA",
      "discountPercentage": 25.0,
      "description": "25% discount for female passengers",
      "isActive": true
    },
    {
      "discountType": "SENIOR_CITIZEN",
      "discountPercentage": 40.0,
      "description": "40% discount for senior citizens (60+ years)",
      "isActive": true
    },
    {
      "discountType": "HANDICAPPED",
      "discountPercentage": 50.0,
      "description": "50% discount for handicapped passengers",
      "isActive": true
    }
  ]
}
```

## 🏆 Loyalty System APIs

### 15. Get Loyalty Account
**Endpoint:** `GET /api/loyalty/account`

**Query Parameters:**
- `userId`: 1

**Response (200 OK):**
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

### 16. Get Available Rewards
**Endpoint:** `GET /api/loyalty/rewards`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Travel Voucher",
    "description": "Get 500 INR off on your next booking",
    "category": "TRAVEL_VOUCHER",
    "pointsRequired": 1000.00,
    "cashValue": 500.00,
    "discountPercentage": null,
    "maxDiscountAmount": null,
    "validityDays": 90,
    "minTierRequired": "BRONZE",
    "isActive": true,
    "isFeatured": true,
    "redemptionLimit": 5,
    "redemptionCount": 2
  },
  {
    "id": 2,
    "name": "Cashback Reward",
    "description": "Get 10% cashback on your booking",
    "category": "CASHBACK",
    "pointsRequired": 2000.00,
    "cashValue": null,
    "discountPercentage": 10.0,
    "maxDiscountAmount": 1000.00,
    "validityDays": 60,
    "minTierRequired": "SILVER",
    "isActive": true,
    "isFeatured": false,
    "redemptionLimit": 3,
    "redemptionCount": 1
  }
]
```

### 17. Redeem Reward
**Endpoint:** `POST /api/loyalty/rewards/redeem`

**Request:**
```json
{
  "rewardId": 1,
  "userId": 1
}
```

**Response (200 OK):**
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

## 🛡️ Travel Insurance APIs

### 18. Get Insurance Providers
**Endpoint:** `GET /api/travel-insurance/providers`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "providerName": "TravelGuard India",
    "companyName": "TravelGuard India Pvt Ltd",
    "description": "Leading travel insurance provider in India",
    "contactEmail": "support@travelguard.in",
    "contactPhone": "1800-123-4567",
    "websiteUrl": "https://www.travelguard.in",
    "logoUrl": "https://example.com/travelguard-logo.png",
    "basePremiumRate": 0.5,
    "minCoverageAmount": 50000.00,
    "maxCoverageAmount": 1000000.00,
    "claimSettlementRatio": 95.5,
    "averageSettlementDays": 7,
    "isActive": true,
    "isFeatured": true,
    "rating": 4.5,
    "totalPoliciesSold": 15000,
    "totalClaimsProcessed": 1200
  }
]
```

### 19. Get Insurance Quote
**Endpoint:** `POST /api/travel-insurance/quote`

**Request:**
```json
{
  "planId": 1,
  "coverageAmount": 100000.00,
  "travelerAge": 30,
  "hasPreExistingMedicalCondition": false,
  "journeyStartDate": "2024-01-20",
  "journeyEndDate": "2024-01-25"
}
```

**Response (200 OK):**
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

### 20. Purchase Insurance
**Endpoint:** `POST /api/travel-insurance/purchase`

**Request:**
```json
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

**Response (200 OK):**
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

## 🍽️ Meal Booking APIs

### 21. Get Meal Vendors
**Endpoint:** `GET /api/meal-booking/vendors`

**Response (200 OK):**
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
  },
  {
    "id": 2,
    "vendorName": "Food Express",
    "stationCode": "AGC",
    "stationName": "Agra Cantt",
    "contactEmail": "orders@foodexpress.com",
    "contactPhone": "0562-1234567",
    "rating": 4.2,
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

### 22. Get Menu by Vendor
**Endpoint:** `GET /api/meal-booking/menu/vendor/{vendorId}`

**Response (200 OK):**
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
  },
  {
    "id": 2,
    "vendor": {
      "id": 1,
      "vendorName": "Railway Catering Services"
    },
    "itemName": "Chicken Curry",
    "description": "Spicy chicken curry with rice",
    "price": 200.00,
    "category": "MAIN_COURSE",
    "mealType": "LUNCH",
    "isVegetarian": false,
    "isAvailable": true,
    "preparationTimeMinutes": 20,
    "imageUrl": "https://example.com/chicken-curry.jpg",
    "createdAt": "2024-01-01T00:00:00"
  }
]
```

### 23. Place Meal Order
**Endpoint:** `POST /api/meal-booking/order`

**Request:**
```json
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
    },
    {
      "mealItemId": 2,
      "quantity": 1,
      "pricePerItem": 200.00
    }
  ]
}
```

**Response (200 OK):**
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
  "totalAmount": 500.00,
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
    },
    {
      "id": 2,
      "mealItem": {
        "id": 2,
        "itemName": "Chicken Curry",
        "price": 200.00
      },
      "quantity": 1,
      "pricePerItem": 200.00,
      "subtotal": 200.00
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

## 🗺️ Trip Planner APIs

### 24. Search Multi-City Itineraries
**Endpoint:** `POST /api/trip-planner/search`

**Request:**
```json
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

**Response (200 OK):**
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

## 📱 Mobile API Endpoints

### 25. Get Mobile Train List (Paginated)
**Endpoint:** `GET /api/mobile/trains`

**Query Parameters:**
- `page`: 0
- `size`: 10
- `sortBy`: departureTime
- `sortDirection`: ASC

**Response (200 OK):**
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

### 26. Get User Bookings (Mobile)
**Endpoint:** `GET /api/mobile/users/{userId}/bookings`

**Query Parameters:**
- `page`: 0
- `size`: 5
- `sortBy`: bookingDate
- `sortDirection`: DESC

**Response (200 OK):**
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

## ❌ Error Responses

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users/register",
  "details": [
    {
      "field": "email",
      "message": "Email must be valid"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/trains"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Insufficient privileges",
  "path": "/api/admin/users"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Train not found with ID: 999",
  "path": "/api/trains/999"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/bookings"
}
```

## 🔧 Testing with cURL

### Authentication Flow
```bash
# 1. Register a new user
curl -X POST http://localhost:8082/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "password": "SecurePass123!",
    "phoneNumber": "9876543210",
    "dateOfBirth": "1990-01-15T00:00:00",
    "gender": "MALE",
    "address": "123 Main Street, City, State"
  }'

# 2. Login to get token
TOKEN=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"SecurePass123!"}' | \
  jq -r '.accessToken')

# 3. Use token for authenticated requests
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/trains
```

### Complete Booking Flow
```bash
# 1. Search trains
curl -X POST http://localhost:8082/api/trains/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceStationCode": "NDLS",
    "destinationStationCode": "MUMB",
    "journeyDate": "2024-01-20",
    "preferredClass": "AC2",
    "numberOfPassengers": 1
  }'

# 2. Get available seats
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8082/api/seat-selection/trains/1/coaches/1/seats?journeyDate=2024-01-20"

# 3. Create booking
curl -X POST http://localhost:8082/api/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "passengerId": 1,
    "seatId": 5,
    "coachId": 1,
    "journeyDate": "2024-01-20",
    "totalFare": 2500.00,
    "baseFare": 2000.00,
    "quotaType": "GENERAL",
    "isTatkal": false
  }'

# 4. Create payment
curl -X POST http://localhost:8082/api/payments/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "amount": 2500.00,
    "paymentMethod": "RAZORPAY",
    "currency": "INR"
  }'
```

---

This comprehensive API payload documentation covers all the major endpoints of the IRCTC Backend System. Each example includes realistic request and response data that can be used for testing and integration purposes.
