# Plan: US-001 — Consolidated Account Dashboard View

## 1. Architecture Decisions

### 1.1 New `account` Domain Module
A new `account` domain package (`com.bank.core.account`) will be created following the existing domain-per-package convention defined in `AGENTS.md`. This keeps account concerns fully encapsulated and avoids polluting the `customer` domain. The domain will own its JPA entity, repository, service interface + implementation, DTOs, MapStruct mapper, and REST controller.

### 1.2 Dashboard Aggregation Endpoint
Rather than exposing a raw account list, a dedicated `DashboardController` under `com.bank.core.dashboard` will provide the `GET /api/v1/dashboard/accounts` endpoint. This controller calls `AccountService.getAccountsByCustomer(String customerExternalId)` and delegates grouping logic to the service layer, keeping the controller thin. The service returns a `DashboardResponseDto` with accounts pre-grouped by `AccountType` enum — the frontend only renders sections for keys present in the map.

### 1.3 Principal-Scoped Security
Spring Security's `@PreAuthorize` will enforce that the authenticated principal's customer external ID matches the requested data. The `SecurityConfig` will be updated to permit the new endpoint path under authenticated access. No anonymous access is permitted.

### 1.4 Database Migration Strategy
A new Flyway migration `V3__add_accounts_table.sql` will create the `accounts` table. The migration follows the conventions established in `V2__add_payees_table.sql`: `snake_case` columns, `BIGINT GENERATED ALWAYS AS IDENTITY` PK, named constraints, explicit indexes on `customer_id` and `external_id`, and `TIMESTAMP NOT NULL` audit columns. An `account_type` column will use a `VARCHAR(20)` with a `CHECK` constraint (MySQL 8.0 supports check constraints) to enforce the enum values `CHECKING`, `SAVINGS`, `CREDIT_CARD`. Similarly, `status` will be `VARCHAR(20)` with a `CHECK` constraint for `ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED`.

### 1.5 No N+1 Queries
The `AccountRepository` will use a single JPQL query with a `WHERE customer_id = :customerId` clause backed by the `idx_accounts_customer_id` index. Grouping is performed in-memory in the service layer after a single database round-trip, avoiding N+1 patterns.

### 1.6 Response Masking
Account numbers are masked at the DTO mapping layer (MapStruct mapper) — only the last 4 digits are exposed (`****XXXX`). Internal database IDs are never serialised; only `externalId` (UUID) is exposed.

---

## 2. API Contract

**Method:** `GET`  
**Path:** `/api/v1/dashboard/accounts`  
**Auth:** `Authorization: Bearer <JWT>`  
**Success:** `200 OK` — `DashboardResponseDto` (see spec.md §6)  
**Errors:** `401`, `403`, `500`

---

## 3. Data Model Changes

### New Table: `accounts`
```sql
-- V3__add_accounts_table.sql
CREATE TABLE accounts (
    id             BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    external_id    VARCHAR(255) NOT NULL,
    customer_id    BIGINT       NOT NULL,
    account_type   VARCHAR(20)  NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    balance        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    currency       VARCHAR(3)   NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    nickname       VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_external_id UNIQUE (external_id),
    CONSTRAINT chk_accounts_type CHECK (account_type IN ('CHECKING','SAVINGS','CREDIT_CARD')),
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE','INACTIVE','FROZEN','CLOSED')),
    CONSTRAINT fk_accounts_customer_id FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX idx_accounts_external_id ON accounts (external_id);
```

---

## 4. Files / Classes to Create or Modify

### Repository: `XEBIA-ACE/ACE_RETAIL_BANKING`

#### New Files — Database Migration
| File | Purpose |
|------|---------|
| `banking-core-service/src/main/resources/db/migration/V3__add_accounts_table.sql` | Creates `accounts` table with constraints and indexes |

#### New Files — Account Domain (`com.bank.core.account`)
| File | Purpose |
|------|---------|
| `banking-core-service/src/main/java/com/bank/core/account/domain/Account.java` | JPA entity (`@Entity`, Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`) |
| `banking-core-service/src/main/java/com/bank/core/account/domain/AccountType.java` | Java enum: `CHECKING`, `SAVINGS`, `CREDIT_CARD` |
| `banking-core-service/src/main/java/com/bank/core/account/domain/AccountStatus.java` | Java enum: `ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED` |
| `banking-core-service/src/main/java/com/bank/core/account/repository/AccountRepository.java` | Spring Data JPA repository; custom query `findAllByCustomerExternalId` |
| `banking-core-service/src/main/java/com/bank/core/account/service/AccountService.java` | Service interface |
| `banking-core-service/src/main/java/com/bank/core/account/service/AccountServiceImpl.java` | Service implementation; groups accounts by type |
| `banking-core-service/src/main/java/com/bank/core/account/dto/AccountSummaryDto.java` | Immutable DTO (Lombok `@Value`) for a single account summary |
| `banking-core-service/src/main/java/com/bank/core/account/dto/DashboardResponseDto.java` | Immutable DTO wrapping `customerId` + `Map<AccountType, List<AccountSummaryDto>>` |
| `banking-core-service/src/main/java/com/bank/core/account/mapper/AccountMapper.java` | MapStruct mapper; masks account number |

#### New Files — Dashboard Controller
| File | Purpose |
|------|---------|
| `banking-core-service/src/main/java/com/bank/core/dashboard/controller/DashboardController.java` | `GET /api/v1/dashboard/accounts`; delegates to `AccountService` |

#### Modified Files
| File | Change |
|------|--------|
| `banking-core-service/src/main/java/com/bank/core/config/SecurityConfig.java` | Permit authenticated access to `/api/v1/dashboard/**` |
| `banking-core-service/src/main/java/com/bank/core/config/OpenApiConfig.java` | Register dashboard API group / tag |

#### New Files — Tests
| File | Purpose |
|------|---------|
| `banking-core-service/src/test/java/com/bank/core/account/service/AccountServiceImplTest.java` | Unit tests for grouping, filtering, and principal scoping |
| `banking-core-service/src/test/java/com/bank/core/dashboard/controller/DashboardControllerTest.java` | MockMvc slice tests for all ACs |
| `banking-core-service/src/test/java/com/bank/core/account/repository/AccountRepositoryIntegrationTest.java` | Testcontainers integration test for repository query |

---

## 5. Delivery Sequence
1. Flyway migration (`V3`) — unblocks all subsequent work.
2. Domain enums and `Account` entity.
3. `AccountRepository` with custom JPQL query.
4. `AccountService` interface + `AccountServiceImpl` (grouping logic).
5. MapStruct mapper with account-number masking.
6. DTOs (`AccountSummaryDto`, `DashboardResponseDto`).
7. `DashboardController` with `@PreAuthorize`.
8. Security and OpenAPI config updates.
9. Unit and integration tests.
10. Performance validation (response time assertion in integration test or load test script).