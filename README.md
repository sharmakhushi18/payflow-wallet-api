# 💸 PayFlow — Digital Wallet API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)
![JWT](https://img.shields.io/badge/JWT-Auth-red)
![Redis](https://img.shields.io/badge/Redis-Cache-red)
![Docker](https://img.shields.io/badge/Docker-Deployed-blue)

UPI-style Digital Wallet API — register, get a wallet, send money, every transaction triggers an automatic email notification to both sender and receiver.

## 🌐 Live
| Service | URL |
|---------|-----|
| User Service API | https://payflow-wallet-api.onrender.com |
| Register | POST https://payflow-wallet-api.onrender.com/api/auth/register |
| Login | POST https://payflow-wallet-api.onrender.com/api/auth/login |

> ⚠️ Hosted on Render free tier — first request may take 30–50s to cold start.

---

## 📌 What Is This?

A Spring Boot microservices REST API that simulates a UPI-style payment system. Users register, get a wallet with a unique UPI ID, and transfer money peer-to-peer. Every transaction atomically debits the sender and credits the receiver — with real email notifications sent to both parties automatically.

Core engineering focus: **financial data integrity under concurrency** — the hardest problem in fintech backends.

---

## 🚀 How It Works

```
User registers → Gets a Wallet with UPI ID (khushi@payflow)
        ↓
User tops up wallet balance
        ↓
User sends money → toUpiId + amount
        ↓
@Transactional block → debit + credit atomically
        ↓
@Version optimistic lock → concurrent requests? second one fails cleanly
        ↓
Notification Service → real Gmail email to sender + receiver
```

---

## 🛠️ Tech Stack

| Technology | Usage |
|------------|-------|
| Java 17 | Core language |
| Spring Boot 3.4.5 | Backend framework |
| Spring Security + JWT | Stateless authentication |
| Spring Data JPA + Hibernate | ORM |
| MySQL (local) / PostgreSQL Neon (prod) | Per-service databases |
| Redis | Wallet balance caching |
| JavaMailSender | Real Gmail email notifications |
| Docker | Containerization |
| Render | Cloud deployment |

---

## 📐 Architecture

```
Client (Postman / Frontend)
        ↓
   User Service        :8081   ← Register, Login, JWT Auth
   Wallet Service      :8082   ← Balance, UPI ID, Top-up, Transfer
   Transaction Service :8083   ← State machine, history
   Notification Service:8084   ← Internal only — Gmail email
```

> Each service owns its own database — no cross-service DB queries. Loose coupling by design.

---

## 🗄️ Database Schema

```
payflow_users DB:
  users → id, name, email (unique), password (BCrypt), role

payflow_wallet DB:
  wallets → id, userId, upiId (unique),
            balance, version (optimistic lock key)

payflow_transactions DB:
  transactions → id, senderUserId, receiverUserId,
                 senderUpiId, receiverUpiId,
                 amount, status, createdAt
```

---

## 🔄 Transaction State Machine

```
INITIATED → PROCESSING → SUCCESS
INITIATED → PROCESSING → FAILED

SUCCESS → terminal (irreversible)
FAILED  → terminal (irreversible)
```

Invalid transitions are rejected at the service layer before any DB write.

---

## 📡 API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login, returns JWT token |

### Wallet (🔒 JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/wallets | Create wallet |
| GET | /api/wallets | Get balance |
| POST | /api/wallets/top-up | Add funds to wallet |
| POST | /api/wallets/transfer | Send money to UPI ID |

### Transactions (🔒 JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/transactions | Create transaction record |
| GET | /api/transactions/history/{userId} | Transaction history |

### Notifications (🔒 Internal Service Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/notifications/send | Trigger email — internal use |
| POST | /api/notifications/transaction | Transaction email — internal use |

> All protected endpoints require: `Authorization: Bearer <token>`  
> Notification endpoints are internal — called by Wallet Service post-transfer, not exposed to clients. Production hardening: service mesh / API gateway to restrict inter-service traffic.

---

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
Authorization: Bearer <token>

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
Authorization: Bearer <token>

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

> userId is extracted from the JWT token server-side — never trusted from request input.

---

## ⚙️ How to Run Locally

**Prerequisites:** Java 17+, MySQL 8.0, Maven, Redis

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
cd user-service        && mvn spring-boot:run   # :8081
cd wallet-service      && mvn spring-boot:run   # :8082
cd transaction-service && mvn spring-boot:run   # :8083
cd notification-service && mvn spring-boot:run  # :8084
```

---

## 💡 Key Design Decisions

**Why @Transactional?**  
Debit and credit must succeed or fail together. Partial success — sender debited, receiver not credited — is never acceptable in fintech. `@Transactional` guarantees atomicity with automatic rollback on any failure.

**Why Optimistic Locking (@Version)?**  
Two concurrent transfers from the same wallet would both read sufficient balance and both succeed — causing negative balance. `@Version` stamps each wallet row; the second concurrent write detects a stale version and fails cleanly with a conflict error instead of silently corrupting data.

**Why Redis?**  
Wallet balance is read on every transaction check. Redis in-memory cache reduces repeated DB reads under load. `@CacheEvict` invalidates the cache on every successful transfer — no stale balance reads.

**Why JWT stateless auth?**  
No server-side sessions. Any service instance validates the token independently without a DB call — essential for horizontal scaling in a microservices architecture.

**Why database-per-service?**  
Each microservice owns its schema. User Service cannot query Wallet DB directly. Services communicate via API, not shared tables — enabling independent deployability and failure isolation.

---

## ✅ Features

- [x] JWT Authentication + Spring Security
- [x] BCrypt password hashing
- [x] User registration and login
- [x] Environment variables for all sensitive config
- [x] Wallet Service — UPI ID generation, balance, top-up, transfer
- [x] Optimistic locking — prevents double debit under concurrency
- [x] Transaction Service — state machine (INITIATED → PROCESSING → SUCCESS/FAILED)
- [x] Transaction history API
- [x] Notification Service — real Gmail email on every transaction
- [x] Docker containerization
- [x] Deployed on Render with PostgreSQL Neon

---

## 🔮 Roadmap

- Kafka — async notification events (decouple notification from transfer flow)
- Docker Compose — single command local setup for all 4 services
- WebSocket — real-time balance updates
- Redis-based rate limiting — prevent transfer spam
- Pagination — transaction history with cursor-based pagination

---

## 👩‍💻 Author

**Khushi Sharma** — Java Backend Developer  
Spring Boot · PostgreSQL · Redis · Microservices · JWT · Docker  
Final Year ECE @ LNCT Bhopal

[GitHub](https://github.com/sharmakhushi18) · [LinkedIn](https://www.linkedin.com/in/khushi-sharma-523153259/)
