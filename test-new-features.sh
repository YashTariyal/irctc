#!/bin/bash

echo "🧪 Testing New Features: Flyway Migrations, Redis Caching, API Versioning"
echo "=================================================="

# Test 1: Compile all services
echo ""
echo "📦 Test 1: Compiling services..."
echo "-----------------------------------"

cd irctc-booking-service && ./mvnw clean compile -q && echo "✅ Booking service compiled" || echo "❌ Booking service failed"
cd ../irctc-train-service && ./mvnw clean compile -q && echo "✅ Train service compiled" || echo "❌ Train service failed"
cd ../irctc-user-service && ./mvnw clean compile -q && echo "✅ User service compiled" || echo "❌ User service failed"
cd ../irctc-notification-service && ./mvnw clean compile -q && echo "✅ Notification service compiled" || echo "❌ Notification service failed"
cd ../irctc-payment-service && ./mvnw clean compile -q && echo "✅ Payment service compiled" || echo "❌ Payment service failed"
cd ..

echo ""
echo "📋 Test 2: Checking Flyway migration files..."
echo "-----------------------------------"
if [ -f "irctc-booking-service/src/main/resources/db/migration/V1__Initial_booking_schema.sql" ]; then
    echo "✅ Booking service migration exists"
else
    echo "❌ Booking service migration missing"
fi

if [ -f "irctc-train-service/src/main/resources/db/migration/V1__Initial_train_schema.sql" ]; then
    echo "✅ Train service migration exists"
else
    echo "❌ Train service migration missing"
fi

if [ -f "irctc-user-service/src/main/resources/db/migration/V1__Initial_user_schema.sql" ]; then
    echo "✅ User service migration exists"
else
    echo "❌ User service migration missing"
fi

if [ -f "irctc-notification-service/src/main/resources/db/migration/V1__Initial_notification_schema.sql" ]; then
    echo "✅ Notification service migration exists"
else
    echo "❌ Notification service migration missing"
fi

if [ -f "irctc-payment-service/src/main/resources/db/migration/V1__Initial_payment_schema.sql" ]; then
    echo "✅ Payment service migration exists"
else
    echo "❌ Payment service migration missing"
fi

echo ""
echo "🔧 Test 3: Checking Redis configuration..."
echo "-----------------------------------"
if grep -q "spring.data.redis" irctc-booking-service/src/main/resources/application.yml; then
    echo "✅ Booking service Redis config found"
else
    echo "⚠️  Booking service Redis config missing (optional)"
fi

if grep -q "spring.data.redis" irctc-train-service/src/main/resources/application.yml; then
    echo "✅ Train service Redis config found"
else
    echo "⚠️  Train service Redis config missing (optional)"
fi

if [ -f "irctc-booking-service/src/main/java/com/irctc/booking/service/BookingCacheService.java" ]; then
    echo "✅ Booking cache service exists"
else
    echo "❌ Booking cache service missing"
fi

if [ -f "irctc-train-service/src/main/java/com/irctc/train/service/TrainCacheService.java" ]; then
    echo "✅ Train cache service exists"
else
    echo "❌ Train cache service missing"
fi

echo ""
echo "🌐 Test 4: Checking API versioning..."
echo "-----------------------------------"
if grep -q "/api/v1/trains" irctc-train-service/src/main/java/com/irctc/train/controller/SimpleTrainController.java; then
    echo "✅ Train controller versioning found"
else
    echo "❌ Train controller versioning missing"
fi

if grep -q "/api/v1/bookings" irctc-booking-service/src/main/java/com/irctc/booking/controller/SimpleBookingController.java; then
    echo "✅ Booking controller versioning found"
else
    echo "❌ Booking controller versioning missing"
fi

if grep -q "/api/v1/trains" irctc-api-gateway/src/main/resources/application.yml; then
    echo "✅ API Gateway v1 routes configured"
else
    echo "❌ API Gateway v1 routes missing"
fi

echo ""
echo "✅ Feature Testing Complete!"
echo "=================================================="

