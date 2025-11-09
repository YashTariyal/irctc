#!/bin/bash

# Contract Testing Script for IRCTC Microservices
# This script runs consumer tests to generate contracts, then verifies them against providers

set -e

echo "🤝 IRCTC Contract Testing"
echo "=========================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Run Consumer Tests (Generate Contracts)
echo -e "${BLUE}Step 1: Running Consumer Tests (Booking Service)${NC}"
echo "----------------------------------------"
cd irctc-booking-service

echo "Running PaymentServiceContractTest..."
../mvnw test -Dtest=PaymentServiceContractTest 2>&1 | tail -20

echo ""
echo "Running UserServiceContractTest..."
../mvnw test -Dtest=UserServiceContractTest 2>&1 | tail -20

# Step 2: Copy Contracts to Providers
echo ""
echo -e "${BLUE}Step 2: Copying Contracts to Provider Services${NC}"
echo "----------------------------------------"

# Find and copy Pact files
if [ -d "target/pacts" ]; then
    echo "Found Pact files in target/pacts/"
    
    # Copy to payment service
    if [ -f "target/pacts/booking-service-payment-service.json" ]; then
        cp target/pacts/booking-service-payment-service.json \
           ../irctc-payment-service/src/test/resources/pacts/ 2>/dev/null || \
        mkdir -p ../irctc-payment-service/src/test/resources/pacts && \
        cp target/pacts/booking-service-payment-service.json \
           ../irctc-payment-service/src/test/resources/pacts/
        echo -e "${GREEN}✅ Copied contract to payment-service${NC}"
    fi
    
    # Copy to user service
    if [ -f "target/pacts/booking-service-user-service.json" ]; then
        cp target/pacts/booking-service-user-service.json \
           ../irctc-user-service/src/test/resources/pacts/ 2>/dev/null || \
        mkdir -p ../irctc-user-service/src/test/resources/pacts && \
        cp target/pacts/booking-service-user-service.json \
           ../irctc-user-service/src/test/resources/pacts/
        echo -e "${GREEN}✅ Copied contract to user-service${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  No Pact files found. Make sure consumer tests ran successfully.${NC}"
fi

cd ..

# Step 3: Run Provider Verification Tests
echo ""
echo -e "${BLUE}Step 3: Running Provider Verification Tests${NC}"
echo "----------------------------------------"

# Payment Service
echo ""
echo "Verifying Payment Service contracts..."
cd irctc-payment-service
if [ -f "src/test/resources/pacts/booking-service-payment-service.json" ]; then
    ../mvnw test -Dtest=PaymentServiceProviderTest 2>&1 | tail -20
    echo -e "${GREEN}✅ Payment Service verification complete${NC}"
else
    echo -e "${YELLOW}⚠️  No contract file found for payment-service${NC}"
fi
cd ..

# User Service
echo ""
echo "Verifying User Service contracts..."
cd irctc-user-service
if [ -f "src/test/resources/pacts/booking-service-user-service.json" ]; then
    ../mvnw test -Dtest=UserServiceProviderTest 2>&1 | tail -20
    echo -e "${GREEN}✅ User Service verification complete${NC}"
else
    echo -e "${YELLOW}⚠️  No contract file found for user-service${NC}"
fi
cd ..

echo ""
echo -e "${GREEN}✅ Contract Testing Complete!${NC}"
echo ""
echo "📋 Summary:"
echo "  - Consumer tests: Generate contracts"
echo "  - Provider tests: Verify contracts"
echo ""
echo "📖 See PACT_CONTRACT_TESTING_GUIDE.md for details"

