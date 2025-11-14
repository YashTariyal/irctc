# 💳 Multiple Payment Gateways Implementation

## Overview

This document describes the implementation of the **Multiple Payment Gateways** feature for the IRCTC Payment Service. This feature enables support for multiple payment gateways (Razorpay, Stripe, PayU) with automatic gateway selection, fallback mechanisms, and statistics tracking.

---

## 🎯 Features Implemented

### 1. **Payment Gateway Abstraction**
- Created `PaymentGateway` interface for gateway abstraction
- Supports multiple gateway implementations
- Standardized payment and refund processing

### 2. **Gateway Implementations**
- **RazorpayGateway**: Indian payment gateway (2% fee, supports INR, USD, EUR)
- **StripeGateway**: International payment gateway (2.9% + $0.30 fee, supports USD, EUR, GBP, INR)
- **PayUGateway**: Indian payment gateway (1.8% fee, supports INR)

### 3. **Automatic Gateway Selection**
- `GatewaySelectorService` automatically selects the best gateway based on:
  - Success rate (70% weight)
  - Transaction fees (30% weight)
  - Currency support
  - Payment method support
  - User preference (if provided)

### 4. **Fallback Mechanism**
- Automatic fallback to alternative gateway if primary gateway fails
- Circuit breaker protection for gateway failures
- Graceful degradation to internal processing if all gateways fail

### 5. **Statistics Tracking**
- `GatewayStatisticsService` tracks:
  - Total transactions
  - Success/failure rates
  - Total amount processed
  - Total fees collected
  - Average fees per transaction

### 6. **Gateway Management APIs**
- `GET /api/payments/gateways` - List all available gateways
- `GET /api/payments/gateways/stats` - Get statistics for all gateways
- `GET /api/payments/gateways/stats/{gatewayName}` - Get statistics for specific gateway
- `GET /api/payments/gateways/compare` - Compare gateways (fees and success rates)

---

## 📁 Files Created

### Core Gateway Components
1. **`PaymentGateway.java`** - Interface for payment gateway abstraction
2. **`RazorpayGateway.java`** - Razorpay implementation
3. **`StripeGateway.java`** - Stripe implementation
4. **`PayUGateway.java`** - PayU implementation

### Services
5. **`GatewaySelectorService.java`** - Automatic gateway selection logic
6. **`GatewayStatisticsService.java`** - Statistics tracking and management

### DTOs
7. **`PaymentRequest.java`** - Payment request DTO
8. **`PaymentResponse.java`** - Payment response DTO
9. **`RefundRequest.java`** - Refund request DTO
10. **`RefundResponse.java`** - Refund response DTO

### Entities & Repositories
11. **`GatewayStatistics.java`** - Entity for gateway statistics
12. **`GatewayStatisticsRepository.java`** - Repository for statistics

### Controllers
13. **`GatewayController.java`** - Gateway management endpoints

### Database
14. **`V5__Add_gateway_fields.sql`** - Database migration for gateway fields

---

## 📝 Files Modified

### Updated Files
1. **`SimplePayment.java`** - Added gateway fields:
   - `gatewayName` - Name of the gateway used
   - `gatewayTransactionId` - Gateway's transaction ID
   - `gatewayFee` - Fee charged by gateway

2. **`SimplePaymentService.java`** - Updated to:
   - Use gateway abstraction for payment processing
   - Implement automatic gateway selection
   - Add fallback mechanism
   - Track gateway statistics
   - Process refunds through original gateway

3. **`SimplePaymentController.java`** - Updated to:
   - Accept `gateway` parameter for preferred gateway
   - Use `processPaymentWithGateway` method

---

## 🔧 Configuration

### Application Properties

Add the following configuration to `application.yml`:

```yaml
payment:
  gateway:
    razorpay:
      enabled: true
      key-id: ${RAZORPAY_KEY_ID:}
      key-secret: ${RAZORPAY_KEY_SECRET:}
      fee-percentage: 2.0
      fixed-fee: 0.0
    stripe:
      enabled: false
      secret-key: ${STRIPE_SECRET_KEY:}
      publishable-key: ${STRIPE_PUBLISHABLE_KEY:}
      fee-percentage: 2.9
      fixed-fee: 0.30
    payu:
      enabled: false
      merchant-key: ${PAYU_MERCHANT_KEY:}
      merchant-salt: ${PAYU_MERCHANT_SALT:}
      fee-percentage: 1.8
      fixed-fee: 0.0
```

---

## 🚀 API Usage

### Process Payment with Auto-Selected Gateway

```http
POST /api/payments
Content-Type: application/json

{
  "bookingId": 123,
  "amount": 1000.00,
  "currency": "INR",
  "paymentMethod": "CARD"
}
```

### Process Payment with Preferred Gateway

```http
POST /api/payments?gateway=RAZORPAY
Content-Type: application/json

{
  "bookingId": 123,
  "amount": 1000.00,
  "currency": "INR",
  "paymentMethod": "CARD"
}
```

