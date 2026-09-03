# Spec: US-001 — Manage Payees

## Story Narrative
As a retail banking customer, I want to add, edit, and remove payees associated with my account so that I can quickly select trusted recipients when making payments without re-entering their details each time.

## Background & Motivation
Currently the payment flow requires users to manually enter destination account details on every transaction. A managed payee list reduces friction, lowers input errors, and is a prerequisite for features such as scheduled payments and payment templates. This story introduces the foundational Payee domain entity and its full CRUD lifecycle.

---

## Acceptance Criteria

### AC-1: Add a New Payee
- **Given** an authenticated customer,
- **When** they submit a request to add a payee with valid details (payee name, account number, bank/routing details, optional nickname),
- **Then** the payee is persisted and assigned a unique `externalId`,
- **And** the payee appears in the customer's payee list,
- **And** the payee is available for selection during payment initiation.

### AC-2: Edit an Existing Payee
- **Given** an authenticated customer who owns an existing payee,
- **When** they submit updated details for that payee,
- **Then** the changes are persisted and the updated payee is returned,
- **And** the payee list reflects the new details immediately.

### AC-3: Remove a Payee
- **Given** an authenticated customer who owns an existing payee,
- **When** they request deletion of that payee,
- **Then** the payee is removed from the system,
- **And** the payee no longer appears in the customer's payee list.

### AC-4: List Payees
- **Given** an authenticated customer,
- **When** they request their payee list,
- **Then** all active payees belonging to that customer are returned.

### AC-5: Duplicate Prevention
- **Given** an authenticated customer,
- **When** they attempt to add a payee with the same account number that already exists for their account,
- **Then** the system rejects the request with a meaningful error message.

### AC-6: Cross-Customer Isolation
- **Given** a payee belonging to Customer A,
- **When** Customer B attempts to edit or delete that payee,
- **Then** the system returns a `ResourceNotFoundException` (404).

### AC-7: Performance
- All payee CRUD operations (add, edit, delete, list) must respond within **2 seconds** under normal load.

---

## Data Model: Payee

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Internal PK |
| `externalId` | `String` | UUID, public identifier |
| `customerId` | `Long` | FK → customers.id |
| `payeeName` | `String` | Display name |
| `accountNumber` | `String` | Destination account number |
| `bankCode` | `String` | Routing/sort/SWIFT code |
| `bankName` | `String` | Optional human-readable bank name |
| `nickname` | `String` | Optional user-defined alias |
| `currency` | `String` | ISO 4217 currency code |
| `createdAt` | `LocalDateTime` | |
| `updatedAt` | `LocalDateTime` | |

---

## API Endpoints (REST)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/customers/{customerExternalId}/payees` | Add a new payee |
| `GET` | `/api/v1/customers/{customerExternalId}/payees` | List all payees |
| `PUT` | `/api/v1/customers/{customerExternalId}/payees/{payeeExternalId}` | Update a payee |
| `DELETE` | `/api/v1/customers/{customerExternalId}/payees/{payeeExternalId}` | Remove a payee |

---

## Out of Scope
- Payment execution using a payee (covered by existing `PaymentService`).
- Payee approval/verification workflows (e.g., micro-deposit verification).
- Bulk import of payees.
- Payee sharing across customers.
- Audit trail / history of payee changes.
- Frontend/UI implementation.

---

## Cross-Service Dependencies
- **CustomerRepository** (existing port): Required to validate that the `customerExternalId` maps to a real, active customer before creating or listing payees.
- **PaymentService** (existing): Will consume the `Payee` entity in a future story to pre-populate payment destination fields; no changes to `PaymentService` in this story.
- **Flyway**: New migration script required to create the `payees` table.
- **GlobalExceptionHandler** (existing): Must handle `ResourceNotFoundException` and `BusinessRuleException` thrown by `PayeeService` — no changes needed if handler already covers these types.