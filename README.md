# 💳 PayFlow — Digital Wallet API

A backend REST API simulating core digital wallet operations — built with Java, Spring Boot, and PostgreSQL.  
Designed with **payment-grade reliability**: ACID transactions, concurrency control, JWT security, and edge case handling.

🔗 **API Docs (Swagger):** _Add your deployed Swagger URL here_  
💻 **GitHub:** [github.com/sharmakhushi18/payflow-wallet-api](https://github.com/sharmakhushi18/payflow-wallet-api)

---

## What It Does

PayFlow handles the core lifecycle of a digital wallet:

| Operation | Description |
|---|---|
| Register / Login | User auth with JWT token issuance |
| Create Wallet | Each user gets one wallet with starting balance |
| Deposit Funds | Add money to your wallet |
| Transfer Funds | Send money to another user's wallet |
| Transaction History | View all credits and debits with timestamps |
| Balance Check | Real-time wallet balance |

---

## The Engineering Problems It Solves

### 1. Concurrent Transfer Problem
> User A and User B both send ₹500 from the same wallet simultaneously.  
> Without locking — both reads see ₹1000, both deduct ₹500, final balance = ₹500 (wrong).  
> **PayFlow uses pessimistic locking** — only one transaction proceeds at a time. Balance is always correct.

### 2. Atomicity Problem
> Transfer = debit sender + credit receiver.  
> If credit fails after debit — money disappears.  
> **PayFlow wraps both in `@Transactional`** — either both succeed or both roll back. No partial state.

### 3. Insufficient Balance
> System validates balance before any debit operation.  
> Throws a clean, structured error — never processes invalid transfers.

### 4. Duplicate Transaction Prevention
> Idempotency checks prevent the same transfer from being processed twice under network retries.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security + JWT |
| ORM | JPA / Hibernate |
| Database | PostgreSQL |
| Containerisation | Docker |
| API Testing | Postman / Swagger UI |
| Build Tool | Maven |

---

## Architecture

```
src/
├── controller/        # REST endpoints (WalletController, TransactionController, AuthController)
├── service/           # Business logic (WalletService, TransactionService)
├── repository/        # JPA repositories
├── model/             # Entities: User, Wallet, Transaction
├── dto/               # Request / Response objects
├── security/          # JWT filter, token provider
└── exception/         # Global exception handler
```

**Design pattern:** Layered architecture (Controller → Service → Repository)  
Each layer has a single responsibility — easy to test, easy to extend.

---

## API Endpoints

### Auth
```
POST /api/auth/register     → Register new user
POST /api/auth/login        → Login, returns JWT token
```

### Wallet
```
GET  /api/wallet/balance            → Check current balance
POST /api/wallet/deposit            → Add funds
POST /api/wallet/transfer           → Transfer to another user
GET  /api/wallet/transactions       → Full transaction history
```

> All wallet endpoints require `Authorization: Bearer <token>` header.

---

## How To Run Locally

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Maven

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/sharmakhushi18/payflow-wallet-api.git
cd payflow-wallet-api

# 2. Set up PostgreSQL database
createdb payflow_db

# 3. Configure application.properties
# Edit src/main/resources/application.properties:
# spring.datasource.url=jdbc:postgresql://localhost:5432/payflow_db
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# 4. Run the application
mvn spring-boot:run
```

App runs at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Run with Docker

```bash
docker build -t payflow-api .
docker run -p 8080:8080 payflow-api
```

---

## Sample Request — Transfer Funds

```http
POST /api/wallet/transfer
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "receiverEmail": "user@example.com",
  "amount": 500.00
}
```

**Success Response:**
```json
{
  "status": "SUCCESS",
  "message": "Transfer completed",
  "newBalance": 1500.00,
  "transactionId": "TXN-20260517-001"
}
```

**Failure Response (Insufficient Balance):**
```json
{
  "status": "FAILED",
  "error": "Insufficient balance",
  "availableBalance": 200.00
}
```

---

## Key Engineering Decisions

**Why pessimistic locking over optimistic?**  
In payment systems, conflicts are frequent and costly. Pessimistic locking guarantees correctness at the cost of some throughput — acceptable for a wallet API where money correctness > speed.

**Why JWT over session-based auth?**  
Stateless authentication scales horizontally. No session store needed — fits microservices architecture.

**Why `@Transactional` on transfer service?**  
A transfer is a two-step atomic operation. Without `@Transactional`, a failure between debit and credit leaves the system in an inconsistent state.

---

## What I Learned Building This

- How payment systems think about consistency vs availability tradeoffs
- Why database-level locking matters more than application-level checks
- How to structure a Spring Boot project for real-world extensibility
- How JWT authentication integrates across a secured REST API

---

## Author

**Khushi Sharma** — Java Backend Developer  
📧 khushis50956@gmail.com  
🔗 [linkedin.com/in/khushissharma](https://linkedin.com/in/khushissharma)  
💻 [github.com/sharmakhushi18](https://github.com/sharmakhushi18)
