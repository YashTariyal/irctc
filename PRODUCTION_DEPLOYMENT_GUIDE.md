# 🚀 Production Deployment Guide

Complete guide for deploying the Payment Service with Multiple Payment Gateways to production.

---

## 📋 Pre-Deployment Checklist

- [ ] All gateway API keys obtained
- [ ] Webhook secrets configured
- [ ] Database configured and migrated
- [ ] Redis cache configured
- [ ] SSL certificates obtained
- [ ] Domain name configured
- [ ] Monitoring tools set up
- [ ] Backup strategy defined
- [ ] Disaster recovery plan ready

---

## 🔐 Step 1: Configure Gateway API Keys

### Environment Variables Setup

Create a `.env` file or set environment variables:

```bash
# Razorpay
export RAZORPAY_KEY_ID=rzp_live_xxxxxxxxxxxxx
export RAZORPAY_KEY_SECRET=your_live_key_secret
export RAZORPAY_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# Stripe
export STRIPE_SECRET_KEY=sk_live_xxxxxxxxxxxxx
export STRIPE_PUBLISHABLE_KEY=pk_live_xxxxxxxxxxxxx
export STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# PayU
export PAYU_MERCHANT_KEY=your_merchant_key
export PAYU_MERCHANT_SALT=your_merchant_salt
```

### Kubernetes Secret (if using K8s)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: payment-gateway-secrets
type: Opaque
stringData:
  RAZORPAY_KEY_ID: rzp_live_xxxxxxxxxxxxx
  RAZORPAY_KEY_SECRET: your_live_key_secret
  RAZORPAY_WEBHOOK_SECRET: whsec_xxxxxxxxxxxxx
  STRIPE_SECRET_KEY: sk_live_xxxxxxxxxxxxx
  STRIPE_WEBHOOK_SECRET: whsec_xxxxxxxxxxxxx
  PAYU_MERCHANT_KEY: your_merchant_key
  PAYU_MERCHANT_SALT: your_merchant_salt
```

### Docker Compose

```yaml
services:
  payment-service:
    environment:
      - RAZORPAY_KEY_ID=${RAZORPAY_KEY_ID}
      - RAZORPAY_KEY_SECRET=${RAZORPAY_KEY_SECRET}
      - RAZORPAY_WEBHOOK_SECRET=${RAZORPAY_WEBHOOK_SECRET}
      - STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
      - STRIPE_WEBHOOK_SECRET=${STRIPE_WEBHOOK_SECRET}
      - PAYU_MERCHANT_KEY=${PAYU_MERCHANT_KEY}
      - PAYU_MERCHANT_SALT=${PAYU_MERCHANT_SALT}
```

---

## 🔗 Step 2: Configure Webhook URLs

### Razorpay Dashboard

1. Log in to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Go to **Settings** → **Webhooks**
3. Add webhook URL: `https://payment.irctc.com/api/payments/webhooks/razorpay`
4. Select events:
   - `payment.captured`
   - `payment.authorized`
   - `payment.failed`
   - `refund.created`
5. Copy webhook secret and set `RAZORPAY_WEBHOOK_SECRET`

### Stripe Dashboard

1. Log in to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Go to **Developers** → **Webhooks**
3. Click **Add endpoint**
4. URL: `https://payment.irctc.com/api/payments/webhooks/stripe`
5. Select events:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `charge.refunded`
6. Copy signing secret and set `STRIPE_WEBHOOK_SECRET`

### PayU Dashboard