### Get Available Gateways

```http
GET /api/payments/gateways
```

Response:
```json
[
  {
    "name": "RAZORPAY",
    "enabled": true,
    "feePercentage": 2.0,
    "fixedFee": 0.0,
    "successRate": 90.5,
    "totalTransactions": 1500,
    "averageFee": 20.00
  },
  {
    "name": "STRIPE",
    "enabled": true,
    "feePercentage": 2.9,
    "fixedFee": 0.30,
    "successRate": 85.2,
    "totalTransactions": 800,
    "averageFee": 29.30
  }
]
```

### Get Gateway Statistics

```http
GET /api/payments/gateways/stats/RAZORPAY
```

### Compare Gateways

```http
GET /api/payments/gateways/compare?currency=INR&paymentMethod=CARD
```

---

## 🔄 Gateway Selection Logic

The `GatewaySelectorService` uses the following criteria to select the best gateway:

1. **Gateway must be enabled**
2. **Gateway must support the currency**
3. **Gateway must support the payment method**
4. **User preference** (if provided)
5. **Success rate** (70% weight)
6. **Transaction fees** (30% weight)

**Selection Score Formula:**
```
Score = (Success Rate × 0.7) + (Fee Score × 0.3)
```

Where Fee Score is normalized to 0-100 scale based on fee percentage.

---

## 🔁 Fallback Mechanism

### Primary Gateway Failure
1. If primary gateway fails, system automatically tries fallback gateway
2. Fallback gateway is selected from available gateways (excluding failed one)
3. Statistics are updated for both primary and fallback attempts

### All Gateways Fail
1. If all gateways fail, system falls back to internal processing
2. Payment is marked with `gatewayName = "INTERNAL"`
3. No gateway fees are charged

### Circuit Breaker
- Circuit breaker protects against cascading failures
- If circuit is open, payment falls back to internal processing
- Payment is marked with `gatewayName = "FALLBACK"`

---

## 📊 Statistics Tracking

### Metrics Tracked
- **Total Transactions**: Total number of payment attempts
- **Successful Transactions**: Number of successful payments
- **Failed Transactions**: Number of failed payments
- **Success Rate**: Percentage of successful transactions
- **Total Amount**: Sum of all successful payment amounts
- **Total Fees**: Sum of all gateway fees
- **Average Fee**: Average fee per successful transaction

### Statistics Updates
- Statistics are updated in real-time after each payment attempt
- Success/failure is recorded immediately
- Statistics are cached for performance

---

## 🧪 Testing

### Unit Tests (To Be Created)
- Gateway selection logic
- Fallback mechanism
- Statistics tracking
- Gateway implementations

### Integration Tests (To Be Created)
- End-to-end payment processing
- Gateway selection with multiple gateways
- Fallback scenarios
- Statistics API endpoints

---

## 🔐 Security Considerations

1. **API Keys**: Gateway credentials stored in environment variables
2. **Transaction IDs**: Unique transaction IDs for tracking
3. **Gateway Response**: Gateway responses stored for audit
4. **Tenant Isolation**: Statistics are tenant-aware

---

## 📈 Performance Optimizations

1. **Caching**: Gateway statistics are cached
2. **Circuit Breaker**: Prevents cascading failures
3. **Bulkhead**: Isolates payment processing
4. **Time Limiter**: Prevents long-running operations

---

## 🐛 Error Handling

### Gateway Errors
- Gateway failures are logged
- Automatic fallback to alternative gateway
- Error details stored in payment response

### Validation Errors
- Currency validation
- Payment method validation
- Amount validation

---

## 🔮 Future Enhancements

1. **Real Gateway Integration**: Replace simulation with actual gateway SDKs
2. **Webhook Support**: Handle gateway webhooks for payment status updates
3. **Payment Links**: Generate payment links for deferred payments
4. **Gateway Health Monitoring**: Monitor gateway availability
5. **Dynamic Fee Calculation**: Adjust fees based on transaction volume
6. **Multi-Currency Support**: Enhanced currency conversion
7. **Gateway Analytics Dashboard**: Visual analytics for gateway performance

---

## ✅ Implementation Status

- [x] Payment Gateway Interface
- [x] Razorpay Gateway Implementation
- [x] Stripe Gateway Implementation
- [x] PayU Gateway Implementation
- [x] Gateway Selector Service
- [x] Statistics Service
- [x] Database Migration
- [x] Controller Endpoints
- [x] Fallback Mechanism
- [x] Circuit Breaker Integration
- [ ] Unit Tests
- [ ] Integration Tests
- [ ] Documentation

---

## 📚 References

- [Razorpay Documentation](https://razorpay.com/docs/)
- [Stripe Documentation](https://stripe.com/docs)
- [PayU Documentation](https://devguide.payu.in/)

---

**Implementation Date**: November 2025  
**Status**: ✅ Core Implementation Complete  
**Next Steps**: Add comprehensive test coverage

