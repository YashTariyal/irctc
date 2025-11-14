# 🎫 Booking Modifications Feature - Implementation Guide

## 📋 Overview

The Booking Modifications feature allows users to modify their existing bookings without cancelling and rebooking. This includes:
- **Date Changes**: Change journey date (and optionally train)
- **Seat Upgrades/Downgrades**: Change seat class (AC, Sleeper, etc.)
- **Passenger Modifications**: Add or remove passengers from booking
- **Route Changes**: Change source/destination stations

---

## 🚀 Features Implemented

### 1. **Date Change Modification**
- Change journey date for existing booking
- Optionally change to a different train
- Automatic fare recalculation (when Train Service integration is added)
- Time-based modification charges

**API Endpoint**: `PUT /api/bookings/{id}/modify-date`

**Request Body**:
```json
{
  "newJourneyDate": "2025-12-25T10:00:00",
  "newTrainId": 123,  // Optional
  "reason": "Change of plans"  // Optional
}
```

### 2. **Seat Upgrade/Downgrade**
- Upgrade or downgrade seat class
- Automatic fare difference calculation
- Modification charges based on time until journey

**API Endpoint**: `PUT /api/bookings/{id}/upgrade-seat`

**Request Body**:
```json
{
  "newSeatClass": "2AC",
  "newFare": 2500.00,
  "newSeatNumber": "A1",  // Optional
  "reason": "Want better comfort"
}
```

### 3. **Passenger Modification**
- Add new passengers to booking
- Remove passengers from booking
- Automatic fare adjustment for passenger changes

**API Endpoint**: `PUT /api/bookings/{id}/modify-passengers`

**Request Body**:
```json
{
  "passengersToAdd": [
    {
      "name": "John Doe",
      "age": 30,
      "gender": "MALE",
      "seatNumber": "A2",
      "idProofType": "AADHAAR",
      "idProofNumber": "123456789012"
    }
  ],
  "passengerIdsToRemove": [5, 6],  // Optional
  "additionalFare": 1200.00,
  "reason": "Adding family member"
}
```

### 4. **Route Change**
- Change source and/or destination stations
- Optionally change to a different train
- Automatic fare recalculation

**API Endpoint**: `PUT /api/bookings/{id}/change-route`

**Request Body**:
```json
{
  "newSourceStation": "Mumbai Central",
  "newDestinationStation": "Delhi",
  "newTrainId": 456,  // Optional
  "newFare": 3000.00,
  "reason": "Change of destination"
}
```

### 5. **Modification Options**
- Get available modification options for a booking
- View modification charges before proceeding
- Check what modifications are allowed based on booking status and time

**API Endpoint**: `GET /api/bookings/{id}/modification-options`

**Response**:
```json
{
  "bookingId": 123,
  "currentStatus": "CONFIRMED",
  "canModifyDate": true,
  "canUpgradeSeat": true,
  "canChangeRoute": false,
  "canModifyPassengers": true,
  "modificationCharges": {
    "dateChange": 200.00,
    "seatUpgrade": 100.00,
    "routeChange": 300.00,
    "passengerModification": 150.00
  },
  "lastModificationDate": null,
  "modificationCount": 0
}
```

---

## 💰 Modification Charges

### Charge Calculation Rules

1. **Base Charges**:
   - Date Change: ₹200
   - Seat Upgrade: ₹100
   - Route Change: ₹300
   - Passenger Modification: ₹150 per passenger

2. **Time-based Multipliers**:
   - **Same day (< 24 hours)**: 2x base charge
   - **Within 48 hours**: 1.5x base charge
   - **Within 72 hours**: 1.2x base charge
   - **More than 72 hours**: Base charge

3. **Special Rules**:
   - Seat downgrade: 50% of base charge
   - Multiple modifications: Charges are cumulative

### Example Calculations

**Date Change (48 hours before journey)**:
- Base: ₹200
- Multiplier: 1.5x
- **Total: ₹300**

**Seat Upgrade (Same day)**:
- Base: ₹100
- Multiplier: 2x
- **Total: ₹200**

---

## ✅ Business Rules

### Modification Eligibility

1. **Booking Status**: Only `CONFIRMED` or `PENDING` bookings can be modified
2. **Time Restrictions**:
   - Date Change: Minimum 4 hours before journey
   - Seat Upgrade: Minimum 2 hours before journey
   - Route Change: Minimum 6 hours before journey
   - Passenger Modification: Minimum 4 hours before journey

3. **Validation**:
   - New journey date must be in the future
   - Cannot modify cancelled or completed bookings
   - Passenger IDs must exist in booking

---

## 📁 File Structure

