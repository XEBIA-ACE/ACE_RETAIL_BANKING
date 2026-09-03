# Spec: Consolidated Account Dashboard View (US-001)

## Story Narrative
As an authenticated retail banking customer,
I want to see all my linked accounts (checking, savings, credit cards) in a single consolidated dashboard view immediately after login,
So that I can quickly understand my overall financial position without navigating between separate screens.

---

## Background & Context
The ACE Retail Banking platform currently lacks a unified post-authentication landing page that surfaces all account balances. Customers must navigate to individual product pages to check balances, creating friction and reducing engagement. This story delivers the backend API and domain model that power the consolidated dashboard. The front-end rendering layer is assumed to be a separate concern (SPA or server-rendered template) but the API contract must support it directly.

---

## Acceptance Criteria

### AC-1 — Consolidated multi-account view
**Given** an authenticated customer with at least one linked checking, savings, or credit card account  
**When** a GET request is made to `GET /api/v1/accounts/dashboard`  
**Then** the response returns HTTP 200 with a JSON payload listing all linked accounts in a single array, each entry containing `accountId`, `accountTypeName`, `maskedAccountNumber`, and `currentBalance`  
**And** no additional navigation or separate API call is needed to retrieve any of the listed accounts

### AC-2 — Empty state
**Given** an authenticated customer with no linked accounts  
**When** a GET request is made to `GET /api/v1/accounts/dashboard`  
**Then** the response returns HTTP 200 with an empty `accounts` array and `hasAccounts: false` flag  
**And** a non-empty `emptyStateMessage` string is included in the response to guide the customer

### AC-3 — Account number masking
**Given** a customer account stored in the database with a full account number  
**When** any account appears in the dashboard response  
**Then** the `maskedAccountNumber` field contains only `****` followed by the last 4 digits (e.g., `****1234`)  
**And** the full account number is never present in any field of the API response

### AC-4 — Accessibility metadata
**Given** the dashboard endpoint is called  
**When** the response is rendered by any client  
**Then** each account entry includes a human-readable `accountTypeName` (e.g., "Checking Account", "Savings Account", "Credit Card") and a `maskedAccountNumber` string  
**And** the `ariaLabel` field is populated as `"{accountTypeName} ending in {last4}"` to support screen reader announcements

### AC-5 — Performance
**Given** the account aggregation service (database) is operating under normal conditions  
**When** the dashboard endpoint is called  
**Then** the API responds in under **500 ms** at p95  
**And** the endpoint does not perform N+1 queries (accounts must be fetched in a single query per customer)

### AC-6 — Authentication guard
**Given** an unauthenticated request (no valid JWT/session)  
**When** `GET /api/v1/accounts/dashboard` is called  
**Then** the response is HTTP 401 Unauthorized  
**And** no account data is returned

### AC-7 — Extensible account type model
**Given** the system currently supports CHECKING, SAVINGS, and CREDIT_CARD account types  
**When** a new account type (e.g., MORTGAGE, INVESTMENT) needs to be introduced  
**Then** adding it requires only: (a) a new enum constant, (b) a display name mapping — no table schema change and no controller change

---

## Out of Scope
- Loan accounts
- Investment / brokerage accounts
- Personal Finance Management (PFM) categorisation
- Transaction history or transaction listing
- Account opening / creation flows
- Credit card rewards or points display
- Balance forecasting or trends
- Push notifications triggered from dashboard load

---

## Data Displayed Per Account Card

| Field | Source | Notes |
|---|---|---|
| `accountId` | `accounts.external_id` | UUID; safe to expose publicly |
| `accountTypeName` | Derived from `AccountType` enum | Human-readable, localisation-ready |
| `maskedAccountNumber` | `accounts.account_number` masked | Last 4 digits only; masking applied server-side |
| `currentBalance` | `accounts.current_balance` | Decimal, 2 decimal places |
| `currencyCode` | `accounts.currency` | ISO 4217, e.g., "USD" |
| `ariaLabel` | Derived field | `"{accountTypeName} ending in {last4}"` |

---

## API Contract

### Endpoint
```
GET /api/v1/accounts/dashboard
Authorization: Bearer <token>
```

### Success Response (HTTP 200)
```json
{
  "hasAccounts": true,
  "emptyStateMessage": null,
  "accounts": [
    {
      "accountId": "a1b2c3d4-...",
      "accountTypeName": "Checking Account",
      "maskedAccountNumber": "****4321",
      "currentBalance": 1250.00,
      "currencyCode": "USD",
      "ariaLabel": "Checking Account ending in 4321"
    },
    {
      "accountId": "e5f6g7h8-...",
      "accountTypeName": "Savings Account",
      "maskedAccountNumber": "****8765",
      "currentBalance": 5400.50,
      "currencyCode": "USD",
      "ariaLabel": "Savings Account ending in 8765"
    }
  ]
}
```

### Empty State Response (HTTP 200)
```json
{
  "hasAccounts": false,
  "emptyStateMessage": "You have no linked accounts. Please visit a branch or contact support to link your accounts.",
  "accounts": []
}
```

### Error Responses
| HTTP Status | Scenario |
|---|---|
| 401 Unauthorized | Missing or invalid authentication token |
| 403 Forbidden | Authenticated but not authorised for this resource |
| 500 Internal Server Error | Unexpected server-side failure |

---

## Cross-Service Dependencies
- **Authentication/Identity service**: Provides the authenticated customer principal (customer ID extracted from JWT or session); no direct service-to-service call needed — Spring Security populates `SecurityContext`
- **Database (MySQL 8.0+)**: `accounts` table (to be created in this story via Flyway migration)
- **Flyway**: Migration version must be `V3__` or later (V1 = init schema, V2 = payees table already exist)

---

## Non-Functional Requirements Summary
| Concern | Requirement |
|---|---|
| Security | All fields sanitised; account numbers masked server-side; 401 for unauthenticated access |
| Performance | API p95 ≤ 500 ms; no N+1 queries |
| Accessibility | `ariaLabel` field on every account entry; human-readable type names |
| Extensibility | `AccountType` enum-driven; zero schema change for new types |
| Observability | SLF4J structured logging at INFO for request/response (no PII); DEBUG for query details |