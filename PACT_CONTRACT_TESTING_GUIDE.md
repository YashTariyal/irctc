# 🤝 Pact Contract Testing Implementation Guide

## 🎯 Overview

This guide explains how to use Pact for contract testing in IRCTC microservices. Contract testing ensures that service contracts are maintained and prevents breaking changes.

---

## 📋 What is Contract Testing?

Contract testing verifies that:
- **Consumers** (services that call other services) send requests in the expected format
- **Providers** (services that receive requests) respond in the expected format
- Contracts are maintained across service deployments

---

## 🏗️ Architecture

### Service Interactions

```
Booking Service (Consumer)
    ↓
    ├─→ Payment Service (Provider)
    └─→ User Service (Provider)
```

### Contracts Defined

1. **Booking Service → Payment Service**
   - Create Payment
   - Get Payment by ID
   - Get Payments by Booking ID

2. **Booking Service → User Service**
   - Get User by ID
   - Get User by ID (Not Found)

---

## 🚀 Quick Start

### 1. Run Consumer Tests (Generate Contracts)

```bash
# In booking-service directory
cd irctc-booking-service
../mvnw test -Dtest=PaymentServiceContractTest
../mvnw test -Dtest=UserServiceContractTest
```

This generates Pact files in `target/pacts/` directory.

### 2. Copy Contracts to Providers

```bash
# Copy contracts to payment service
cp target/pacts/booking-service-payment-service.json \
   ../irctc-payment-service/src/test/resources/pacts/

# Copy contracts to user service
cp target/pacts/booking-service-user-service.json \
   ../irctc-user-service/src/test/resources/pacts/
```

### 3. Run Provider Verification Tests

```bash
# In payment-service directory
cd irctc-payment-service
../mvnw test -Dtest=PaymentServiceProviderTest

# In user-service directory
cd irctc-user-service
../mvnw test -Dtest=UserServiceProviderTest
```

---

## 📁 Project Structure

```
irctc-booking-service/
  src/test/java/com/irctc/booking/contract/
    ├── PaymentServiceContractTest.java    # Consumer test
    └── UserServiceContractTest.java       # Consumer test
  target/pacts/
    ├── booking-service-payment-service.json
    └── booking-service-user-service.json

irctc-payment-service/
  src/test/java/com/irctc/payment/contract/
    └── PaymentServiceProviderTest.java   # Provider verification
  src/test/resources/pacts/
    └── booking-service-payment-service.json

irctc-user-service/
  src/test/java/com/irctc/user/contract/
    └── UserServiceProviderTest.java       # Provider verification
  src/test/resources/pacts/
    └── booking-service-user-service.json
```

---

## 🔧 Configuration

### Maven Dependencies

Both consumer and provider services need Pact dependencies:

```xml
<!-- Consumer -->
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.2</version>
    <scope>test</scope>
</dependency>

<!-- Provider -->
<dependency>
    <groupId>au.com.dius.pact.provider</groupId>
    <artifactId>junit5</artifactId>
    <version>4.6.2</version>
    <scope>test</scope>
</dependency>
```

---

## 📝 Writing Consumer Tests

### Example: Payment Service Contract

```java
@ExtendWith(PactConsumerTestExt.class)
public class PaymentServiceContractTest {

    @Pact(consumer = "booking-service", provider = "payment-service")
    public RequestResponsePact createPaymentPact(PactDslWithProvider builder) {
        return builder
            .given("payment service is available")
            .uponReceiving("a request to create payment")
            .path("/api/payments")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body("""
                {
                  "bookingId": 123,
                  "amount": 1000.0,
                  "currency": "INR",
                  "paymentMethod": "CREDIT_CARD"
                }
                """)
            .willRespondWith()
            .status(200)
            .body("""
                {
                  "id": 1,
                  "bookingId": 123,
                  "amount": 1000.0,
                  "status": "SUCCESS",
                  "transactionId": "TXN123456789"
                }
                """)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPaymentPact")
    void testCreatePayment(MockServer mockServer) {
        // Test implementation
    }
}
```

