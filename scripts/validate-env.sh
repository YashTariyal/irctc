#!/bin/bash

# Environment Variable Validation Script
# Validates that all required environment variables are set correctly

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

ERRORS=0
WARNINGS=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Environment Variable Validation${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Load .env file if it exists
if [ -f .env ]; then
    echo -e "${YELLOW}Loading .env file...${NC}"
    export $(cat .env | grep -v '^#' | xargs)
    echo -e "${GREEN}✅ .env file loaded${NC}"
    echo ""
else
    echo -e "${YELLOW}⚠️  .env file not found${NC}"
    echo -e "${YELLOW}Using environment variables from current shell${NC}"
    echo ""
fi

# Function to validate variable
validate_var() {
    local var_name=$1
    local var_value=${!var_name}
    local required=${2:-true}
    local pattern=${3:-""}
    
    if [ -z "$var_value" ]; then
        if [ "$required" = "true" ]; then
            echo -e "${RED}❌ $var_name is not set (REQUIRED)${NC}"
            ((ERRORS++))
            return 1
        else
            echo -e "${YELLOW}⚠️  $var_name is not set (OPTIONAL)${NC}"
            ((WARNINGS++))
            return 0
        fi
    else
        # Check pattern if provided
        if [ -n "$pattern" ]; then
            if [[ ! "$var_value" =~ $pattern ]]; then
                echo -e "${RED}❌ $var_name has invalid format${NC}"
                ((ERRORS++))
                return 1
            fi
        fi
        
        # Mask sensitive values
        if [[ "$var_name" == *"SECRET"* ]] || [[ "$var_name" == *"KEY"* ]] || [[ "$var_name" == *"PASSWORD"* ]]; then
            local masked_value="${var_value:0:8}...${var_value: -4}"
            echo -e "${GREEN}✅ $var_name is set: $masked_value${NC}"
        else
            echo -e "${GREEN}✅ $var_name is set: $var_value${NC}"
        fi
        return 0
    fi
}

# Validate Razorpay
echo -e "${BLUE}Razorpay Configuration:${NC}"
validate_var "RAZORPAY_KEY_ID" true "^rzp_(test_|live_)"
validate_var "RAZORPAY_KEY_SECRET" true "^.{20,}"
validate_var "RAZORPAY_WEBHOOK_SECRET" false "^whsec_"
echo ""

# Validate Stripe
echo -e "${BLUE}Stripe Configuration:${NC}"
if [ "${STRIPE_ENABLED:-false}" = "true" ]; then
    validate_var "STRIPE_SECRET_KEY" true "^sk_(test_|live_)"
    validate_var "STRIPE_PUBLISHABLE_KEY" true "^pk_(test_|live_)"
    validate_var "STRIPE_WEBHOOK_SECRET" false "^whsec_"
else
    echo -e "${YELLOW}⚠️  Stripe is disabled${NC}"
fi
echo ""

# Validate PayU
echo -e "${BLUE}PayU Configuration:${NC}"
if [ "${PAYU_ENABLED:-false}" = "true" ]; then
    validate_var "PAYU_MERCHANT_KEY" true "^.{10,}"
    validate_var "PAYU_MERCHANT_SALT" true "^.{10,}"
else
    echo -e "${YELLOW}⚠️  PayU is disabled${NC}"
fi
echo ""

# Validate Database
echo -e "${BLUE}Database Configuration:${NC}"
validate_var "SPRING_DATASOURCE_URL" true "^jdbc:"
validate_var "SPRING_DATASOURCE_USERNAME" true
validate_var "SPRING_DATASOURCE_PASSWORD" true
echo ""

# Validate Redis
echo -e "${BLUE}Redis Configuration:${NC}"
validate_var "SPRING_REDIS_HOST" false
validate_var "SPRING_REDIS_PORT" false
echo ""

# Validate Server
echo -e "${BLUE}Server Configuration:${NC}"
validate_var "SERVER_PORT" false
echo ""

# Validate Gateway Mode
echo -e "${BLUE}Gateway Mode:${NC}"
if [ "${PAYMENT_GATEWAY_RAZORPAY_USE_REAL_SDK:-false}" = "true" ]; then
    echo -e "${GREEN}✅ Razorpay Real SDK enabled${NC}"
else
    echo -e "${YELLOW}⚠️  Razorpay using simulation mode${NC}"
fi

if [ "${PAYMENT_GATEWAY_STRIPE_USE_REAL_SDK:-false}" = "true" ]; then
    echo -e "${GREEN}✅ Stripe Real SDK enabled${NC}"
else
    echo -e "${YELLOW}⚠️  Stripe using simulation mode${NC}"
fi
echo ""

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Validation Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ All required variables are set correctly!${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  $WARNINGS optional variables are not set${NC}"
    fi
    echo ""
    echo -e "${GREEN}Ready for deployment!${NC}"
    exit 0
else
    echo -e "${RED}❌ $ERRORS required variables are missing or invalid${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  $WARNINGS optional variables are not set${NC}"
    fi
    echo ""
    echo -e "${RED}Please fix the errors before deploying${NC}"
    exit 1
fi

