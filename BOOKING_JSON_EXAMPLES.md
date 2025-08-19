# IRCTC Booking JSON Examples

## 🚂 **Create Booking API**

**Endpoint:** `POST /api/bookings`

**Content-Type:** `application/json`

---

## 📋 **Complete Booking JSON Example**

```json
{
  "pnrNumber": "PNR123456789",
  "user": {
    "id": 1
  },
  "train": {
    "id": 1
  },
  "passenger": {
    "id": 1
  },
  "seat": {
    "id": 1
  },
  "coach": {
    "id": 1
  },
  "journeyDate": "2024-12-25",
  "bookingDate": "2024-12-20T10:30:00",
  "totalFare": 1250.00,
  "baseFare": 1000.00,
  "tatkalFare": 200.00,
  "convenienceFee": 30.00,
  "gstAmount": 20.00,
  "status": "CONFIRMED",
  "paymentStatus": "PENDING",
  "quotaType": "GENERAL",
  "isTatkal": false,
  "isCancelled": false,
  "bookingSource": "WEB"
}
```

---

## 🎯 **Minimal Required Fields**

```json
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
  "journeyDate": "2024-12-25",
  "totalFare": 1250.00
}
```

---

## 📝 **Field Descriptions**

### **Required Fields**
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `user.id` | Long | User ID making the booking | `1` |
| `train.id` | Long | Train ID for the journey | `1` |
| `passenger.id` | Long | Passenger ID traveling | `1` |
| `coach.id` | Long | Coach ID for seating | `1` |
| `journeyDate` | String | Journey date (YYYY-MM-DD) | `"2024-12-25"` |
| `totalFare` | BigDecimal | Total fare amount | `1250.00` |

### **Optional Fields**
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `pnrNumber` | String | PNR number (auto-generated if not provided) | `"PNR123456789"` |
| `seat.id` | Long | Specific seat ID | `1` |
| `bookingDate` | String | Booking date and time | `"2024-12-20T10:30:00"` |
| `baseFare` | BigDecimal | Base fare amount | `1000.00` |
| `tatkalFare` | BigDecimal | Tatkal fare amount | `200.00` |
| `convenienceFee` | BigDecimal | Convenience fee | `30.00` |
| `gstAmount` | BigDecimal | GST amount | `20.00` |
| `status` | String | Booking status | `"CONFIRMED"` |
| `paymentStatus` | String | Payment status | `"PENDING"` |
| `quotaType` | String | Quota type | `"GENERAL"` |
| `isTatkal` | Boolean | Is Tatkal booking | `false` |
| `isCancelled` | Boolean | Is cancelled | `false` |
| `bookingSource` | String | Booking source | `"WEB"` |

---

## 🎨 **Enum Values**

### **Booking Status**
- `CONFIRMED`
- `WAITLIST`
- `RAC`
- `CANCELLED`
- `COMPLETED`

### **Payment Status**
- `PENDING`
- `COMPLETED`
- `FAILED`
- `REFUNDED`
- `PARTIALLY_REFUNDED`

### **Quota Type**
- `GENERAL`
- `LADIES`
- `SENIOR_CITIZEN`
- `HANDICAPPED`
- `TATKAL`
- `PREMIUM_TATKAL`

### **Booking Source**
- `WEB`
- `MOBILE_APP`
- `COUNTER`
- `AGENT`

---

## 🔧 **Different Booking Scenarios**

### **1. General Quota Booking**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1250.00,
  "quotaType": "GENERAL",
  "status": "CONFIRMED",
  "paymentStatus": "PENDING"
}
```

### **2. Tatkal Booking**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1450.00,
  "baseFare": 1000.00,
  "tatkalFare": 200.00,
  "convenienceFee": 30.00,
  "gstAmount": 20.00,
  "quotaType": "TATKAL",
  "isTatkal": true,
  "status": "CONFIRMED",
  "paymentStatus": "PENDING"
}
```

### **3. Ladies Quota Booking**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1250.00,
  "quotaType": "LADIES",
  "status": "CONFIRMED",
  "paymentStatus": "PENDING"
}
```

### **4. Senior Citizen Booking**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1000.00,
  "quotaType": "SENIOR_CITIZEN",
  "status": "CONFIRMED",
  "paymentStatus": "PENDING"
}
```

### **5. Waitlist Booking**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1250.00,
  "status": "WAITLIST",
  "paymentStatus": "PENDING"
}
```

---

## 🚀 **API Usage Examples**

### **cURL Example**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "user": { "id": 1 },
    "train": { "id": 1 },
    "passenger": { "id": 1 },
    "coach": { "id": 1 },
    "journeyDate": "2024-12-25",
    "totalFare": 1250.00,
    "status": "CONFIRMED",
    "paymentStatus": "PENDING"
  }'
```

### **JavaScript/Fetch Example**
```javascript
const bookingData = {
  user: { id: 1 },
  train: { id: 1 },
  passenger: { id: 1 },
  coach: { id: 1 },
  journeyDate: "2024-12-25",
  totalFare: 1250.00,
  status: "CONFIRMED",
  paymentStatus: "PENDING"
};

fetch('/api/bookings', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(bookingData)
})
.then(response => response.json())
.then(data => console.log('Booking created:', data))
.catch(error => console.error('Error:', error));
```

### **Postman Example**
1. **Method:** POST
2. **URL:** `http://localhost:8080/api/bookings`
3. **Headers:** `Content-Type: application/json`
4. **Body (raw JSON):**
```json
{
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "totalFare": 1250.00,
  "status": "CONFIRMED",
  "paymentStatus": "PENDING"
}
```

---

## ⚠️ **Important Notes**

1. **ID References**: Use only the `id` field for related entities (user, train, passenger, coach, seat)
2. **Date Format**: Use ISO 8601 format for dates (`YYYY-MM-DD` for dates, `YYYY-MM-DDTHH:mm:ss` for datetime)
3. **Auto-generated Fields**: `id`, `pnrNumber`, `createdAt`, `updatedAt` are auto-generated
4. **Default Values**: 
   - `status`: `CONFIRMED`
   - `paymentStatus`: `PENDING`
   - `quotaType`: `GENERAL`
   - `isTatkal`: `false`
   - `isCancelled`: `false`
5. **Validation**: Ensure all required fields are provided
6. **Business Logic**: The system will validate seat availability, user permissions, etc.

---

## 🔍 **Response Example**

**Success Response (201 Created):**
```json
{
  "id": 1,
  "pnrNumber": "PNR123456789",
  "user": { "id": 1 },
  "train": { "id": 1 },
  "passenger": { "id": 1 },
  "coach": { "id": 1 },
  "journeyDate": "2024-12-25",
  "bookingDate": "2024-12-20T10:30:00",
  "totalFare": 1250.00,
  "status": "CONFIRMED",
  "paymentStatus": "PENDING",
  "quotaType": "GENERAL",
  "isTatkal": false,
  "isCancelled": false,
  "createdAt": "2024-12-20T10:30:00",
  "updatedAt": "2024-12-20T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Error: User not found"
}
```
