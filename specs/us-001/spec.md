# Spec: US-001 — Consolidated Account Dashboard View

## 1. Story Narrative
As a retail banking customer, I want to see all of my accounts (checking, savings, and credit cards) displayed together in a single consolidated dashboard, so that I can get an immediate, complete picture of my financial position without navigating between separate screens.

## 2. Background & Motivation
Customers currently have no single view of their full account portfolio. This story delivers the foundational dashboard that subsequent stories (account detail drill-down US-04, transaction history US-03) will build upon. The dashboard must be accurate, performant, and visually clear.

## 3. Acceptance Criteria

### AC-1 — All held account types displayed together
**Given** a customer holds checking, savings, and credit card accounts,  
**When** the dashboard loads,  
**Then** all three account types are displayed in one consolidated view, each clearly distinguishable from the others (e.g., distinct section headers, icons, or colour coding per account type).

### AC-2 — No empty placeholders for unheld account types
**Given** a customer does not hold a savings account (or any other account type),  
**When** the dashboard loads,  
**Then** no empty placeholder, empty card, or "No accounts" section is rendered for that account type.

### AC-3 — Non-active accounts are visually distinguished, not hidden
**Given** a customer has one or more non-active accounts (e.g., status = `INACTIVE`, `FROZEN`, `CLOSED`),  
**When** the dashboard loads,  
**Then** those accounts appear in the dashboard with a clear visual indicator (e.g., greyed-out card, "Inactive" badge) and are not removed from the view.

### AC-4 — Single account type customer sees no placeholders
**Given** a customer holds only one account type (e.g., only a checking account),  
**When** the dashboard loads,  
**Then** only that account type is displayed; no sections or placeholders for savings or credit cards are rendered.

### AC-5 — Performance (NFR-001 / RB-NFR-001)
**Given** the dashboard is loaded under normal supported conditions,  
**When** page load is measured,  
**Then** the backend API response time is under 2 seconds (p95) and the frontend time-to-interactive is under 3 seconds.

## 4. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-01 | The dashboard API endpoint returns all accounts belonging to the authenticated customer, grouped by account type. |
| FR-02 | Each account record in the response includes: `externalId`, `accountType` (`CHECKING`, `SAVINGS`, `CREDIT_CARD`), `accountNumber` (masked), `balance`, `currency`, `status` (`ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED`), and `nickname` (optional). |
| FR-03 | The API must only return accounts owned by the currently authenticated customer (principal-scoped query). |
| FR-04 | Account types with zero accounts for the customer must not appear in the response payload. |
| FR-05 | The response groups accounts under their respective `accountType` key; the frontend renders sections only for keys present in the response. |
| FR-06 | Non-active accounts (`status != ACTIVE`) are included in the response with their `status` field populated; the frontend applies a visual distinction. |

## 5. Data Model Requirements
- A new `accounts` table (or extension of an existing one) must store: `id`, `external_id`, `customer_id`, `account_type`, `account_number`, `balance`, `currency`, `status`, `nickname`, `created_at`, `updated_at`.
- `account_type` is an enum: `CHECKING`, `SAVINGS`, `CREDIT_CARD`.
- `status` is an enum: `ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED`.
- Index on `customer_id` for fast per-customer queries.

## 6. API Contract (Summary)

**Endpoint:** `GET /api/v1/dashboard/accounts`  
**Auth:** Bearer JWT (Spring Security — authenticated principal)  
**Response:** `200 OK`

```json
{
  "customerId": "ext-uuid-123",
  "accounts": {
    "CHECKING": [
      {
        "externalId": "acc-uuid-001",
        "accountNumber": "****1234",
        "balance": 1500.00,
        "currency": "USD",
        "status": "ACTIVE",
        "nickname": "My Main Checking"
      }
    ],
    "CREDIT_CARD": [
      {
        "externalId": "acc-uuid-003",
        "accountNumber": "****5678",
        "balance": -250.00,
        "currency": "USD",
        "status": "INACTIVE",
        "nickname": null
      }
    ]
  }
}
```
*Note: Only account type keys present in the response are rendered by the frontend. An absent key means the customer holds no accounts of that type.*

**Error Responses:**
- `401 Unauthorized` — missing or invalid JWT.
- `403 Forbidden` — authenticated but not authorised.
- `500 Internal Server Error` — unexpected server fault.

## 7. Out of Scope
- Account types beyond `CHECKING`, `SAVINGS`, and `CREDIT_CARD`.
- Account detail drill-down (covered in US-04).
- Transaction history display (covered in US-03).
- Account creation or modification.
- Pagination of accounts (bounded per customer; design must not preclude future addition).
- Frontend framework selection (assumed to exist; this story adds the dashboard view component).

## 8. Cross-Service Dependencies
- **Authentication Service:** JWT issuance and validation — the dashboard endpoint relies on the authenticated principal's customer ID.
- **Customer Domain (`com.bank.core.customer`):** Customer entity and repository already exist; the accounts domain references `customers.id` via foreign key.
- **US-03 (Transaction History):** Will consume the same account `externalId` values produced by this story.
- **US-04 (Account Detail):** Will use the dashboard as the entry point; account `externalId` is the navigation key.

## 9. Non-Functional Requirements
| ID | Requirement | Target |
|----|-------------|--------|
| NFR-001 | API p95 response time | ≤ 2 000 ms |
| NFR-001 | Frontend time-to-interactive | ≤ 3 000 ms |
| NFR-002 | Data isolation | No cross-customer data leakage |
| NFR-003 | Observability | Structured INFO logs on entry/exit; ERROR logs on exception |