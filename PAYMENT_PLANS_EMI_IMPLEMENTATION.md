## Payment Plans & EMI Implementation

### Goals
- Allow high-value bookings to be financed via installment plans.
- Track the repayment schedule, record EMI payments, and surface plan details over APIs.

### Payment Service Changes
1. **Database**
   - `V8__Create_payment_plans_and_emi_payments.sql` introduces:
     - `payment_plans` table storing booking/user, total amount, down payment, EMI amount, interest, status, etc.
     - `emi_payments` table storing installment schedule, due dates, status, penalties.
2. **Domain & Persistence**
   - Entities: `PaymentPlan`, `EmiPayment`.
   - Repositories: `PaymentPlanRepository`, `EmiPaymentRepository`.
3. **DTOs**
   - `PaymentPlanRequest`, `PaymentPlanResponse` (with embedded EMI summaries), `EmiPaymentRequest`, `EmiPaymentResponse`.
4. **Service**
   - `PaymentPlanService` handles plan creation, schedule generation, retrieval by ID/user, and EMI payment recording.
   - EMI amount calculation supports basic interest (using amortized formula) and optional down payment.
5. **API**
   - `PaymentPlanController` exposes:
     - `POST /api/payments/plans`
     - `GET /api/payments/plans/{id}`
     - `GET /api/payments/plans/user/{userId}`
     - `POST /api/payments/plans/emi/{emiId}/pay`

### Testing
- `PaymentPlanServiceTest` validates plan creation, retrieval, and EMI payment recording.
- `PaymentPlanControllerTest` ensures API responses and request handling work in isolation.