1. Log in to [PayU Dashboard](https://dashboard.payu.in/)
2. Go to **Settings** → **Webhooks**
3. Add URL: `https://payment.irctc.com/api/payments/webhooks/payu`
4. Select events:
   - Payment Success
   - Payment Failure
   - Refund Success

---

## ⚙️ Step 3: Enable Real SDK Mode

### Update Configuration

Set in `application-prod.yml` or environment variables:

```yaml
payment:
  gateway:
    razorpay:
      use-real-sdk: true  # Enable real SDK
    stripe:
      use-real-sdk: true  # Enable real SDK
```

Or via environment:

```bash
export PAYMENT_GATEWAY_RAZORPAY_USE_REAL_SDK=true
export PAYMENT_GATEWAY_STRIPE_USE_REAL_SDK=true
```

### Verify SDK Initialization

Check logs for:
```
✅ Razorpay SDK initialized successfully
✅ Stripe SDK initialized successfully
```

---

## 🧪 Step 4: Test Webhook Endpoints

### Using Test Scripts

```bash
# Make scripts executable
chmod +x scripts/test-webhook-*.sh

# Test Razorpay
WEBHOOK_URL=https://payment.irctc.com/api/payments/webhooks/razorpay \
RAZORPAY_WEBHOOK_SECRET=your_secret \
./scripts/test-webhook-razorpay.sh

# Test Stripe
WEBHOOK_URL=https://payment.irctc.com/api/payments/webhooks/stripe \
STRIPE_WEBHOOK_SECRET=your_secret \
./scripts/test-webhook-stripe.sh

# Test PayU
WEBHOOK_URL=https://payment.irctc.com/api/payments/webhooks/payu \
PAYU_MERCHANT_SALT=your_salt \
./scripts/test-webhook-payu.sh
```

### Using Gateway Test Tools

#### Razorpay
1. Use Razorpay Dashboard → **Webhooks** → **Test Webhook**
2. Select event type
3. Verify response

#### Stripe
```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Forward webhooks
stripe listen --forward-to https://payment.irctc.com/api/payments/webhooks/stripe

# Trigger test event
stripe trigger payment_intent.succeeded
```

---

## 📊 Step 5: Deploy Analytics Dashboard

### Access Analytics Endpoints

```http
# Overview
GET https://payment.irctc.com/api/payments/analytics/overview

# Daily Stats
GET https://payment.irctc.com/api/payments/analytics/daily?startDate=2025-01-01&endDate=2025-01-31

# Gateway Performance
GET https://payment.irctc.com/api/payments/analytics/gateway-performance

# Payment Methods
GET https://payment.irctc.com/api/payments/analytics/payment-methods
```

### Frontend Integration

Example React component:

```javascript
// Fetch analytics data
const fetchAnalytics = async () => {
  const response = await fetch('https://payment.irctc.com/api/payments/analytics/overview');
  const data = await response.json();
  return data;
};

// Display in dashboard
const AnalyticsDashboard = () => {
  const [analytics, setAnalytics] = useState(null);
  
  useEffect(() => {
    fetchAnalytics().then(setAnalytics);
  }, []);
  
  return (
    <div>
      <h2>Payment Analytics</h2>
      <p>Total Transactions: {analytics?.totalTransactions}</p>
      <p>Success Rate: {analytics?.successRate}%</p>
      <p>Total Revenue: ₹{analytics?.totalAmount}</p>
    </div>
  );
};
```

---

## 📈 Step 6: Set Up Monitoring

### Health Checks

```http
GET /actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "payment-gateway": {"status": "UP"}
  }
}
```

### Metrics Endpoint

```http
GET /actuator/metrics
GET /actuator/prometheus
```

### Gateway Statistics

```http
GET /api/payments/gateways/stats
```

### Log Monitoring

Monitor logs for:
- Payment processing errors
- Webhook signature failures
- Gateway timeouts
- Database connection issues

### Alerting Rules

Set up alerts for:
- Payment failure rate > 5%
- Webhook processing errors
- Gateway unavailability
- High response times
- Database connection failures

---

## 🔒 Security Configuration

### SSL/TLS

- Use valid SSL certificates
- Enable TLS 1.2 or higher
- Configure HTTPS redirect

### Firewall Rules

- Allow only necessary ports (443, 8083)
- Restrict webhook endpoints to gateway IPs (if possible)
- Enable rate limiting

### Secrets Management

- Use secret management tools (Vault, AWS Secrets Manager)
- Never commit secrets to version control
- Rotate secrets regularly

---

## 🚀 Deployment Steps

### 1. Build Application

```bash
cd irctc-payment-service
./mvnw clean package -DskipTests
```

### 2. Run Database Migrations

```bash
# Migrations run automatically on startup
# Or manually:
./mvnw flyway:migrate
```

### 3. Start Application

```bash
# Using Spring Boot
java -jar target/irctc-payment-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod

# Using Docker
docker-compose up -d

# Using Kubernetes
kubectl apply -f k8s/payment-service.yaml
```

### 4. Verify Deployment

```bash
# Check health
curl https://payment.irctc.com/actuator/health

# Check gateway status
curl https://payment.irctc.com/api/payments/gateways

# Test payment processing
curl -X POST https://payment.irctc.com/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 123,
    "amount": 1000.00,
    "currency": "INR",
    "paymentMethod": "CARD"
  }'
```

---

## 📝 Post-Deployment Verification

### Checklist

- [ ] Application starts successfully
- [ ] Database connections working
- [ ] Redis cache connected
- [ ] Gateway APIs accessible
- [ ] Webhook endpoints responding
- [ ] Analytics endpoints working
- [ ] Health checks passing
- [ ] Logs being generated
- [ ] Metrics being collected
- [ ] Alerts configured

### Test Scenarios

1. **Process Payment**
   - Create test payment
   - Verify gateway selection
   - Check payment status

2. **Webhook Processing**
   - Send test webhook
   - Verify signature validation
   - Check payment status update

3. **Analytics**
   - Query analytics endpoints
   - Verify data accuracy
   - Check performance

4. **Fallback Mechanism**
   - Simulate gateway failure
   - Verify fallback to alternative gateway
   - Check statistics recording

---

## 🐛 Troubleshooting

### Common Issues

#### Gateway Not Responding
- Check API keys
- Verify network connectivity
- Check gateway status page

#### Webhook Not Received
- Verify webhook URL accessibility
- Check firewall rules
- Verify gateway configuration

#### Payment Status Not Updated
- Check webhook processing logs
- Verify transaction ID mapping
- Check database connection

---

## 📞 Support

For issues or questions:
- Check logs: `logs/payment-service.log`
- Review documentation: `WEBHOOK_SETUP_GUIDE.md`
- Contact: devops@irctc.com

---

**Last Updated**: November 2025  
**Status**: ✅ Production Ready

