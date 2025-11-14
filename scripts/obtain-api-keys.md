# 🔑 Obtaining Payment Gateway API Keys

This guide provides step-by-step instructions for obtaining API keys from each payment gateway.

---

## 💳 Razorpay API Keys

### Step 1: Create Razorpay Account
1. Visit [https://razorpay.com](https://razorpay.com)
2. Click **Sign Up**
3. Complete registration with business details
4. Verify email and phone number

### Step 2: Complete KYC
1. Log in to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Navigate to **Settings** → **Account & Settings**
3. Complete KYC documentation:
   - Business PAN
   - Bank account details
   - Business address proof
4. Wait for verification (usually 24-48 hours)

### Step 3: Get API Keys
1. Go to **Settings** → **API Keys**
2. Click **Generate Test Key** (for testing)
3. Copy:
   - **Key ID** (starts with `rzp_test_` or `rzp_live_`)
   - **Key Secret** (shown only once - save it!)
4. For production, click **Generate Live Key** after KYC approval

### Step 4: Get Webhook Secret
1. Go to **Settings** → **Webhooks**
2. Click **Add New Webhook**
3. Enter webhook URL: `https://your-domain.com/api/payments/webhooks/razorpay`
4. Select events (see WEBHOOK_SETUP_GUIDE.md)
5. Copy **Webhook Secret** (starts with `whsec_`)

### API Key Format:
```
Key ID: rzp_live_xxxxxxxxxxxxx
Key Secret: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Webhook Secret: whsec_xxxxxxxxxxxxx
```

---

## 💳 Stripe API Keys

### Step 1: Create Stripe Account
1. Visit [https://stripe.com](https://stripe.com)
2. Click **Start now**
3. Complete registration
4. Verify email

### Step 2: Complete Business Details
1. Log in to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Complete business information:
   - Business type
   - Business address
   - Bank account details
3. Submit for verification

### Step 3: Get API Keys
1. Go to **Developers** → **API keys**
2. Toggle **Test mode** for test keys
3. Copy:
   - **Publishable key** (starts with `pk_test_` or `pk_live_`)
   - **Secret key** (starts with `sk_test_` or `sk_live_`)
4. Click **Reveal test key** to see secret key

### Step 4: Get Webhook Secret
1. Go to **Developers** → **Webhooks**
2. Click **Add endpoint**
3. Enter URL: `https://your-domain.com/api/payments/webhooks/stripe`
4. Select events:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `charge.refunded`
5. Copy **Signing secret** (starts with `whsec_`)

### API Key Format:
```
Publishable Key: pk_live_xxxxxxxxxxxxx
Secret Key: sk_live_xxxxxxxxxxxxx
Webhook Secret: whsec_xxxxxxxxxxxxx
```

---

## 💳 PayU API Keys

### Step 1: Create PayU Account
1. Visit [https://www.payu.in](https://www.payu.in)
2. Click **Sign Up**
3. Complete merchant registration
4. Verify email and phone

### Step 2: Complete Merchant Onboarding
1. Log in to [PayU Dashboard](https://dashboard.payu.in/)
2. Complete merchant details:
   - Business PAN
   - Bank account details
   - Business documents
3. Submit for approval

### Step 3: Get Merchant Credentials
1. Go to **Settings** → **Merchant Key & Salt**
2. Copy:
   - **Merchant Key**
   - **Merchant Salt** (shown only once - save it!)
3. These are used for both API calls and webhook verification

### Merchant Credentials Format:
```
Merchant Key: xxxxxxxxxxxxxxxx
Merchant Salt: xxxxxxxxxxxxxxxx
```

---

## 🔐 Security Best Practices

1. **Never commit keys to version control**
   - Use `.env` file (already in `.gitignore`)
   - Use secret management tools in production

2. **Use different keys for test and production**
   - Test keys for development
   - Live keys only in production

3. **Rotate keys regularly**
   - Change keys every 90 days
   - Update all services when rotating

4. **Restrict key permissions**
   - Use least privilege principle
   - Disable unused keys

5. **Monitor key usage**
   - Set up alerts for unusual activity
   - Review access logs regularly

---

## ✅ Verification Checklist

After obtaining keys, verify:

- [ ] Razorpay Key ID and Secret obtained
- [ ] Razorpay Webhook Secret obtained
- [ ] Stripe Publishable and Secret keys obtained
- [ ] Stripe Webhook Secret obtained
- [ ] PayU Merchant Key and Salt obtained
- [ ] All keys saved securely (password manager)
- [ ] Test keys verified in test environment
- [ ] Production keys ready for deployment

---

## 📝 Next Steps

After obtaining keys:
1. Add to `.env` file
2. Run `./scripts/setup-production.sh` to validate
3. Test with test keys first
4. Switch to production keys after testing

