#!/bin/bash

echo "🚂 Setting up IRCTC Booking Data"
echo "================================="
echo ""

# Base URL
BASE_URL="http://localhost:8080"

echo "📋 Step 1: Creating User..."
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "9876543210",
    "gender": "MALE",
    "idProofType": "AADHAR",
    "idProofNumber": "123456789012",
    "isVerified": true,
    "isActive": true,
    "role": "USER"
  }')

if [[ $USER_RESPONSE == *"id"* ]]; then
    echo "✅ User created successfully"
    USER_ID=$(echo $USER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   User ID: $USER_ID"
else
    echo "❌ Failed to create user: $USER_RESPONSE"
    exit 1
fi

echo ""
echo "🚂 Step 2: Creating Train..."
TRAIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/trains" \
  -H "Content-Type: application/json" \
  -d '{
    "trainNumber": "12345",
    "trainName": "Rajdhani Express",
    "sourceStation": { "id": 1 },
    "destinationStation": { "id": 2 },
    "departureTime": "08:00:00",
    "arrivalTime": "18:00:00",
    "journeyDuration": 600,
    "totalDistance": 800.0,
    "trainType": "RAJDHANI",
    "status": "ACTIVE",
    "isRunning": true
  }')

if [[ $TRAIN_RESPONSE == *"id"* ]]; then
    echo "✅ Train created successfully"
    TRAIN_ID=$(echo $TRAIN_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Train ID: $TRAIN_ID"
else
    echo "❌ Failed to create train: $TRAIN_RESPONSE"
    exit 1
fi

echo ""
echo "👥 Step 3: Creating Passenger..."
PASSENGER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/passengers" \
  -H "Content-Type: application/json" \
  -d "{
    \"user\": { \"id\": $USER_ID },
    \"firstName\": \"John\",
    \"lastName\": \"Doe\",
    \"age\": 34,
    \"gender\": \"MALE\",
    \"passengerType\": \"ADULT\",
    \"idProofType\": \"AADHAR\",
    \"idProofNumber\": \"123456789012\",
    \"isSeniorCitizen\": false,
    \"isLadiesQuota\": false,
    \"isHandicapped\": false
  }")

if [[ $PASSENGER_RESPONSE == *"id"* ]]; then
    echo "✅ Passenger created successfully"
    PASSENGER_ID=$(echo $PASSENGER_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Passenger ID: $PASSENGER_ID"
else
    echo "❌ Failed to create passenger: $PASSENGER_RESPONSE"
    exit 1
fi

echo ""
echo "🚇 Step 4: Creating Coach..."
COACH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/coaches" \
  -H "Content-Type: application/json" \
  -d "{
    \"train\": { \"id\": $TRAIN_ID },
    \"coachNumber\": \"A1\",
    \"coachType\": \"AC_FIRST_CLASS\",
    \"totalSeats\": 24,
    \"availableSeats\": 20,
    \"farePerSeat\": 2500.00,
    \"isActive\": true
  }")

if [[ $COACH_RESPONSE == *"id"* ]]; then
    echo "✅ Coach created successfully"
    COACH_ID=$(echo $COACH_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Coach ID: $COACH_ID"
else
    echo "❌ Failed to create coach: $COACH_RESPONSE"
    exit 1
fi

echo ""
echo "💺 Step 5: Creating Seat..."
SEAT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/seats" \
  -H "Content-Type: application/json" \
  -d "{
    \"coach\": { \"id\": $COACH_ID },
    \"seatNumber\": \"A1-1\",
    \"seatType\": \"LOWER_BERTH\",
    \"isAvailable\": true,
    \"isReserved\": false,
    \"fare\": 2500.00
  }")

if [[ $SEAT_RESPONSE == *"id"* ]]; then
    echo "✅ Seat created successfully"
    SEAT_ID=$(echo $SEAT_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Seat ID: $SEAT_ID"
else
    echo "❌ Failed to create seat: $SEAT_RESPONSE"
    exit 1
fi

echo ""
echo "🎯 Step 6: Creating Booking..."
BOOKING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/bookings" \
  -H "Content-Type: application/json" \
  -d "{
    \"user\": { \"id\": $USER_ID },
    \"train\": { \"id\": $TRAIN_ID },
    \"passenger\": { \"id\": $PASSENGER_ID },
    \"coach\": { \"id\": $COACH_ID },
    \"seat\": { \"id\": $SEAT_ID },
    \"journeyDate\": \"2024-12-25\",
    \"totalFare\": 2500.00,
    \"status\": \"CONFIRMED\",
    \"paymentStatus\": \"PENDING\"
  }")

if [[ $BOOKING_RESPONSE == *"id"* ]]; then
    echo "✅ Booking created successfully!"
    BOOKING_ID=$(echo $BOOKING_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Booking ID: $BOOKING_ID"
else
    echo "❌ Failed to create booking: $BOOKING_RESPONSE"
    exit 1
fi

echo ""
echo "📊 Summary of Created Entities:"
echo "   User ID: $USER_ID"
echo "   Train ID: $TRAIN_ID"
echo "   Passenger ID: $PASSENGER_ID"
echo "   Coach ID: $COACH_ID"
echo "   Seat ID: $SEAT_ID"
echo "   Booking ID: $BOOKING_ID"

echo ""
echo "🎉 All data setup completed successfully!"
echo ""
echo "🔍 You can now verify the booking:"
echo "   curl $BASE_URL/api/bookings/$BOOKING_ID"
echo ""
echo "📋 Or check all bookings:"
echo "   curl $BASE_URL/api/bookings"
