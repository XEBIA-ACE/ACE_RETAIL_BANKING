# Spec: US-002 — Real-Time Balance Display per Account

## User Story
**As a** retail banking customer,  
**I want** to see the current, real-time balance for each of my accounts when I view or refresh the dashboard,  
**So that** I always have an accurate picture of my financial position without needing to navigate away or wait for a batch update.

---

## Background & Context
The dashboard currently lists accounts (via `AccountService.listAccountsByCustomer`) but does not surface the live balance alongside each account row. The `Account` domain model already carries a `balance` field (type `BigDecimal`) and a `currency` field (type `String`). The goal of this story is to expose that balance through the API and ensure it is displayed correctly in the UI, formatted with the correct currency symbol, on every dashboard load and manual refresh.

This story covers the **happy path only**. Fallback behaviour when the balance source is unavailable is deferred to US-04. Aggregated totals across accounts are deferred to US-03.

---

## Acceptance Criteria

### AC-1 — Single account balance reflects latest state
- **Given** an account's balance has changed (e.g., a transaction was recorded),  
- **When** the customer views or refreshes the dashboard,  
- **Then** the balance shown for that account equals the current value stored in the system (no caching lag on the happy path).

### AC-2 — Multiple accounts each show their own balance
- **Given** a customer has two or more accounts,  
- **When** the dashboard loads,  
- **Then** each account row displays its own balance independently, with the correct currency symbol and locale-appropriate formatting (e.g., `$1,234.56`, `€1.234,56`).

### AC-3 — Accessibility: WCAG 2.1 AA compliance
- **Given** a Retiree Customer persona using high-contrast mode (OS or browser level),  
- **When** balances are displayed on the dashboard,  
- **Then** balance figures maintain a contrast ratio of at least 4.5:1 against their background, and are not conveyed by colour alone.

### AC-4 — Performance: page load under 2 seconds
- **Given** normal operating conditions (service healthy, database responsive),  
- **When** balances are retrieved and rendered for a customer's accounts,  
- **Then** the full dashboard page load (including balance data) completes in under 2 seconds (p95).

---

## Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1 | The backend must expose a way to retrieve the current balance for all accounts belonging to a customer in a single API call. |
| FR-2 | Each balance response item must include: `accountExternalId`, `accountNumber`, `accountType`, `balance` (BigDecimal), `currency` (ISO 4217 code), `status`. |
| FR-3 | The balance value returned must be the live value from the persistence layer at the time of the request (no application-level cache on this path). |
| FR-4 | The API response must use `externalId` only; internal database IDs must not be exposed. |
| FR-5 | Currency formatting in the UI must use the currency code from the response to render the correct symbol and locale format. |
| FR-6 | A manual refresh action on the dashboard must re-invoke the balance API and update all displayed balances. |

---

## Out of Scope
- Stale-data / fallback handling when the balance source is unavailable (US-04).
- Aggregated / total balance across all accounts (US-03).
- Push/streaming balance updates (WebSocket, SSE) — polling on load/refresh is sufficient.
- Balance history or trend indicators.
- Modifying loan, payment, or transaction domain flows.

---

## Cross-Service Dependencies

| Dependency | Direction | Notes |
|------------|-----------|-------|
| `AccountRepository.findByCustomerId` | Read | Already exists; balance field already present on `Account` domain model. |
| `CustomerRepository.findByExternalId` | Read | Used to resolve customer internal ID from external ID before querying accounts. |
| Authentication / Identity service | Inbound | The balance endpoint must be protected; the authenticated principal must match the requested `customerExternalId` (or be an authorised operator). |

---

## Data Model
No schema changes are required. The `balance` and `currency` columns already exist on the `accounts` table (established in `V1__init_schema.sql`). The `Account` domain model already exposes both fields.

---

## API Contract (Happy Path)

### Endpoint
```
GET /api/v1/customers/{customerExternalId}/accounts/balances
```

### Response — 200 OK
```json
[
  {
    "accountExternalId": "uuid-string",
    "accountNumber": "1234567890",
    "accountType": "CHECKING",
    "balance": 1234.56,
    "currency": "USD",
    "status": "ACTIVE"
  }
]
```

### Error Responses
| HTTP Status | Condition |
|-------------|-----------|
| 404 | Customer `externalId` not found |
| 401 | Unauthenticated request |
| 403 | Authenticated user does not have access to this customer's data |