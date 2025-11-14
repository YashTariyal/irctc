# 🔗 Webhook Setup Guide

This guide explains how to configure webhooks for payment gateways to receive real-time payment status updates.

---

## 📋 Prerequisites

1. Payment service deployed and accessible via HTTPS
2. Gateway accounts configured with API keys
3. Webhook endpoints accessible from the internet

---

## 🔧 Razorpay Webhook Setup

### Step 1: Get Webhook Secret

1. Log in to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Navigate to **Settings** → **Webhooks**
3. Click **Add New Webhook**
4. Copy the **Webhook Secret** (starts with `whsec_`)

### Step 2: Configure Webhook URL

**Webhook URL Format:**
```
https://your-domain.com/api/payments/webhooks/razorpay
```

**Example:**
```
https://payment.irctc.com/api/payments/webhooks/razorpay
```

### Step 3: Select Events

Select the following events:
- ✅ `payment.captured`
- ✅ `payment.authorized`
- ✅ `payment.failed`
- ✅ `refund.created`
- ✅ `refund.processed`

### Step 4: Set Environment Variable

```bash
export RAZORPAY_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

Or add to `.env` file:
```env
RAZORPAY_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

### Step 5: Test Webhook

Use Razorpay's webhook testing tool or send a test webhook:

```bash
curl -X POST https://your-domain.com/api/payments/webhooks/razorpay \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: <signature>" \
  -d '{
    "event": "payment.captured",
    "payload": {
      "payment": {
        "entity": {
          "id": "pay_test123"
        }
      }
    }
  }'
```

---

## 💳 Stripe Webhook Setup

### Step 1: Get Webhook Secret

1. Log in to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Navigate to **Developers** → **Webhooks**
3. Click **Add endpoint**
4. Enter webhook URL: `https://your-domain.com/api/payments/webhooks/stripe`
5. Select events:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `charge.refunded`
6. Copy the **Signing secret** (starts with `whsec_`)

### Step 2: Set Environment Variable

```bash
export STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
```

### Step 3: Test Webhook

Use Stripe CLI to test:

```bash
stripe listen --forward-to https://your-domain.com/api/payments/webhooks/stripe
stripe trigger payment_intent.succeeded
```

---

## 🏦 PayU Webhook Setup

### Step 1: Configure Webhook URL

1. Log in to [PayU Dashboard](https://dashboard.payu.in/)
2. Navigate to **Settings** → **Webhooks**
3. Add webhook URL: `https://your-domain.com/api/payments/webhooks/payu`
4. Select events:
   - Payment Success
   - Payment Failure
   - Refund Success

### Step 2: Set Merchant Salt

```bash
export PAYU_MERCHANT_SALT=your_merchant_salt_here
```

### Step 3: Test Webhook

```bash
curl -X POST https://your-domain.com/api/payments/webhooks/payu \
  -H "Content-Type: application/json" \
  -d '{
    "status": "success",
    "txnid": "TXN123456",
    "amount": "1000.00"
  }'
```

---

## 🧪 Webhook Testing

### Using Test Scripts

We provide test scripts for each gateway:

#### Test Razorpay Webhook:
```bash
./scripts/test-webhook-razorpay.sh
```

#### Test Stripe Webhook:
```bash
./scripts/test-webhook-stripe.sh
```

#### Test PayU Webhook:
```bash
./scripts/test-webhook-payu.sh
```

### Manual Testing

1. **Create a test payment** through the gateway dashboard
2. **Check webhook logs** in your application
3. **Verify payment status** was updated in database
4. **Check webhook signature** validation logs

---

## 🔒 Security Best Practices

### 1. Always Use HTTPS
- Webhooks must be served over HTTPS
- Use valid SSL certificates
- Enable TLS 1.2 or higher

### 2. Verify Signatures
- Always verify webhook signatures
- Never process unsigned webhooks
- Log all signature verification failures

### 3. Idempotency
- Handle duplicate webhook events
- Use transaction IDs for deduplication
- Store processed webhook IDs

### 4. Rate Limiting
- Implement rate limiting on webhook endpoints
- Prevent webhook spam
- Monitor webhook traffic

### 5. Logging
- Log all webhook events
- Store webhook payloads for audit
- Monitor webhook processing times

---

## 📊 Monitoring Webhooks

### Health Check Endpoint

```http
GET /actuator/health
```

### Webhook Statistics

```http
GET /api/payments/webhooks/stats
```

### Log Monitoring

Monitor webhook logs for:
- Signature verification failures
- Processing errors
- Response times
- Event types

---

## 🐛 Troubleshooting

### Common Issues

#### 1. Signature Verification Fails
**Solution:**
- Verify webhook secret is correct
- Check request body encoding
- Ensure signature header is present

#### 2. Webhook Not Received
**Solution:**
- Verify webhook URL is accessible
- Check firewall rules
- Verify gateway configuration

#### 3. Payment Status Not Updated
**Solution:**
- Check webhook processing logs
- Verify transaction ID mapping
- Check database connection

#### 4. Duplicate Webhooks
**Solution:**
- Implement idempotency checks
- Store processed webhook IDs
- Use database unique constraints

---

## 📝 Webhook Event Reference

### Razorpay Events
- `payment.captured` - Payment successfully captured
- `payment.authorized` - Payment authorized
- `payment.failed` - Payment failed
- `refund.created` - Refund initiated
- `refund.processed` - Refund completed

### Stripe Events
- `payment_intent.succeeded` - Payment succeeded
- `payment_intent.payment_failed` - Payment failed
- `charge.refunded` - Refund processed

### PayU Events
- `success` - Payment successful
- `failure` - Payment failed
- `refund` - Refund processed

---

## ✅ Verification Checklist

- [ ] Webhook URLs configured in gateway dashboards
- [ ] Webhook secrets set in environment variables
- [ ] HTTPS enabled for webhook endpoints
- [ ] Signature verification working
- [ ] Test webhooks received successfully
- [ ] Payment status updates working
- [ ] Logging configured
- [ ] Monitoring set up
- [ ] Error handling implemented
- [ ] Documentation updated

---

**Last Updated**: November 2025  
**Status**: ✅ Production Ready

