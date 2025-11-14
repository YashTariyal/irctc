#!/bin/bash

# Production Setup Script
# This script helps set up the payment service for production deployment

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Payment Service Production Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Creating .env file from template...${NC}"
    cp .env.example .env
    echo -e "${GREEN}✅ .env file created${NC}"
    echo -e "${YELLOW}Please edit .env file with your actual values${NC}"
    echo ""
fi

# Function to check environment variable
check_env_var() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo -e "${RED}❌ $var_name is not set${NC}"
        return 1
    else
        echo -e "${GREEN}✅ $var_name is set${NC}"
        return 0
    fi
}

# Load .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo -e "${YELLOW}Checking environment variables...${NC}"
echo ""

# Check Razorpay configuration
echo -e "${BLUE}Razorpay Configuration:${NC}"
check_env_var RAZORPAY_KEY_ID
check_env_var RAZORPAY_KEY_SECRET
check_env_var RAZORPAY_WEBHOOK_SECRET
echo ""

# Check Stripe configuration
echo -e "${BLUE}Stripe Configuration:${NC}"
if [ "$STRIPE_ENABLED" = "true" ]; then
    check_env_var STRIPE_SECRET_KEY
    check_env_var STRIPE_PUBLISHABLE_KEY
    check_env_var STRIPE_WEBHOOK_SECRET
else
    echo -e "${YELLOW}⚠️  Stripe is disabled${NC}"
fi
echo ""

# Check PayU configuration
echo -e "${BLUE}PayU Configuration:${NC}"
if [ "$PAYU_ENABLED" = "true" ]; then
    check_env_var PAYU_MERCHANT_KEY
    check_env_var PAYU_MERCHANT_SALT
else
    echo -e "${YELLOW}⚠️  PayU is disabled${NC}"
fi
echo ""

# Check database configuration
echo -e "${BLUE}Database Configuration:${NC}"
check_env_var SPRING_DATASOURCE_URL
check_env_var SPRING_DATASOURCE_USERNAME
echo ""

# Check Redis configuration
echo -e "${BLUE}Redis Configuration:${NC}"
check_env_var SPRING_REDIS_HOST
check_env_var SPRING_REDIS_PORT
echo ""

# Check gateway mode
echo -e "${BLUE}Gateway Mode:${NC}"
if [ "$PAYMENT_GATEWAY_RAZORPAY_USE_REAL_SDK" = "true" ]; then
    echo -e "${GREEN}✅ Razorpay Real SDK enabled${NC}"
else
    echo -e "${YELLOW}⚠️  Razorpay using simulation mode${NC}"
fi

if [ "$PAYMENT_GATEWAY_STRIPE_USE_REAL_SDK" = "true" ]; then
    echo -e "${GREEN}✅ Stripe Real SDK enabled${NC}"
else
    echo -e "${YELLOW}⚠️  Stripe using simulation mode${NC}"
fi
echo ""

# Test webhook endpoints
echo -e "${YELLOW}Testing webhook endpoints...${NC}"
echo ""

# Test Razorpay webhook
if [ ! -z "$RAZORPAY_WEBHOOK_SECRET" ]; then
    echo -e "${BLUE}Testing Razorpay webhook...${NC}"
    if ./scripts/test-webhook-razorpay.sh > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Razorpay webhook test passed${NC}"
    else
        echo -e "${RED}❌ Razorpay webhook test failed${NC}"
    fi
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Setup Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
echo "1. Configure webhook URLs in gateway dashboards"
echo "2. Test payment processing"
echo "3. Set up monitoring and alerts"
echo "4. Review PRODUCTION_DEPLOYMENT_GUIDE.md"
echo ""