---

## ✅ Writing Provider Verification Tests

### Example: Payment Service Provider

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("payment-service")
@PactFolder("pacts")
public class PaymentServiceProviderTest {

    @State("payment service is available")
    void paymentServiceAvailable() {
        // Setup test data
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
```

---

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Contract Tests

on: [push, pull_request]

jobs:
  consumer-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Consumer Tests
        run: |
          cd irctc-booking-service
          mvn test -Dtest=*ContractTest
      - name: Upload Pact Files
        uses: actions/upload-artifact@v3
        with:
          name: pacts
          path: irctc-booking-service/target/pacts/*.json

  provider-tests:
    needs: consumer-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Download Pact Files
        uses: actions/download-artifact@v3
        with:
          name: pacts
      - name: Copy to Provider Services
        run: |
          cp booking-service-payment-service.json \
             irctc-payment-service/src/test/resources/pacts/
          cp booking-service-user-service.json \
             irctc-user-service/src/test/resources/pacts/
      - name: Run Provider Tests
        run: |
          cd irctc-payment-service && mvn test -Dtest=*ProviderTest
          cd ../irctc-user-service && mvn test -Dtest=*ProviderTest
```

---

## 🎯 Best Practices

### 1. **Keep Contracts Simple**
- Focus on essential fields
- Don't include internal implementation details
- Use matchers for flexible matching

### 2. **Version Contracts**
- Use semantic versioning for contracts
- Document breaking changes
- Maintain backward compatibility when possible

### 3. **Test States**
- Use `@State` annotations to set up test data
- Keep states minimal and focused
- Clean up after tests

### 4. **Run Tests Regularly**
- Run consumer tests on every commit
- Run provider tests before deployment
- Fail builds on contract violations

---

## 🐛 Troubleshooting

### Contract Mismatch

**Error**: `Response body does not match`

**Solution**:
1. Check if provider response matches contract
2. Verify field names and types
3. Update contract if provider changed intentionally

### Missing State

**Error**: `State 'payment service is available' not found`

**Solution**:
1. Ensure `@State` method exists in provider test
2. Check method name matches exactly
3. Verify state setup logic

### Pact File Not Found

**Error**: `Pact file not found`

**Solution**:
1. Verify Pact files are in correct location
2. Check `@PactFolder` annotation path
3. Ensure contracts are copied from consumer

---

## 📊 Benefits

1. **Early Detection**
   - Catch breaking changes before deployment
   - Prevent production issues

2. **Documentation**
   - Contracts serve as API documentation
   - Clear expectations between services

3. **Independent Development**
   - Teams can work independently
   - Contracts ensure compatibility

4. **Confidence**
   - Deploy with confidence
   - Know services will work together

---

## 🔄 Workflow

### Development Workflow

1. **Consumer Team**:
   - Write consumer contract test
   - Generate Pact file
   - Commit to repository

2. **Provider Team**:
   - Download Pact file
   - Write provider verification test
   - Fix any contract violations
   - Deploy provider

3. **CI/CD Pipeline**:
   - Run consumer tests
   - Generate Pact files
   - Run provider verification
   - Block deployment on failures

---

## 📚 Additional Resources

- [Pact Documentation](https://docs.pact.io/)
- [Pact JVM](https://github.com/DiUS/pact-jvm)
- [Contract Testing Best Practices](https://docs.pact.io/best_practices)

---

## 🎯 Next Steps

1. **Add More Contracts**
   - Train Service contracts
   - Notification Service contracts

2. **Pact Broker Integration**
   - Centralized contract storage
   - Version management
   - Webhook notifications

3. **Automated Contract Publishing**
   - Publish contracts on merge
   - Notify teams of changes
   - Track contract versions

---

*Last Updated: November 2025*

