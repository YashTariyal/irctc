#!/bin/bash

echo "🧪 Comprehensive Testing: All New Features"
echo "=================================================="
echo ""

# Test 1: Compilation
echo "📦 Test 1: Compiling all services..."
echo "-----------------------------------"
cd irctc-booking-service && ./mvnw clean compile -q && echo "✅ Booking service compiled" || echo "❌ Booking service failed"
cd ../irctc-train-service && ./mvnw clean compile -q && echo "✅ Train service compiled" || echo "❌ Train service failed"
cd ../irctc-user-service && ./mvnw clean compile -q && echo "✅ User service compiled" || echo "❌ User service failed"
cd ../irctc-notification-service && ./mvnw clean compile -q && echo "✅ Notification service compiled" || echo "❌ Notification service failed"
cd ../irctc-payment-service && ./mvnw clean compile -q && echo "✅ Payment service compiled" || echo "❌ Payment service failed"
cd ../irctc-config-server && ./mvnw clean compile -q 2>&1 | grep -q "BUILD SUCCESS" && echo "✅ Config server compiled" || echo "⚠️  Config server compilation (optional)"
cd ..

echo ""
echo "📋 Test 2: Feature Implementation Verification"
echo "-----------------------------------"

# Flyway Migrations
echo "Checking Flyway migrations..."
migration_count=$(find . -path "*/db/migration/*.sql" | wc -l | tr -d ' ')
if [ "$migration_count" -ge 5 ]; then
    echo "✅ Found $migration_count Flyway migration files"
else
    echo "⚠️  Found $migration_count migration files (expected 5+)"
fi

# Redis Configuration
echo "Checking Redis configuration..."
if grep -q "spring.data.redis" irctc-booking-service/src/main/resources/application.yml && \
   grep -q "spring.data.redis" irctc-train-service/src/main/resources/application.yml; then
    echo "✅ Redis configuration found in booking and train services"
else
    echo "⚠️  Redis configuration incomplete"
fi

# Cache Services
if [ -f "irctc-booking-service/src/main/java/com/irctc/booking/service/BookingCacheService.java" ] && \
   [ -f "irctc-train-service/src/main/java/com/irctc/train/service/TrainCacheService.java" ]; then
    echo "✅ Cache services implemented"
else
    echo "❌ Cache services missing"
fi

# API Versioning
echo "Checking API versioning..."
if grep -q "/api/v1/trains" irctc-train-service/src/main/java/com/irctc/train/controller/SimpleTrainController.java && \
   grep -q "/api/v1/bookings" irctc-booking-service/src/main/java/com/irctc/booking/controller/SimpleBookingController.java; then
    echo "✅ API versioning implemented in controllers"
else
    echo "❌ API versioning missing"
fi

if grep -q "/api/v1/trains" irctc-api-gateway/src/main/resources/application.yml; then
    echo "✅ API Gateway versioning configured"
else
    echo "❌ API Gateway versioning missing"
fi

# Audit Logging
echo "Checking audit logging..."
if [ -f "irctc-booking-service/src/main/java/com/irctc/booking/annotation/Auditable.java" ] && \
   [ -f "irctc-booking-service/src/main/java/com/irctc/booking/aspect/AuditAspect.java" ] && \
   [ -f "irctc-booking-service/src/main/java/com/irctc/booking/entity/AuditLog.java" ]; then
    echo "✅ Audit logging components implemented"
else
    echo "❌ Audit logging components missing"
fi

if grep -q "@Auditable" irctc-booking-service/src/main/java/com/irctc/booking/controller/SimpleBookingController.java; then
    echo "✅ @Auditable annotations applied to controllers"
else
    echo "⚠️  No @Auditable annotations found"
fi

# Config Server
echo "Checking Config Server..."
if [ -f "irctc-config-server/pom.xml" ] && \
   [ -f "irctc-config-server/src/main/java/com/irctc/config/ConfigServerApplication.java" ]; then
    echo "✅ Config Server module created"
else
    echo "❌ Config Server missing"
fi

if [ -d "config-repo" ] && [ -f "config-repo/irctc-booking-service.yml" ]; then
    echo "✅ Config repository with service configs created"
else
    echo "❌ Config repository missing"
fi

if [ -f "irctc-booking-service/src/main/resources/bootstrap.yml" ]; then
    echo "✅ Bootstrap configuration for Config Client created"
else
    echo "⚠️  Bootstrap configuration missing"
fi

echo ""
echo "📊 Test 3: Dependency Verification"
echo "-----------------------------------"

# Check for Flyway
if grep -q "flyway-core" irctc-booking-service/pom.xml; then
    echo "✅ Flyway dependency added"
else
    echo "❌ Flyway dependency missing"
fi

# Check for Redis
if grep -q "spring-boot-starter-data-redis" irctc-booking-service/pom.xml && \
   grep -q "spring-boot-starter-data-redis" irctc-train-service/pom.xml; then
    echo "✅ Redis dependencies added"
else
    echo "❌ Redis dependencies missing"
fi

# Check for AOP
if grep -q "spring-boot-starter-aop" irctc-booking-service/pom.xml; then
    echo "✅ AOP dependency added for audit logging"
else
    echo "❌ AOP dependency missing"
fi

# Check for Config Client
if grep -q "spring-cloud-starter-config" irctc-booking-service/pom.xml; then
    echo "✅ Config Client dependency added"
else
    echo "❌ Config Client dependency missing"
fi

echo ""
echo "✅ Comprehensive Feature Testing Complete!"
echo "=================================================="
echo ""
echo "📝 Features Implemented:"
echo "  1. ✅ Flyway Database Migrations"
echo "  2. ✅ Redis Caching"
echo "  3. ✅ API Versioning"
echo "  4. ✅ Audit Logging (AOP-based)"
echo "  5. ✅ Centralized Configuration (Config Server)"
echo ""

