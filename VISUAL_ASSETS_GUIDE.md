# 🎨 IRCTC Project Visual Assets for LinkedIn

## 📊 System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    IRCTC Railway Booking System                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   React Frontend │    │   Spring Boot   │    │ PostgreSQL  │ │
│  │   (Port 3000)   │◄──►│   Backend      │◄──►│  Database   │ │
│  │   TypeScript    │    │   (Port 8082)   │    │             │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                             │
│           │              ┌─────────────────┐                    │
│           │              │   Apache Kafka │                    │
│           │              │   Event Stream │                    │
│           │              └─────────────────┘                    │
│           │                       │                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   Swagger UI    │    │   Razorpay      │    │   Redis     │ │
│  │   API Docs      │    │   Payment       │    │   Cache     │ │
│  │   (Port 8082)   │    │   Gateway       │    │             │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Microservices Architecture (Future)

```
┌─────────────────────────────────────────────────────────────────┐
│                    IRCTC Microservices Architecture             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────┐ │
│  │   API Gateway   │    │   Eureka Server │    │   React     │ │
│  │   (Port 8090)   │    │   (Port 8761)   │    │  Frontend   │ │
│  └─────────────────┘    └─────────────────┘    └─────────────┘ │
│           │                       │                             │
│  ┌────────┼────────┐              │                             │
│  │        │        │              │                             │
│  ▼        ▼        ▼              │                             │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────────────┐ │
│ │User │ │Train│ │Book │ │Pay  │ │Notif│ │Swag│ │   Shared    │ │
│ │Svc  │ │Svc  │ │Svc  │ │Svc  │ │Svc  │ │Hub │ │   Events    │ │
│ │8091 │ │8092 │ │8093 │ │8094 │ │8095 │ │8096│ │             │ │
│ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 Key Features Showcase

```
┌─────────────────────────────────────────────────────────────────┐
│                    IRCTC Feature Highlights                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  🔐 Authentication    🚂 Train Search    💳 Payment Gateway     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ • JWT Security  │  │ • Multi-city    │  │ • Razorpay      │ │
│  │ • Role-based    │  │ • Real-time     │  │ • Secure        │ │
│  │ • Password      │  │ • Route         │  │ • Refunds       │ │
│  │   Encryption    │  │   Optimization  │  │ • History       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  🎫 Smart Booking    🏆 Loyalty System   📱 Real-time Notifications │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ • Seat Selection │  │ • Points        │  │ • WebSocket     │ │
│  │ • Waitlist/RAC   │  │ • Tiers         │  │ • Kafka        │ │
│  │ • PNR Tracking   │  │ • Redemption    │  │ • Multi-channel│ │
│  │ • Cancellation  │  │ • Rewards       │  │ • Templates    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  🛡️ Travel Insurance  🍽️ Meal Booking    📊 Performance Dashboard │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ • Comprehensive │  │ • Station-wise  │  │ • Real-time     │ │
│  │ • Multiple      │  │ • Vendor        │  │ • Analytics     │ │
│  │   Providers     │  │   Integration   │  │ • Monitoring    │ │
│  │ • Claims        │  │ • Delivery      │  │ • Alerts       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack Visualization

```
┌─────────────────────────────────────────────────────────────────┐
│                    Technology Stack                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  🎨 Frontend Layer                                              │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ React 19 + TypeScript + Material-UI + Recharts + Axios     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  🔧 Backend Layer                                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ Spring Boot 3.5.6 + Java 21 + Spring Security + JPA       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  🗄️ Data Layer                                                  │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PostgreSQL + H2 + Redis + HikariCP                        │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  📡 Integration Layer                                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ Apache Kafka + Razorpay + WebSocket + Swagger/OpenAPI     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  🚀 DevOps Layer                                                │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ Docker + Docker Compose + Maven + Micrometer + Log4j2     │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 📱 Demo Interface Mockup

```
┌─────────────────────────────────────────────────────────────────┐
│  🚂 IRCTC Railway Booking System - Dashboard                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ 🔍 Train Search  │  │ 🎫 My Bookings  │  │ 💳 Payments    │ │
│  │                 │  │                 │  │                 │ │
│  │ From: [Delhi  ▼] │  │ PNR: PNR123456 │  │ Status: ✅ Paid │ │
│  │ To:   [Mumbai ▼] │  │ Train: Rajdhani│  │ Amount: ₹2,500  │ │
│  │ Date: [20 Jan ▼] │  │ Date: 20 Jan   │  │ Method: Card    │ │
│  │                 │  │ Status: Confirmed│  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ 🏆 Loyalty       │  │ 🛡️ Insurance   │  │ 🍽️ Meals       │ │
│  │                 │  │                 │  │                 │ │
│  │ Points: 5,000    │  │ Coverage: ₹1L  │  │ Vendor: RCS    │ │
│  │ Tier: Silver     │  │ Premium: ₹590   │  │ Order: Biryani │ │
│  │ Next: Gold       │  │ Status: Active  │  │ Delivery: AGC  │ │
│  │                 │  │                 │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  📊 Performance Dashboard                                       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ API Response Time: 45ms │ Active Users: 1,247 │ Uptime: 99.9% │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🎨 Color Scheme & Branding

```
Primary Colors:
- Railway Blue: #1E3A8A
- Success Green: #10B981
- Warning Orange: #F59E0B
- Error Red: #EF4444
- Neutral Gray: #6B7280

Typography:
- Headers: Inter Bold
- Body: Inter Regular
- Code: JetBrains Mono

Icons:
- 🚂 Train & Railway
- 💳 Payment & Finance
- 🔐 Security & Auth
- 📊 Analytics & Data
- 🎫 Booking & Tickets
```

## 📸 Suggested Screenshots

1. **Dashboard Overview** - Main application interface
2. **Train Search Results** - Search functionality with filters
3. **Seat Selection** - Interactive seat map
4. **Payment Flow** - Razorpay integration
5. **Swagger API Docs** - API documentation interface
6. **Performance Metrics** - Real-time dashboard
7. **Mobile Responsive** - Mobile interface view
8. **Microservices Architecture** - Service discovery dashboard
