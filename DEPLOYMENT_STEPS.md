# 🚀 Step-by-Step Deployment Guide

Complete walkthrough for deploying the Payment Service to production.

---

## Step 1: Obtain Gateway API Keys

### 1.1 Razorpay
```bash
# Follow the guide
cat scripts/obtain-api-keys.md | grep -A 20 "Razorpay"
```

**Quick Steps:**
1. Sign up at https://razorpay.com
2. Complete KYC
3. Go to Settings → API Keys
4. Generate Test/Live keys
5. Copy Key ID and Secret
6. Set up webhook and copy webhook secret

### 1.2 Stripe
```bash
# Follow the guide
cat scripts/obtain-api-keys.md | grep -A 20 "Stripe"
```

**Quick Steps:**
1. Sign up at https://stripe.com
2. Complete business details
3. Go to Developers → API keys
4. Copy Publishable and Secret keys
5. Set up webhook and copy signing secret

### 1.3 PayU
```bash
# Follow the guide
cat scripts/obtain-api-keys.md | grep -A 20 "PayU"
```

**Quick Steps:**
1. Sign up at https://www.payu.in
2. Complete merchant onboarding
3. Go to Settings → Merchant Key & Salt
4. Copy Merchant Key and Salt

---

## Step 2: Configure Webhook URLs

### 2.1 Generate Webhook URLs
```bash
chmod +x scripts/configure-webhooks.sh
./scripts/configure-webhooks.sh
```

This will:
- Prompt for your domain
- Generate webhook URLs for all gateways
- Save configuration to `webhook-config.txt`
- Test endpoint accessibility

### 2.2 Configure in Dashboards

**Razorpay:**
1. Go to https://dashboard.razorpay.com/app/webhooks
2. Click "Add New Webhook"
3. Enter URL from `webhook-config.txt`
4. Select events
5. Copy webhook secret

**Stripe:**
1. Go to https://dashboard.stripe.com/webhooks
2. Click "Add endpoint"
3. Enter URL from `webhook-config.txt`
4. Select events
5. Copy signing secret

**PayU:**
1. Go to https://dashboard.payu.in/webhooks
2. Add webhook URL from `webhook-config.txt`
3. Select events

---

## Step 3: Set Environment Variables

### 3.1 Create .env File
```bash
cp .env.example .env
nano .env  # or use your preferred editor
```

### 3.2 Add Your Keys
```env
# Razorpay
RAZORPAY_KEY_ID=rzp_live_xxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=your_key_secret
RAZORPAY_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# Stripe
STRIPE_SECRET_KEY=sk_live_xxxxxxxxxxxxx
STRIPE_PUBLISHABLE_KEY=pk_live_xxxxxxxxxxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# PayU
PAYU_MERCHANT_KEY=your_merchant_key
PAYU_MERCHANT_SALT=your_merchant_salt

# Enable Real SDK
PAYMENT_GATEWAY_RAZORPAY_USE_REAL_SDK=true
PAYMENT_GATEWAY_STRIPE_USE_REAL_SDK=true
```

### 3.3 Validate Configuration
```bash
chmod +x scripts/validate-env.sh
./scripts/validate-env.sh
```

This validates:
- All required variables are set
- Variable formats are correct
- Optional variables are noted

---

## Step 4: Deploy

### Option A: Docker Compose
```bash
# Make scripts executable
chmod +x scripts/*.sh

# Deploy
./scripts/deploy.sh docker
```

### Option B: Kubernetes
```bash
# Deploy
./scripts/deploy.sh kubernetes
```

### Option C: Manual
```bash
# Build
cd irctc-payment-service
./mvnw clean package -DskipTests

# Run
java -jar target/irctc-payment-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

---

## Step 5: Test Webhook Endpoints

### 5.1 Test All Webhooks
```bash
chmod +x scripts/test-all-webhooks.sh
./scripts/test-all-webhooks.sh
```

### 5.2 Test Individual Webhooks
```bash
# Razorpay
./scripts/test-webhook-razorpay.sh

# Stripe
./scripts/test-webhook-stripe.sh

# PayU
./scripts/test-webhook-payu.sh
```

### 5.3 Test with Gateway Tools

**Razorpay:**
- Use Dashboard → Webhooks → Test Webhook

**Stripe:**
```bash
stripe listen --forward-to https://your-domain.com/api/payments/webhooks/stripe
stripe trigger payment_intent.succeeded
```

---

## Step 6: Monitor Analytics Dashboard

### 6.1 Start Monitoring
```bash
chmod +x scripts/monitor-analytics.sh
./scripts/monitor-analytics.sh
```

### 6.2 Access Dashboard
```bash
# Overview
curl http://localhost:8083/api/payments/analytics/overview | jq

# Gateway Performance
curl http://localhost:8083/api/payments/analytics/gateway-performance | jq

# Daily Stats
curl http://localhost:8083/api/payments/analytics/daily | jq
```

### 6.3 Set Up Continuous Monitoring
```bash
# Run in background
nohup ./scripts/monitor-analytics.sh > analytics.log 2>&1 &

# Or use systemd service
sudo systemctl enable payment-analytics-monitor
sudo systemctl start payment-analytics-monitor
```

---

## Step 7: Post-Deployment Verification

### 7.1 Run Deployment Tests
```bash
chmod +x scripts/test-deployment.sh
./scripts/test-deployment.sh
```

### 7.2 Verify Health
```bash
curl http://localhost:8083/actuator/health | jq
```

### 7.3 Check Logs
```bash
# Docker
docker-compose -f docker-compose.prod.yml logs -f payment-service

# Kubernetes
kubectl logs -f deployment/payment-service
```

### 7.4 Verify Metrics
```bash
curl http://localhost:8083/actuator/metrics | jq
curl http://localhost:8083/actuator/prometheus
```

---

## ✅ Verification Checklist

- [ ] All API keys obtained and configured
- [ ] Webhook URLs configured in all gateway dashboards
- [ ] Environment variables validated
- [ ] Application deployed successfully
- [ ] Health checks passing
- [ ] Webhook endpoints tested
- [ ] Analytics dashboard accessible
- [ ] Monitoring set up
- [ ] Logs being generated
- [ ] Metrics being collected

---

## 🆘 Troubleshooting

### Issue: Webhook not received
**Solution:**
1. Check webhook URL is accessible
2. Verify signature in logs
3. Test with gateway test tools
4. Check firewall rules

### Issue: Payment processing fails
**Solution:**
1. Verify API keys are correct
2. Check gateway status
3. Review payment logs
4. Test with simulation mode first

### Issue: Analytics not showing data
**Solution:**
1. Verify database connection
2. Check if payments are being processed
3. Review analytics service logs
4. Clear cache if needed

---

## 📞 Support

For deployment issues:
1. Check logs: `logs/payment-service.log`
2. Review documentation
3. Run validation scripts
4. Check health endpoints

---

**Last Updated**: November 2025

