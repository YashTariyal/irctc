# Setup Data for IRCTC Booking

## 🚨 **Error Resolution**

The error you encountered indicates that the passenger with ID 1 doesn't exist in the database. You need to create the necessary entities first before creating a booking.

**Error:** `Key (passenger_id)=(1) is not present in table "passengers"`

## 📋 **Required Entities for Booking**

To create a booking, you need these entities to exist first:
1. **User** (who is making the booking)
2. **Train** (which train to book)
3. **Passenger** (who is traveling)
4. **Coach** (which coach/compartment)
5. **Seat** (optional - specific seat)

---

## 👤 **1. Create User**

**Endpoint:** `POST /api/users/register`

```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1990-05-15T00:00:00",
  "gender": "MALE",
  "idProofType": "AADHAR",
  "idProofNumber": "123456789012",
  "isVerified": true,
  "isActive": true,
  "role": "USER"
}
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1990-05-15T00:00:00",
  "gender": "MALE",
  "idProofType": "AADHAR",
  "idProofNumber": "123456789012",
  "isVerified": true,
  "isActive": true,
  "role": "USER",
  "createdAt": "2024-12-20T10:30:00",
  "updatedAt": "2024-12-20T10:30:00"
}
```

---

## 🚂 **2. Create Train**

**Endpoint:** `POST /api/trains`

```json
{
  "trainNumber": "12345",
  "trainName": "Rajdhani Express",
  "sourceStation": {
    "id": 1
  },
  "destinationStation": {
    "id": 2
  },
  "departureTime": "08:00:00",
  "arrivalTime": "18:00:00",
  "journeyDuration": 600,
  "totalDistance": 800.0,
  "trainType": "RAJDHANI",
  "status": "ACTIVE",
  "isRunning": true
}
```

**Note:** You'll need to create stations first. Here's a station example:

**Create Station (if needed):**
```json
{
  "stationCode": "NDLS",
  "stationName": "New Delhi",
  "city": "Delhi",
  "state": "Delhi",
  "zone": "Northern Railway"
}
```

---

## 👥 **3. Create Passenger**

**Endpoint:** `POST /api/passengers`

```json
{
  "user": {
    "id": 1
  },
  "firstName": "John",
  "lastName": "Doe",
  "age": 34,
  "gender": "MALE",
  "passengerType": "ADULT",
  "idProofType": "AADHAR",
  "idProofNumber": "123456789012",
  "isSeniorCitizen": false,
  "isLadiesQuota": false,
  "isHandicapped": false
}
```

**Response:**
```json
{
  "id": 1,
  "user": { "id": 1 },
  "firstName": "John",
  "lastName": "Doe",
  "age": 34,
  "gender": "MALE",
  "passengerType": "ADULT",
  "idProofType": "AADHAR",
  "idProofNumber": "123456789012",
  "isSeniorCitizen": false,
  "isLadiesQuota": false,
  "isHandicapped": false,
  "createdAt": "2024-12-20T10:30:00",
  "updatedAt": "2024-12-20T10:30:00"
}
```

---

## 🚇 **4. Create Coach**

**Endpoint:** `POST /api/coaches`

```json
{
  "train": {
    "id": 1
  },
  "coachNumber": "A1",
  "coachType": "AC_FIRST_CLASS",
  "totalSeats": 24,
  "availableSeats": 20,
  "farePerSeat": 2500.00,
  "isActive": true
}
```

---

## 💺 **5. Create Seat (Optional)**

**Endpoint:** `POST /api/seats`

```json
{
  "coach": {
    "id": 1
  },
  "seatNumber": "A1-1",
  "seatType": "LOWER_BERTH",
  "isAvailable": true,
  "isReserved": false,
  "fare": 2500.00
}
```

---

## 🚀 **Complete Setup Script**

Here's a complete script to set up all required data:

### **Step 1: Create User**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "9876543210",
    "gender": "MALE",
    "idProofType": "AADHAR",
    "idProofNumber": "123456789012",
    "isVerified": true,
    "isActive": true,
    "role": "USER"
  }'
```

### **Step 2: Create Train**
```bash
curl -X POST http://localhost:8080/api/trains \
  -H "Content-Type: application/json" \
  -d '{
    "trainNumber": "12345",
    "trainName": "Rajdhani Express",
    "sourceStation": { "id": 1 },
    "destinationStation": { "id": 2 },
    "departureTime": "08:00:00",
    "arrivalTime": "18:00:00",
    "journeyDuration": 600,
    "totalDistance": 800.0,
    "trainType": "RAJDHANI",
    "status": "ACTIVE",
    "isRunning": true
  }'
```

### **Step 3: Create Passenger**
```bash
curl -X POST http://localhost:8080/api/passengers \
  -H "Content-Type: application/json" \
  -d '{
    "user": { "id": 1 },
    "firstName": "John",
    "lastName": "Doe",
    "age": 34,
    "gender": "MALE",
    "passengerType": "ADULT",
    "idProofType": "AADHAR",
    "idProofNumber": "123456789012",
    "isSeniorCitizen": false,
    "isLadiesQuota": false,
    "isHandicapped": false
  }'
```

### **Step 4: Create Coach**
```bash
curl -X POST http://localhost:8080/api/coaches \
  -H "Content-Type: application/json" \
  -d '{
    "train": { "id": 1 },
    "coachNumber": "A1",
    "coachType": "AC_FIRST_CLASS",
    "totalSeats": 24,
    "availableSeats": 20,
    "farePerSeat": 2500.00,
    "isActive": true
  }'
```

### **Step 5: Create Seat (Optional)**
```bash
curl -X POST http://localhost:8080/api/seats \
  -H "Content-Type: application/json" \
  -d '{
    "coach": { "id": 1 },
    "seatNumber": "A1-1",
    "seatType": "LOWER_BERTH",
    "isAvailable": true,
    "isReserved": false,
    "fare": 2500.00
  }'
```

---

## ✅ **Now Create Booking**

After setting up all the required entities, you can create a booking:

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "user": { "id": 1 },
    "train": { "id": 1 },
    "passenger": { "id": 1 },
    "coach": { "id": 1 },
    "seat": { "id": 1 },
    "journeyDate": "2024-12-25",
    "totalFare": 2500.00,
    "status": "CONFIRMED",
    "paymentStatus": "PENDING"
  }'
```

---

## 🔍 **Verify Data Exists**

Before creating a booking, verify that all required entities exist:

### **Check User**
```bash
curl http://localhost:8080/api/users/1
```

### **Check Train**
```bash
curl http://localhost:8080/api/trains/1
```

### **Check Passenger**
```bash
curl http://localhost:8080/api/passengers/1
```

### **Check Coach**
```bash
curl http://localhost:8080/api/coaches/1
```

---

## 🎯 **Quick Setup Summary**

1. **Create User** → Get user ID
2. **Create Train** → Get train ID  
3. **Create Passenger** (with user ID) → Get passenger ID
4. **Create Coach** (with train ID) → Get coach ID
5. **Create Seat** (with coach ID) → Get seat ID
6. **Create Booking** (with all IDs)

This ensures all foreign key constraints are satisfied and your booking will be created successfully!
