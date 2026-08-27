# Loan Platform

A full-stack consumer loan management system built with Spring Boot (backend) and Angular (frontend).

---

## Quick Start

Follow the setup instructions for each component, then run them together.

### 1. Backend Setup

Navigate to `loan-platform-backend/` and follow [Backend README](loan-platform-backend/README.md).

**TL;DR:**
```bash
cd loan-platform-backend
docker compose up -d              # Start PostgreSQL
./mvnw spring-boot:run            # Start API on http://localhost:8080
```

### 2. Frontend Setup

Navigate to `loan-platform-frontend/` and follow [Frontend README](loan-platform-frontend/README.md).

**TL;DR:**
```bash
cd loan-platform-frontend
npm install
npm start                         # Start dev server on http://localhost:4200
```

### 3. Access the Application

Open your browser and go to:
```
http://localhost:4200
```

The frontend communicates with the backend API at `http://localhost:8080`.

---

## Project Structure

```
loan-platform/
├── loan-platform-backend/    # Spring Boot REST API
├── loan-platform-frontend/   # Angular application
└── README.MD                 # This file
```

## Requirements

- **Docker** (for PostgreSQL)
- **Java 21** (for backend)
- **Node.js & npm** (for frontend)
- **Maven** (included via `./mvnw`)

For detailed setup instructions, see the individual README files in each directory.
