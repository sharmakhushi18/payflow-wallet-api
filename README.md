# 💸 PayFlow — Digital Wallet API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![JWT](https://img.shields.io/badge/JWT-Auth-red)
![Kafka](https://img.shields.io/badge/Kafka-Events-black)
![Redis](https://img.shields.io/badge/Redis-Cache-red)

Event-driven Digital Wallet API — send money, receive money, every transaction triggers an automatic email notification.

## 🌐 Live
| Service | URL |
|---------|-----|
| Backend API | Coming soon |
| Swagger UI | Coming soon |

## 📌 What Is This?
A Spring Boot REST API that allows users to send and receive money via UPI-style wallet system. When a transaction happens, both sender and receiver get an automatic email — no manual intervention required.

Secured with JWT Authentication. Every transaction is protected with pessimistic locking to prevent double debit.

## 🚀 What This Project Does
```
User registers → Gets a Wallet with UPI ID (khushi@payflow)
        ↓
User sends money to another user
        ↓
@Transactional + Pessimistic Lock → no double debit ever
        ↓
Kafka event published → Notification Service
        ↓
Sender + Receiver both get email instantly
```

## 🛠️ Tech Stack
| Technology | Usage |
|------------|-------|
| Java 17 | Core language |
| Spring Boot 3.4.5 | Backend framework |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | Database ORM |
| MySQL | Per-service database |
| Apache Kafka | Async transaction events |
| Redis | Wallet balance caching |
| Docker | Containerization |

## 📐 Architecture
```
Client (Postman / Frontend)
        ↓
   API Gateway         ← JWT validated on every request
        ↓
   User Service        ← Register, Login, JWT Auth
   Wallet Service      ← Balance, UPI ID, Top-up (Redis cache)
   Transaction Svc     ← Send/Receive with Pessimistic Lock
        ↓
   Apache Kafka        ← ORDER_PLACED event published
        ↓
   Notification Svc    ← Email to sender + receiver
```

## 🗄️ Database Schema
```
users
├── id, name, email (unique)
├── password (BCrypt hashed)
└── role (USER / ADMIN)

wallets
├── id, upiId (unique) e.g. khushi@payflow
├── balance (pessimistic lock on update)
└── user_id (FK)

transactions
├── id, amount, type (CREDIT / DEBIT)
├── status (SUCCESS / FAILED)
├── senderWalletId (FK), receiverWalletId (FK)
└── createdAt (auto-set)
```

## 🔄 Transaction Flow — State Machine
```
INITIATED → PROCESSING → SUCCESS
INITIATED → PROCESSING → FAILED
SUCCESS   → terminal (cannot be reversed)
FAILED    → terminal
```
Invalid transitions rejected at service layer.

## 📡 API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login, get JWT token |

### Wallet (🔒 JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/wallet/balance | Get wallet balance |
| POST | /api/wallet/topup | Add money to wallet |
| GET | /api/wallet/upi-id | Get your UPI ID |

### Transactions (🔒 JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/transactions/send | Send money to UPI ID |
| GET | /api/transactions/history | Transaction history |

All protected endpoints require: `Authorization: Bearer <token>`

## 📬 Sample Requests

### Register
```json
POST /api/auth/register
{
  "name": "Khushi Sharma",
  "email": "khushi@payflow.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "name": "Khushi Sharma",
  "role": "USER"
}
```

### Send Money
```json
POST /api/transactions/send
Authorization: Bearer eyJhbGci...
{
  "toUpiId": "rahul@payflow",
  "amount": 500.00
}

Response:
{
  "transactionId": "TXN123456",
  "status": "SUCCESS",
  "amount": 500.00,
  "message": "Money sent successfully"
}
```

## ⚙️ How to Run Locally

**Prerequisites:** Java 17+, MySQL 8.0, Maven

```bash
# 1. Clone
git clone https://github.com/sharmakhushi18/payflow-wallet-api.git

# 2. Create MySQL database
CREATE DATABASE payflow_users;

# 3. Set environment variables
DB_PASSWORD=your_mysql_password
JWT_SECRET=payflow-super-secret-jwt-key-32chars!!

# 4. Run User Service
cd user-service
mvn spring-boot:run

# Server: http://localhost:8081
```

## 💡 Key Design Decisions

**Why @Transactional + Pessimistic Locking?**
Two users sending from the same wallet simultaneously would both see sufficient balance and both succeed — causing negative balance. Pessimistic lock ensures only one transaction proceeds at a time. DB unique constraints are the final safety net.

**Why Kafka for notifications?**
Transaction and Notification are completely decoupled. If email fails, the transaction still succeeds. Kafka guarantees the notification will be delivered eventually — no data loss.

**Why Redis for wallet balance?**
Wallet balance is read on every transaction check. Redis cache reduces repeated DB reads. Cache is invalidated on every successful transaction using @CacheEvict.

**Why JWT stateless auth?**
No server-side sessions. Any service instance can verify the token without hitting the DB — essential for horizontal scaling in microservices.

**Why database-per-service?**
Each microservice owns its schema. User Service cannot directly query Wallet Service DB. This ensures loose coupling and independent deployability.

## ✅ Features Completed
- [x] JWT Authentication + Spring Security
- [x] BCrypt password hashing (cost factor 12)
- [x] User registration and login
- [x] Environment variables for sensitive config
- [ ] Wallet Service with Redis cache
- [ ] Transaction Service with Pessimistic Locking
- [ ] Kafka event publishing
- [ ] Notification Service with email
- [ ] Docker + docker-compose
- [ ] Render deployment

## 🔮 Planned
- WebSocket — real-time balance update
- Rate limiting — Redis-based request throttling
- Pagination — transaction history

## 👩‍💻 Author
**Khushi Sharma** — Java Backend Developer

Spring Boot · MySQL · Kafka · Redis · Microservices

Final Year ECE @ LNCT Bhopal

[GitHub](https://github.com/sharmakhushi18) · [LinkedIn](https://linkedin.com/in/khushi-sharma)
