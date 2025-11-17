## Biometric Authentication Implementation

### Scope
- Fingerprint & face registration per device.
- Verification endpoint for user-login/payment flows.
- Biometric payment authorization hook for Payment Service via REST client.

### User Service
1. **Database**
   - `V9__Create_biometric_credentials_table.sql` stores per-device biometric templates, status, audit timestamps and tenant metadata.
2. **Domain**
   - `BiometricCredential` entity + `BiometricCredentialRepository`.
3. **DTOs**
   - `BiometricRegistrationRequest/Response`
   - `BiometricVerificationRequest/Response`
4. **Service**
   - `BiometricAuthService` hashes templates (SHA-256), persists credentials, and validates verification tokens with per-request UUID verification IDs.
5. **API**
   - `BiometricAuthController`
     - `POST /api/auth/biometric/register`
     - `POST /api/auth/biometric/verify`
6. **Tests**
   - Service & controller level unit tests covering register/verify paths.

### Payment Service (Integration Point)
See `BiometricAuthorizationService` for `POST /api/payments/biometric-authorize`, which delegates verification to User Service using the new API and logs every authorization attempt.

