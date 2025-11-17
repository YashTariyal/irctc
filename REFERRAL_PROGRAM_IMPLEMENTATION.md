## Referral Program Implementation

### Goals
- Generate unique referral codes for every user
- Track invitations, rewards and leaderboards
- Allow bookings to credit referral rewards when a referred user travels

### User Service
1. **Schema**
   - Added `referral_code`, `referred_by_user_id`, `referral_points` columns to `simple_users`
   - Created `user_referrals` table for detailed referral events
2. **API**
   - `GET /api/users/{id}/referral-code`
   - `GET /api/users/{id}/referrals`
   - `GET /api/users/referral-leaderboard?limit=10`
   - `POST /api/users/referrals/reward`
3. **Logic**
   - `ReferralService` handles code generation, signup rewards, booking rewards and leaderboard aggregation
   - `MinimalUserController` now accepts `?referralCode=` during registration

### Booking Service
1. **Integration**
   - Added `ReferralServiceClient` (Feign) and `ReferralRewardService`
   - After a booking is confirmed, the service notifies user service to award bonus points

### Testing
- Added unit tests for referral service/controller in user service
- Added tests for referral reward integration in booking service

The referral program now supports end-to-end flows: code sharing, signup rewards, booking rewards and leaderboard visibility.

