# IRCTC Backend API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
The API uses basic authentication for most endpoints. User registration and login endpoints are publicly accessible.

## API Endpoints

### 1. User Management

#### Register User
```
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "gender": "MALE",
  "idProofType": "AADHAR",
  "idProofNumber": "123456789012"
}
```

#### Login User
```
POST /api/users/login?username=john_doe&password=password123
```

#### Get All Users
```
GET /api/users
Authorization: Basic <credentials>
```

#### Get User by ID
```
GET /api/users/{id}
Authorization: Basic <credentials>
```

#### Get User by Username
```
GET /api/users/username/{username}
Authorization: Basic <credentials>
```

#### Get User by Email
```
GET /api/users/email/{email}
Authorization: Basic <credentials>
```

#### Search Users by Name
```
GET /api/users/search/name?name=john
Authorization: Basic <credentials>
```

#### Update User
```
PUT /api/users/{id}
Authorization: Basic <credentials>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "9876543211",
  "gender": "MALE"
}
```

#### Update User Role
```
PUT /api/users/{id}/role?role=ADMIN
Authorization: Basic <credentials>
```

#### Verify User
```
PUT /api/users/{id}/verify
Authorization: Basic <credentials>
```

#### Activate/Deactivate User
```
PUT /api/users/{id}/activate
PUT /api/users/{id}/deactivate
Authorization: Basic <credentials>
```

#### Delete User
```
DELETE /api/users/{id}
Authorization: Basic <credentials>
```

### 2. Train Management

#### Create Train
```
POST /api/trains
Authorization: Basic <credentials>
Content-Type: application/json

{
  "trainNumber": "12345",
  "trainName": "Rajdhani Express",
  "sourceStation": {
    "id": 1
  },
  "destinationStation": {
    "id": 2
  },
  "departureTime": "16:00:00",
  "arrivalTime": "08:30:00",
  "trainType": "RAJDHANI",
  "totalDistance": 1500.0
}
```

#### Get All Trains
```
GET /api/trains
Authorization: Basic <credentials>
```

#### Get Active Trains
```
GET /api/trains/active
Authorization: Basic <credentials>
```

#### Get Train by ID
```
GET /api/trains/{id}
Authorization: Basic <credentials>
```

#### Get Train by Number
```
GET /api/trains/number/{trainNumber}
Authorization: Basic <credentials>
```

#### Search Trains
```
GET /api/trains/search?searchTerm=rajdhani
Authorization: Basic <credentials>
```

#### Get Trains by Type
```
GET /api/trains/type/{trainType}
Authorization: Basic <credentials>
```

#### Get Trains Between Stations
```
GET /api/trains/route?sourceStationCode=NDLS&destStationCode=BCT
Authorization: Basic <credentials>
```

#### Get Trains Between Cities
```
GET /api/trains/route/city?sourceCity=Delhi&destCity=Mumbai
Authorization: Basic <credentials>
```

#### Get Trains Between States
```
GET /api/trains/route/state?sourceState=Delhi&destState=Maharashtra
Authorization: Basic <credentials>
```

#### Update Train
```
PUT /api/trains/{id}
Authorization: Basic <credentials>
Content-Type: application/json

{
  "trainName": "Updated Train Name",
  "departureTime": "17:00:00",
  "arrivalTime": "09:30:00"
}
```

#### Update Train Status
```
PUT /api/trains/{id}/status?status=ACTIVE
Authorization: Basic <credentials>
```

#### Update Train Running Status
```
PUT /api/trains/{id}/running?isRunning=true
Authorization: Basic <credentials>
```

#### Delete Train
```
DELETE /api/trains/{id}
Authorization: Basic <credentials>
```

### 3. Booking Management

#### Create Booking
```
POST /api/bookings
Authorization: Basic <credentials>
Content-Type: application/json

{
  "user": {
    "id": 1
  },
  "train": {
    "id": 1
  },
  "passenger": {
    "id": 1
  },
  "coach": {
    "id": 1
  },
  "seat": {
    "id": 1
  },
  "journeyDate": "2024-01-15",
  "isTatkal": false,
  "quotaType": "GENERAL"
}
```

#### Get All Bookings
```
GET /api/bookings
Authorization: Basic <credentials>
```

#### Get Booking by ID
```
GET /api/bookings/{id}
Authorization: Basic <credentials>
```

#### Get Booking by PNR
```
GET /api/bookings/pnr/{pnrNumber}
Authorization: Basic <credentials>
```

#### Get Bookings by User
```
GET /api/bookings/user/{userId}
Authorization: Basic <credentials>
```

#### Get Upcoming Bookings by User
```
GET /api/bookings/user/{userId}/upcoming
Authorization: Basic <credentials>
```

#### Get Past Bookings by User
```
GET /api/bookings/user/{userId}/past
Authorization: Basic <credentials>
```

#### Get Confirmed Upcoming Bookings by User
```
GET /api/bookings/user/{userId}/confirmed
Authorization: Basic <credentials>
```

#### Get Bookings by Journey Date
```
GET /api/bookings/date/{journeyDate}
Authorization: Basic <credentials>
```

#### Get Bookings by Status
```
GET /api/bookings/status/{status}
Authorization: Basic <credentials>
```

#### Search Bookings by PNR
```
GET /api/bookings/search/pnr?pnr=1234567890
Authorization: Basic <credentials>
```

#### Update Booking Status
```
PUT /api/bookings/{id}/status?status=CONFIRMED
Authorization: Basic <credentials>
```

#### Update Payment Status
```
PUT /api/bookings/{id}/payment-status?paymentStatus=COMPLETED
Authorization: Basic <credentials>
```

#### Cancel Booking
```
PUT /api/bookings/{id}/cancel
Authorization: Basic <credentials>
```

## Sample Data

The system comes with pre-loaded sample data:

### Users
- **Admin**: username: `admin`, password: `admin123`
- **John Doe**: username: `john_doe`, password: `password123`
- **Jane Smith**: username: `jane_smith`, password: `password123`

### Stations
- NDLS - New Delhi
- BCT - Mumbai Central
- SBC - Bangalore City
- MAS - Chennai Central
- HWH - Howrah Junction

### Trains
- 12345 - Rajdhani Express (New Delhi → Mumbai)
- 12019 - Shatabdi Express (New Delhi → Bangalore)
- 12213 - Duronto Express (Mumbai → Chennai)

### Coaches
- AC First Class (A1) - 20 seats
- AC 2 Tier (B1) - 50 seats
- Sleeper Class (S1) - 72 seats

## Response Formats

### Success Response
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00"
}
```

### Error Response
```json
{
  "error": "User not found"
}
```

## Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `404` - Not Found
- `500` - Internal Server Error

## Testing the API

### Using curl

#### Register a new user:
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "9876543213"
  }'
```

#### Login:
```bash
curl -X POST "http://localhost:8080/api/users/login?username=testuser&password=password123"
```

#### Get all trains:
```bash
curl -X GET http://localhost:8080/api/trains \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Using Postman

1. Import the collection
2. Set the base URL to `http://localhost:8080/api`
3. Use the provided sample data for testing
4. Set Authorization header for protected endpoints

## Notes

- All timestamps are in ISO 8601 format
- All monetary values are in Indian Rupees (INR)
- PNR numbers are 10-digit unique identifiers
- Train numbers follow Indian Railways format
- Station codes are 3-4 letter codes 