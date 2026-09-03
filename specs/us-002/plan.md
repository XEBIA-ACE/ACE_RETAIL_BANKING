# Plan: US-002 — Real-Time Balance Display per Account

## Architecture Decisions

### AD-1: Reuse existing `listAccountsByCustomer` service method
The `AccountService.listAccountsByCustomer(String customerExternalId)` method already queries all accounts for a customer and returns `Account` domain objects that include `balance` and `currency`. Rather than introducing a separate balance-specific repository query, the new use-case method will delegate to this existing logic and map the results to a dedicated `AccountBalanceSummary` response DTO. This avoids duplication and keeps the persistence layer unchanged.

### AD-2: New dedicated endpoint rather than modifying the existing accounts list endpoint
A new endpoint `GET /api/v1/customers/{customerExternalId}/accounts/balances` is introduced. This keeps the existing `GET /api/v1/customers/{customerExternalId}/accounts` contract stable (no breaking change for existing consumers) and gives the dashboard a single, purpose-built call for balance data. The two endpoints may converge in a future story if the accounts list is also updated to include balance inline.

### AD-3: No application-level caching on the balance path
Per FR-3 and AC-1, balances must reflect the live database state. Spring's `@Transactional(readOnly = true)` ensures a consistent read within the transaction. No `@Cacheable` annotation is applied to this path. Database-level connection pooling (HikariCP, already configured) is sufficient to meet the 2-second p95 SLA for typical customer account counts.

### AD-4: DTO mapping in the controller adapter layer
Mapping from `Account` domain objects to `AccountBalanceSummary` response DTOs happens in the `AccountController` (adapter/in/web layer), consistent with the existing pattern in the codebase. The domain model is never serialised directly to JSON.

---

## Files / Classes to Change

### New Files

| File | Purpose |
|------|---------|
| `banking-core-service/src/main/java/com/banking/adapter/in/web/dto/AccountBalanceSummaryResponse.java` | Response DTO: `accountExternalId`, `accountNumber`, `accountType`, `balance`, `currency`, `status`. |
| `banking-core-service/src/test/java/com/banking/adapter/in/web/AccountBalanceControllerTest.java` | `@WebMvcTest` tests for the new balance endpoint (happy path: single account, multiple accounts, 404 on unknown customer). |
| `banking-core-service/src/test/java/com/banking/application/service/AccountBalanceServiceTest.java` | Unit tests for the `getAccountBalances` service method. |

### Modified Files

| File | Change |
|------|--------|
| `banking-core-service/src/main/java/com/banking/application/port/in/AccountUseCase.java` | Add method signature: `List<Account> getAccountBalances(String customerExternalId)` (or reuse `listAccountsByCustomer` — confirm during implementation; if semantically identical, no change needed here). |
| `banking-core-service/src/main/java/com/banking/application/service/AccountService.java` | Add `getAccountBalances` method (delegates to `listAccountsByCustomer` or is an alias with explicit read-only transaction). |
| `banking-core-service/src/main/java/com/banking/adapter/in/web/AccountController.java` | Add `GET /api/v1/customers/{customerExternalId}/accounts/balances` handler method; map `Account` list to `AccountBalanceSummaryResponse` list. |
| `banking-core-service/src/test/java/com/banking/application/service/AccountServiceTest.java` | Add test cases for `getAccountBalances` covering single account, multiple accounts, and unknown customer (ResourceNotFoundException). |

---

## API Contract Detail

### Request
```
GET /api/v1/customers/{customerExternalId}/accounts/balances
Authorization: Bearer <token>
```

### Success Response — 200 OK
```json
[
  {
    "accountExternalId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "accountNumber": "0001234567",
    "accountType": "CHECKING",
    "balance": 1234.56,
    "currency": "USD",
    "status": "ACTIVE"
  },
  {
    "accountExternalId": "7cb92e11-1234-4321-a1b2-9d8e7f6c5b4a",
    "accountNumber": "0009876543",
    "accountType": "SAVINGS",
    "balance": 5000.00,
    "currency": "USD",
    "status": "ACTIVE"
  }
]
```

### Error Responses
- `404 Not Found` — customer `externalId` does not exist (thrown by `ResourceNotFoundException`, handled by `GlobalExceptionHandler`).
- `401 Unauthorized` — no valid bearer token (handled by security filter chain).
- `403 Forbidden` — token does not authorise access to this customer's data.

---

## Currency Formatting
The `currency` field in the response carries the ISO 4217 code (e.g., `"USD"`, `"EUR"`). The frontend (or any consumer) must use this code with a locale-aware formatter (e.g., `Intl.NumberFormat` in JavaScript, or `NumberFormat.getCurrencyInstance(Locale)` in Java) to render the correct symbol and decimal/grouping separators. The backend returns the raw `BigDecimal` value; formatting is a presentation concern.

---

## Performance Considerations
- The query path is: HTTP request → `AccountController` → `AccountService.getAccountBalances` → `AccountRepository.findByCustomerId` → single SQL `SELECT` with a `WHERE customer_id = ?` filter.
- For typical retail customers (1–10 accounts), this is a single indexed query well within the 2-second SLA.
- No N+1 query risk: all accounts are fetched in one query.
- Connection pool (HikariCP) settings in `application.yml` should be reviewed to ensure adequate pool size for concurrent dashboard loads; no code change required unless pool is under-configured.

---

## Accessibility Implementation Notes
- Balance figures must be rendered in `<span>` or `<td>` elements with sufficient colour contrast.
- Do not use colour alone to distinguish positive/negative balances; use a text prefix or `aria-label`.
- Ensure `aria-label` on balance cells includes both the numeric value and the currency (e.g., `aria-label="Balance: 1,234.56 US dollars"`).
- These are frontend concerns; the backend spec ensures the currency code is always present in the response to enable correct labelling.