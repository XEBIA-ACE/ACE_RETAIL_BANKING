# Banking Core Service

A production-ready **Spring Boot 3 / Java 21** microservice that implements core banking capabilities following **Hexagonal Architecture** (Ports & Adapters).

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Overview

The Banking Core Service manages the following domains:

| Domain | Responsibilities |
|---|---|
| **Customers** | Onboarding, profile management, lifecycle |
| **Accounts** | Open/close accounts (Checking, Savings, etc.) |
| **Transactions** | Debit/credit recording, balance management |
| **Loans** | Application, approval, disbursement |
| **Payments** | Internal transfers, ACH, wire, bill payments |

---

## Architecture

The service follows **Hexagonal Architecture** (also known as Ports & Adapters):

```
┌─────────────────────────────────────────────────────────────┐
│                        Adapters (in)                        │
│   REST Controllers  ──►  Application Ports (in)             │
│                              │                              │
│                    ┌─────────▼──────────┐                   │
│                    │   Domain Model     │                    │
│                    │  (pure Java POJOs) │                    │
│                    └─────────┬──────────┘                   │
│                              │                              │
│   Application Ports (out) ◄──┘                              │
│         │                                                   │
│   Adapters (out): JPA / MySQL                               │
└─────────────────────────────────────────────────────────────┘
```

**Package layout:**

```
com.banking
├── domain/
│   ├── model/          # Pure domain entities & enums
│   └── exception/      # Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/         # Inbound use-case interfaces
│   │   └── out/        # Outbound repository interfaces
│   └── service/        # Use-case implementations
└── adapter/
    ├── in/
    │   └── web/        # REST controllers + DTOs
    └── out/
        └── persistence/ # JPA entities, repositories, mappers
```

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| ORM | Hibernate / Spring Data JPA |
| Database | MySQL 8 |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Container | Docker (multi-stage, Alpine JRE) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (optional, for local MySQL)

### Running Locally

1. **Copy environment variables:**

   ```bash
   cp .env.example .env
   # Edit .env with your local DB credentials
   ```

2. **Start MySQL (via Docker Compose):**

   ```bash
   docker compose up -d mysql
   ```

3. **Run the application:**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify health:**

   ```bash
   curl http://localhost:8080/api/v1/health
   # {"status":"UP","service":"banking-core-service","timestamp":"..."}
   ```

### Running with Docker

```bash
# Build the image
docker build -t banking-core-service:latest .

# Run (assumes MySQL is accessible at DB_URL)
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/banking_core \
  -e DB_USERNAME=banking_user \
  -e DB_PASSWORD=secret \
  banking-core-service:latest
```

---

## API Reference

Once running, visit **Swagger UI** at:

```
http://localhost:8080/swagger-ui.html
```

### Key Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/health` | Service health check |
| `POST` | `/api/v1/customers` | Onboard a new customer |
| `GET` | `/api/v1/customers/{id}` | Get customer by ID |
| `GET` | `/api/v1/customers` | List customers (paginated) |
| `POST` | `/api/v1/accounts` | Open a new account |
| `GET` | `/api/v1/accounts/{id}` | Get account by ID |
| `DELETE` | `/api/v1/accounts/{id}` | Close an account |
| `POST` | `/api/v1/transactions` | Record a transaction |
| `GET` | `/api/v1/transactions/{id}` | Get transaction by ID |
| `POST` | `/api/v1/loans` | Apply for a loan |
| `PUT` | `/api/v1/loans/{id}/approve` | Approve a loan |
| `PUT` | `/api/v1/loans/{id}/disburse` | Disburse a loan |
| `POST` | `/api/v1/payments` | Initiate a payment |
| `PUT` | `/api/v1/payments/{id}/process` | Process a payment |

Spring Actuator endpoints are available at `/actuator/health`, `/actuator/info`, `/actuator/metrics`.

---

## Configuration

All configuration is driven by environment variables. See [`.env.example`](.env.example) for the full list.

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:mysql://localhost:3306/banking_core` | JDBC URL |
| `DB_USERNAME` | `banking_user` | DB username |
| `DB_PASSWORD` | — | DB password |
| `DB_POOL_SIZE` | `10` | HikariCP max pool size |
| `JPA_SHOW_SQL` | `false` | Log SQL statements |
| `LOG_LEVEL` | `INFO` | Application log level |

---

## Database Migrations

Schema is managed by **Flyway**. Migration scripts live in:

```
src/main/resources/db/migration/
  V1__init_schema.sql
```

Flyway runs automatically on startup. To run manually:

```bash
./mvnw flyway:migrate
```

---

## Testing

```bash
# Run all unit tests
./mvnw test

# Run with integration tests (requires Docker for Testcontainers)
./mvnw verify
```

Test categories:

| Type | Location | Description |
|---|---|---|
| Unit | `*Test.java` | Service logic with mocked ports |
| Web slice | `*ControllerTest.java` | MockMvc controller tests |
| Integration | `*IntegrationTest.java` | Full context with Testcontainers MySQL |

---

## Project Structure

```
banking-core-service/
├── src/
│   ├── main/
│   │   ├── java/com/banking/
│   │   │   ├── BankingCoreServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   └── exception/
│   │   │   ├── application/
│   │   │   │   ├── port/in/
│   │   │   │   ├── port/out/
│   │   │   │   └── service/
│   │   │   └── adapter/
│   │   │       ├── in/web/
│   │   │       └── out/persistence/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/banking/
├── Dockerfile
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```