```
irctc-booking-service/
├── src/main/java/com/irctc/booking/
│   ├── dto/
│   │   ├── BookingModificationRequest.java
│   │   ├── DateChangeRequest.java
│   │   ├── SeatUpgradeRequest.java
│   │   ├── PassengerModificationRequest.java
│   │   ├── RouteChangeRequest.java
│   │   ├── ModificationOptionsResponse.java
│   │   └── ModificationResponse.java
│   ├── service/
│   │   ├── BookingModificationService.java
│   │   └── ModificationChargeCalculator.java
│   └── controller/
│       └── SimpleBookingController.java (updated with modification endpoints)
```

---

## 🔧 Technical Implementation

### Key Components

1. **BookingModificationService**: 
   - Handles all modification business logic
   - Validates modification eligibility
   - Updates booking entities
   - Returns modification responses

2. **ModificationChargeCalculator**:
   - Calculates modification charges based on business rules
   - Time-based charge multipliers
   - Fare difference calculations

3. **DTOs**:
   - Request DTOs for each modification type
   - Response DTOs with modification details
   - Options response for available modifications

### Integration Points

- **SimpleBookingService**: Used to fetch and update bookings
- **TenantContext**: Multi-tenancy support
- **Cache Management**: Automatic cache invalidation on modifications
- **Audit Logging**: All modifications are audited via `@Auditable` annotation

---

## 🧪 Testing

### Test Scenarios

1. **Date Change**:
   - ✅ Valid date change (more than 4 hours before)
   - ✅ Invalid date change (less than 4 hours before)
   - ✅ Date change with train change
   - ✅ Past date validation

2. **Seat Upgrade**:
   - ✅ Valid seat upgrade
   - ✅ Seat downgrade
   - ✅ Same-day upgrade (higher charge)

3. **Passenger Modification**:
   - ✅ Add passengers
   - ✅ Remove passengers
   - ✅ Add and remove simultaneously
   - ✅ Invalid passenger ID validation

4. **Route Change**:
   - ✅ Valid route change
   - ✅ Route change with train change
   - ✅ Invalid route change (less than 6 hours)

5. **Modification Options**:
   - ✅ Get options for modifiable booking
   - ✅ Get options for non-modifiable booking
   - ✅ Charge calculation accuracy

### Example Test Request

```bash
# Get modification options
curl -X GET http://localhost:8093/api/bookings/1/modification-options

# Modify date
curl -X PUT http://localhost:8093/api/bookings/1/modify-date \
  -H "Content-Type: application/json" \
  -d '{
    "newJourneyDate": "2025-12-25T10:00:00",
    "reason": "Change of plans"
  }'

# Upgrade seat
curl -X PUT http://localhost:8093/api/bookings/1/upgrade-seat \
  -H "Content-Type: application/json" \
  -d '{
    "newSeatClass": "2AC",
    "newFare": 2500.00
  }'
```

---

## 🔮 Future Enhancements

1. **Train Service Integration**:
   - Fetch real-time fare from Train Service
   - Check seat availability before modification
   - Get alternative train options

2. **Payment Integration**:
   - Automatic payment processing for fare differences
   - Refund processing for downgrades
   - Payment gateway integration

3. **Notification Integration**:
   - Send modification confirmation emails/SMS
   - Notify about modification charges
   - Alert about refund processing

4. **Modification History**:
   - Track all modifications in database
   - View modification history
   - Modification audit trail

5. **Advanced Features**:
   - Partial modifications (modify only some passengers)
   - Modification scheduling (schedule modification for later)
   - Modification cancellation (undo modification)

---

## 📊 API Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/bookings/{id}/modification-options` | GET | Get available modification options |
| `/api/bookings/{id}/modify-date` | PUT | Change booking date |
| `/api/bookings/{id}/upgrade-seat` | PUT | Upgrade/downgrade seat class |
| `/api/bookings/{id}/modify-passengers` | PUT | Add/remove passengers |
| `/api/bookings/{id}/change-route` | PUT | Change source/destination |

---

## ✅ Status

- ✅ DTOs created
- ✅ ModificationChargeCalculator implemented
- ✅ BookingModificationService implemented
- ✅ Controller endpoints added
- ✅ Validation and error handling
- ✅ Multi-tenancy support
- ✅ Cache management
- ✅ Audit logging
- ⏳ Train Service integration (future)
- ⏳ Payment integration (future)
- ⏳ Notification integration (future)

---

## 🎯 Next Steps

1. **Integration Testing**: Test all modification endpoints
2. **Train Service Client**: Create Feign client for fare calculation
3. **Payment Service Integration**: Handle fare differences automatically
4. **Notification Service**: Send modification confirmations
5. **Database Migration**: Add modification history table (optional)

---

**Feature Status**: ✅ **IMPLEMENTED AND READY FOR TESTING**

