#!/bin/bash

# Webhook Configuration Helper Script
# This script helps configure webhook URLs in gateway dashboards

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Webhook Configuration Helper${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Get webhook URL
read -p "Enter your webhook base URL (e.g., https://payment.irctc.com): " WEBHOOK_BASE_URL

if [ -z "$WEBHOOK_BASE_URL" ]; then
    echo -e "${RED}❌ Webhook URL is required${NC}"
    exit 1
fi

# Remove trailing slash
WEBHOOK_BASE_URL=${WEBHOOK_BASE_URL%/}

# Generate webhook URLs
RAZORPAY_WEBHOOK_URL="${WEBHOOK_BASE_URL}/api/payments/webhooks/razorpay"
STRIPE_WEBHOOK_URL="${WEBHOOK_BASE_URL}/api/payments/webhooks/stripe"
PAYU_WEBHOOK_URL="${WEBHOOK_BASE_URL}/api/payments/webhooks/payu"

echo ""
echo -e "${GREEN}Webhook URLs Generated:${NC}"
echo ""
echo -e "${BLUE}Razorpay:${NC}"
echo -e "${YELLOW}$RAZORPAY_WEBHOOK_URL${NC}"
echo ""
echo -e "${BLUE}Stripe:${NC}"
echo -e "${YELLOW}$STRIPE_WEBHOOK_URL${NC}"
echo ""
echo -e "${BLUE}PayU:${NC}"
echo -e "${YELLOW}$PAYU_WEBHOOK_URL${NC}"
echo ""

# Create configuration file
CONFIG_FILE="webhook-config.txt"
cat > "$CONFIG_FILE" << EOF
# Webhook Configuration
# Generated: $(date)

# Razorpay Webhook
URL: $RAZORPAY_WEBHOOK_URL
Events:
  - payment.captured
  - payment.authorized
  - payment.failed
  - refund.created
  - refund.processed

Dashboard: https://dashboard.razorpay.com/app/webhooks

# Stripe Webhook
URL: $STRIPE_WEBHOOK_URL
Events:
  - payment_intent.succeeded
  - payment_intent.payment_failed
  - charge.refunded

Dashboard: https://dashboard.stripe.com/webhooks

# PayU Webhook
URL: $PAYU_WEBHOOK_URL
Events:
  - Payment Success
  - Payment Failure
  - Refund Success

Dashboard: https://dashboard.payu.in/webhooks
EOF

echo -e "${GREEN}✅ Configuration saved to $CONFIG_FILE${NC}"
echo ""

# Instructions
echo -e "${YELLOW}Next Steps:${NC}"
echo ""
echo -e "${BLUE}1. Razorpay:${NC}"
echo "   a. Go to https://dashboard.razorpay.com/app/webhooks"
echo "   b. Click 'Add New Webhook'"
echo "   c. URL: $RAZORPAY_WEBHOOK_URL"
echo "   d. Select the events listed above"
echo "   e. Copy the webhook secret"
echo ""

echo -e "${BLUE}2. Stripe:${NC}"
echo "   a. Go to https://dashboard.stripe.com/webhooks"
echo "   b. Click 'Add endpoint'"
echo "   c. URL: $STRIPE_WEBHOOK_URL"
echo "   d. Select the events listed above"
echo "   e. Copy the signing secret"
echo ""

echo -e "${BLUE}3. PayU:${NC}"
echo "   a. Go to https://dashboard.payu.in/webhooks"
echo "   b. Add webhook URL: $PAYU_WEBHOOK_URL"
echo "   c. Select the events listed above"
echo ""

echo -e "${YELLOW}After configuring, update your .env file with webhook secrets${NC}"
echo ""

# Test webhook URLs
read -p "Do you want to test webhook URLs now? (y/n): " TEST_WEBHOOKS

if [ "$TEST_WEBHOOKS" = "y" ] || [ "$TEST_WEBHOOKS" = "Y" ]; then
    echo ""
    echo -e "${YELLOW}Testing webhook endpoints...${NC}"
    echo ""
    
    # Test Razorpay
    echo -e "${BLUE}Testing Razorpay webhook...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" "$RAZORPAY_WEBHOOK_URL" | grep -q "405\|400\|200"; then
        echo -e "${GREEN}✅ Razorpay webhook endpoint is accessible${NC}"
    else
        echo -e "${RED}❌ Razorpay webhook endpoint not accessible${NC}"
    fi
    
    # Test Stripe
    echo -e "${BLUE}Testing Stripe webhook...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" "$STRIPE_WEBHOOK_URL" | grep -q "405\|400\|200"; then
        echo -e "${GREEN}✅ Stripe webhook endpoint is accessible${NC}"
    else
        echo -e "${RED}❌ Stripe webhook endpoint not accessible${NC}"
    fi
    
    # Test PayU
    echo -e "${BLUE}Testing PayU webhook...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" "$PAYU_WEBHOOK_URL" | grep -q "405\|400\|200"; then
        echo -e "${GREEN}✅ PayU webhook endpoint is accessible${NC}"
    else
        echo -e "${RED}❌ PayU webhook endpoint not accessible${NC}"
    fi
fi

echo ""
echo -e "${GREEN}✅ Webhook configuration complete!${NC}"
echo ""

