# Backend — Loan Platform API

REST API for consumer loan management. Built with Spring Boot 3.5, Java 21, and PostgreSQL 17.

---

## Requirements

- Java 21
- Docker (for PostgreSQL)

## Quick Start

### 1. Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432`. Flyway will automatically apply database migrations on startup.

### 2. Start the Backend

```bash
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`.

### 3. Run Tests

```bash
./mvnw test
```

95 tests, no Docker required (uses H2 in-memory database).

---

## Tech Stack

| Component | Choice |
|-----------|--------|
| Framework | Spring Boot 3.5 |
| Language | Java 21 |
| Database | PostgreSQL 17 |
| Auth | JWT + BCrypt |
| Testing | JUnit 5, Mockito, H2 |

---

## API Overview

All endpoints require JWT authentication (except `/register` and `/login`).

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/auth/register` | POST | Create customer account |
| `/api/auth/login` | POST | Get JWT token |
| `/api/loans/loan-application` | POST | Apply for a loan (customer) |
| `/api/loans/loan-application` | PUT | Review/approve/reject (officer/admin) |
| `/api/loans/loan-application` | DELETE | Withdraw application (customer) |

See the [full backend README](README.md) in the repository for detailed API documentation.

---

## Project Structure

```
src/main/java/io/github/edgarsskrabins/loan_platform/
├── auth/                  # Authentication & login
├── customer/              # Customer profiles
├── loanApplication/       # Loan application workflow
├── audit/                 # Audit logging
└── security/              # JWT & security filters
```

Each feature module contains `controller`, `service`, `repository`, `dto`, and `entity` as needed.
