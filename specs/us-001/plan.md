# Plan: Consolidated Account Dashboard View (US-001)

## 1. Architecture Decisions

### 1.1 Domain Model
A new `Account` JPA entity is introduced in the `com.bank.core.account` package. The entity stores a full account number (encrypted at rest via MySQL column-level AES or application-level encryption — stored as VARCHAR(255)) and exposes it only as a masked string through the DTO layer. `AccountType` is modelled as a Java `enum` with a companion method `getDisplayName()` returning human-readable labels. This satisfies AC-7 extensibility with zero schema changes for new types.

### 1.2 Masking Strategy
Account number masking is applied exclusively inside `AccountMapper` (MapStruct), ensuring the full number is never copied into a DTO field. The masking utility method `MaskingUtils.maskAccountNumber(String)` is a static helper in the `com.bank.core.common.util` package, making it independently unit-testable and reusable across future features.

### 1.3 Query Strategy
The `AccountRepository` exposes a single JPQL method `findAllByCustomerId(Long customerId)` returning a `List<Account>`. This single query satisfies AC-5 (no N+1). The `customer_id` column is indexed in the Flyway migration, mirroring the pattern established in V2 for payees.

### 1.4 Security
The `DashboardController` endpoint is annotated `@PreAuthorize("isAuthenticated()")`. The authenticated customer's ID is resolved from the Spring Security `Authentication` principal (injected as a method parameter via `@AuthenticationPrincipal`). This ensures one customer can never retrieve another customer's accounts.

### 1.5 Response Envelope
A `DashboardResponse` DTO wraps the account list and adds `hasAccounts` (boolean) and `emptyStateMessage` (nullable String). This envelope pattern is cheap to extend and directly maps to client-side empty-state rendering without additional client logic.

---

## 2. Flyway Migration — V3

File: `banking-core-service/src/main/resources/db/migration/V3__add_accounts_table.sql`

Creates the `accounts` table with:
- `id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY` — internal PK
- `external_id VARCHAR(36) NOT NULL UNIQUE` — UUID exposed in API responses
- `customer_id BIGINT NOT NULL` — FK to `customers(id)`
- `account_type VARCHAR(50) NOT NULL` — maps to `AccountType` enum name
- `account_number VARCHAR(255) NOT NULL` — full number (application-layer masking on read)
- `current_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000`
- `currency VARCHAR(3) NOT NULL`
- `created_at TIMESTAMP NOT NULL`
- `updated_at TIMESTAMP NOT NULL`
- Constraints: `pk_accounts`, `uq_accounts_external_id`
- Indexes: `idx_accounts_customer_id`, `idx_accounts_external_id`

No join table or product-type-specific column is needed. New account types map to existing columns — zero migration required per AC-7.

---

## 3. API Contract Summary
```
GET /api/v1/accounts/dashboard
Authorization: Bearer <jwt>

200 OK  → DashboardResponse { hasAccounts, emptyStateMessage, accounts[] }
401     → unauthenticated
403     → authorised but forbidden
500     → unexpected error
```

---

## 4. Files / Classes to Create or Modify

### New Files

| File Path | Purpose |
|---|---|
| `banking-core-service/src/main/resources/db/migration/V3__add_accounts_table.sql` | Flyway migration — accounts table |
| `banking-core-service/src/main/java/com/bank/core/account/domain/Account.java` | JPA entity |
| `banking-core-service/src/main/java/com/bank/core/account/domain/AccountType.java` | Enum with display name mapping |
| `banking-core-service/src/main/java/com/bank/core/account/repository/AccountRepository.java` | Spring Data JPA repository |
| `banking-core-service/src/main/java/com/bank/core/account/dto/AccountSummaryDto.java` | Per-account response DTO |
| `banking-core-service/src/main/java/com/bank/core/account/dto/DashboardResponse.java` | Dashboard envelope DTO |
| `banking-core-service/src/main/java/com/bank/core/account/mapper/AccountMapper.java` | MapStruct mapper (masking applied here) |
| `banking-core-service/src/main/java/com/bank/core/account/service/AccountService.java` | Service interface |
| `banking-core-service/src/main/java/com/bank/core/account/service/AccountServiceImpl.java` | Service implementation |
| `banking-core-service/src/main/java/com/bank/core/account/controller/AccountController.java` | REST controller — dashboard endpoint |
| `banking-core-service/src/main/java/com/bank/core/common/util/MaskingUtils.java` | Static masking utility |
| `banking-core-service/src/test/java/com/bank/core/account/service/AccountServiceImplTest.java` | Unit tests — service layer |
| `banking-core-service/src/test/java/com/bank/core/account/controller/AccountControllerTest.java` | Unit tests — controller (MockMvc) |
| `banking-core-service/src/test/java/com/bank/core/account/integration/AccountDashboardIntegrationTest.java` | Integration test — Testcontainers |
| `banking-core-service/src/test/java/com/bank/core/common/util/MaskingUtilsTest.java` | Unit tests — masking utility |

### Modified Files

| File Path | Change |
|---|---|
| `banking-core-service/src/main/java/com/bank/core/config/SecurityConfig.java` | Permit `/api/v1/accounts/**` for authenticated users; ensure 401 on unauthenticated |
| `banking-core-service/src/main/java/com/bank/core/config/OpenApiConfig.java` | Register account dashboard endpoint in OpenAPI grouping |

---

## 5. Test Strategy

- **MaskingUtilsTest**: parameterised tests covering short numbers, exact-4, long numbers, null/blank input
- **AccountServiceImplTest**: mock `AccountRepository`; verify correct DTO mapping, empty-state logic, `hasAccounts` flag
- **AccountControllerTest**: `@WebMvcTest` with MockMvc; test 200 with accounts, 200 empty state, 401 unauthenticated
- **AccountDashboardIntegrationTest**: Testcontainers MySQL; seed customer + accounts via Flyway; assert full JSON shape, masking, `ariaLabel` field, and that no full account number appears in response body
- JaCoCo enforced at ≥ 80% line coverage; new classes expected to reach ≥ 90%