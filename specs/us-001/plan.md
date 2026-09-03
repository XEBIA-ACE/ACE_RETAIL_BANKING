# Plan: US-001 — Manage Payees

## Architecture Decisions

### 1. Hexagonal Layering
The Payee feature follows the same hexagonal pattern used by `Account`, `Customer`, and `Payment`. A new `PayeeUseCase` inbound port defines the application contract; a `PayeeRepository` outbound port abstracts persistence; `PayeeService` implements the use case; `PayeePersistenceAdapter` implements the repository port using Spring Data / JDBC consistent with existing adapters; and `PayeeController` exposes the REST API.

### 2. Domain Model
`Payee` is a pure Lombok `@Value @Builder(toBuilder = true)` class in `domain/model`, mirroring `Account` and `Customer`. It carries no JPA annotations.

### 3. Database Migration
A new Flyway script `V2__add_payees_table.sql` (or the next available version number) creates the `payees` table with a unique constraint on `(customer_id, account_number)` to enforce duplicate prevention at the DB level in addition to the service-layer check. Indexes are added on `customer_id` and `external_id` for query performance.

### 4. Duplicate Detection
`PayeeService.addPayee()` calls `payeeRepository.existsByCustomerIdAndAccountNumber()` before persisting; if true, throws `BusinessRuleException`. The DB unique constraint acts as a safety net.

### 5. Cross-Customer Isolation
All `findByExternalId` lookups are followed by an ownership check comparing the resolved `customerId` against the customer resolved from `customerExternalId` in the URL path. Mismatches throw `ResourceNotFoundException`.

### 6. Performance
All read operations use `@Transactional(readOnly = true)`. The `payees` table is indexed on `customer_id` ensuring list queries are O(payees per customer). No external service calls are made during payee CRUD.

---

## API Contracts

### POST `/api/v1/customers/{customerExternalId}/payees`
**Request body:**
```json
{
  "payeeName": "John Doe",
  "accountNumber": "123456789",
  "bankCode": "021000021",
  "bankName": "Chase Bank",
  "nickname": "John",
  "currency": "USD"
}
```
**Response 201:**
```json
{
  "externalId": "uuid",
  "payeeName": "John Doe",
  "accountNumber": "123456789",
  "bankCode": "021000021",
  "bankName": "Chase Bank",
  "nickname": "John",
  "currency": "USD",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### PUT `/api/v1/customers/{customerExternalId}/payees/{payeeExternalId}`
**Request body:** same fields as POST (all optional except at least one must be present).
**Response 200:** updated payee object.

### DELETE `/api/v1/customers/{customerExternalId}/payees/{payeeExternalId}`
**Response 204:** no body.

### GET `/api/v1/customers/{customerExternalId}/payees`
**Response 200:** array of payee objects.

---

## Files / Classes to Create or Modify

### New Files
| File | Purpose |
|---|---|
| `domain/model/Payee.java` | Domain entity |
| `application/port/in/PayeeUseCase.java` | Inbound port (interface + command records) |
| `application/port/out/PayeeRepository.java` | Outbound port (interface) |
| `application/service/PayeeService.java` | Use-case implementation |
| `adapter/in/web/PayeeController.java` | REST controller |
| `adapter/out/persistence/PayeePersistenceAdapter.java` | Persistence adapter |
| `resources/db/migration/V2__add_payees_table.sql` | Flyway migration |
| `test/.../service/PayeeServiceTest.java` | Unit tests for service |

### Modified Files
| File | Change |
|---|---|
| `GlobalExceptionHandler.java` | Verify (and add if missing) handlers for `ResourceNotFoundException` and `BusinessRuleException` returning 404 and 422 respectively |
| `V1__init_schema.sql` | No change — reference only to determine next migration version |