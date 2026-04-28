# 💸 PayFlow — Digital Wallet API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)
![JWT](https://img.shields.io/badge/JWT-Auth-red)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Docker](https://img.shields.io/badge/Docker-Deployed-blue)

UPI-style Digital Wallet API — register, get a wallet, send money, every transaction triggers an automatic email notification.

## 🌐 Live
| Service | URL |
|---------|-----|
| User Service API | https://payflow-wallet-api.onrender.com |
| Register | POST https://payflow-wallet-api.onrender.com/api/auth/register |
| Login | POST https://payflow-wallet-api.onrender.com/api/auth/login |

## 📌 What Is This?
A Spring Boot microservices REST API that allows users to send and receive money via UPI-style wallet. When a transaction happens, both sender and receiver get an automatic email — no manual intervention required.

Secured with JWT Authentication. Every transaction uses @Transactional + Optimistic Locking to prevent double debit.

## 🚀 What This Project Does
```
User registers → Gets a Wallet with UPI ID (khushi@payflow)
        ↓
User tops up wallet balance
        ↓
User sends money to another UPI ID
        ↓
@Transactional + Optimistic Lock → no double debit ever
        ↓
Notification Service → Real email to sender + receiver
```

## 🛠️ Tech Stack
| Technology | Usage |
|------------|-------|
| Java 17 | Core language |
| Spring Boot 3.4.5 | Backend framework |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | Database ORM |
| MySQL (local) / PostgreSQL Neon (prod) | Per-service database |
| Redis | Wallet balance caching |
| JavaMailSender | Real Gmail email notifications |
| Docker | Containerization |
| Render | Cloud deployment |

## 📐 Architecture
```
Client (Postman / Frontend)
        ↓
   User Service (8081)         ← Register, Login, JWT Auth
   Wallet Service (8082)       ← Balance, UPI ID, Top-up, Transfer
   Transaction Service (8083)  ← State machine, history
   Notification Service (8084) ← Real Gmail email
```

## 🗄️ Database Schema
```
payflow_users DB:
users → id, name, email (unique), password (BCrypt), role

payflow_wallet DB:
wallets → id, userId, upiId (unique),
          balance, version (optimistic lock)

payflow_transactions DB:
transactions → id, senderUserId, receiverUserId,
               senderUpiId, receiverUpiId,
               amount, status, createdAt
```

## 🔄 Transaction State Machine
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
| POST | /api/wallets | Create wallet |
| GET | /api/wallets | Get balance |
| POST | /api/wallets/top-up | Add money to wallet |
| POST | /api/wallets/transfer | Send money to UPI ID |

### Transactions (🔒 JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/transactions | Create transaction record |
| GET | /api/transactions/history/{userId} | Transaction history |

### Notifications (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/notifications/send | Send email notification |
| POST | /api/notifications/transaction | Send transaction email |

All protected endpoints require: `Authorization: Bearer <token>`

## 📬 Sample Requests

### Register (Live)
```json
POST https://payflow-wallet-api.onrender.com/api/auth/register
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

### Create Wallet
```json
POST /api/wallets
X-User-Id: 1
X-User-Name: Khushi Sharma

Response:
{
  "id": 1,
  "upiId": "khushish@payflow",
  "balance": 0,
  "createdAt": "2026-04-26T06:38:51"
}
```

### Send Money
```json
POST /api/wallets/transfer
X-User-Id: 1
{
  "toUpiId": "rahul@payflow",
  "amount": 500.00
}

Response:
{
  "status": "SUCCESS",
  "message": "Transfer successful! Sent Rs.500 to rahul@payflow"
}
```

## ⚙️ How to Run Locally

**Prerequisites:** Java 17+, MySQL 8.0, Maven

```bash
# 1. Clone
git clone https://github.com/sharmakhushi18/payflow-wallet-api.git

# 2. Create MySQL databases
CREATE DATABASE payflow_users;
CREATE DATABASE payflow_wallet;
CREATE DATABASE payflow_transactions;

# 3. Set environment variables
DB_PASSWORD=your_mysql_password
JWT_SECRET=payflow-super-secret-jwt-key-32chars!!
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# 4. Run all services
cd user-service && mvn spring-boot:run         # port 8081
cd wallet-service && mvn spring-boot:run       # port 8082
cd transaction-service && mvn spring-boot:run  # port 8083
cd notification-service && mvn spring-boot:run # port 8084
```

## 💡 Key Design Decisions

**Why @Transactional?**
Debit and credit must succeed or fail together. Partial success — sender debited but receiver not credited — is never acceptable in fintech. @Transactional ensures atomicity.

**Why Optimistic Locking (@Version)?**
Two users sending from same wallet simultaneously would both see sufficient balance and both succeed — causing negative balance. @Version detects concurrent conflicts and fails the second transaction.

**Why Redis?**
Wallet balance is read on every transaction check. Redis in-memory cache reduces repeated DB reads. @CacheEvict invalidates cache on every successful transfer.

**Why JWT stateless auth?**
No server-side sessions. Any service instance can verify the token without hitting DB — essential for horizontal scaling in microservices.

**Why database-per-service?**
Each microservice owns its schema — loose coupling. User Service cannot directly query Wallet DB. Independent deployability.

## ✅ Features Completed
- [x] JWT Authentication + Spring Security
- [x] BCrypt password hashing (cost factor 12)
- [x] User registration and login
- [x] Environment variables for sensitive config
- [x] Wallet Service — UPI ID generation, balance, top-up, transfer
- [x] Optimistic locking on wallet — prevents double debit
- [x] Transaction Service — state machine INITIATED→PROCESSING→SUCCESS
- [x] Transaction history API
- [x] Notification Service — real Gmail email on transaction
- [x] Docker + Dockerfile for User Service
- [x] Deployed on Render with PostgreSQL Neon — Live URL available
- [ ] Kafka — async notification events
- [ ] Docker Compose — all services together

## 🔮 Planned
- Kafka — async notification events
- WebSocket — real-time balance update
- Rate limiting — Redis-based request throttling
- Pagination — transaction history

## 👩‍💻 Author
**Khushi Sharma** — Java Backend Developer

Spring Boot · PostgreSQL · Redis · Microservices · JWT · Docker

Final Year ECE @ LNCT Bhopal

[GitHub](https://github.com/sharmakhushi18) · [LinkedIn](https://www.linkedin.com/in/khushi-sharma-523153259/)
