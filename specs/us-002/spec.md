# Spec: View Read-Only Account Details on Profile Page (US-002)

## User Story

**As** an authenticated banking customer,  
**I want** to see my account details (Name, Email Address, Registration Date, and Account Status) on my Profile page,  
**So that** I can verify my personal and account information at a glance without risk of accidental modification.

---

## Background & Context

The Profile page is the primary self-service view for a customer's identity and account metadata. This story delivers the read-only display layer only. The data is sourced from the backend via a single `GET /profile` call on page load. The existing `Customer` domain model already holds `firstName`, `lastName`, `email`, `createdAt` (Registration Date), and `status` (Account Status), making this a thin presentation layer over existing domain data.

---

## Acceptance Criteria

### AC-1: Successful Profile Load
**GIVEN** an authenticated user navigates to the Profile page,  
**WHEN** the page loads,  
**THEN**:
- A `GET /profile` HTTP request is issued to the backend.
- The response contains all four fields: `name` (full name), `email`, `registrationDate`, `accountStatus`.
- All four fields are rendered on the page with the values returned by the API.
- No loading spinner or blank state persists after the response is received.

### AC-2: Read-Only Field Enforcement
**GIVEN** the Profile page has loaded successfully,  
**WHEN** the user inspects the Email Address, Registration Date, and Account Status fields,  
**THEN**:
- All three fields are rendered as non-editable (e.g., plain text, disabled input, or read-only label).
- The Name field is also rendered as read-only in this story (editing is deferred to US-03).
- No input, textarea, or contenteditable element is present for Email, Registration Date, or Account Status.

### AC-3: API Failure — Error State
**GIVEN** `GET /profile` returns a non-2xx HTTP response or a network error,  
**WHEN** the page attempts to render user details,  
**THEN**:
- A descriptive, user-friendly error message is displayed (e.g., "Unable to load profile. Please try again later.").
- No blank field values are silently rendered.
- No stale data from a previous session or cache is displayed.
- The error message is visible without requiring the user to scroll.

### AC-4: Performance
**GIVEN** an authenticated user on the Profile page under normal load,  
**WHEN** the page renders,  
**THEN**:
- The end-to-end response time for `GET /profile` is under 1 second at p95.
- The backend service fetches all required profile data in a single database query (no N+1).

---

## API Contract

### Endpoint
```
GET /profile
Authorization: Bearer <token>
```

### Success Response — HTTP 200
```json
{
  "externalId": "uuid-string",
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "registrationDate": "2023-04-15T10:30:00",
  "accountStatus": "ACTIVE"
}
```

### Error Response — HTTP 404
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found: <externalId>"
}
```

### Error Response — HTTP 500
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred."
}
```

**Field Mapping from Domain Model:**

| Response Field    | Domain Source                          | Notes                          |
|-------------------|----------------------------------------|--------------------------------|
| `externalId`      | `Customer.externalId`                  | Never expose internal `id`     |
| `name`            | `Customer.firstName + " " + lastName`  | Concatenated full name         |
| `email`           | `Customer.email`                       | Read-only, never editable      |
| `registrationDate`| `Customer.createdAt`                   | ISO-8601 datetime              |
| `accountStatus`   | `Customer.status` (CustomerStatus enum)| ACTIVE / INACTIVE / SUSPENDED / CLOSED |

---

## Out of Scope

| Item                                      | Reason                              |
|-------------------------------------------|-------------------------------------|
| Editing the Name field                    | Deferred to US-03                   |
| Account Settings section                  | Separate story                      |
| Account Actions section                   | Separate story                      |
| Password change / security settings       | Separate story                      |
| Profile photo upload                      | Not in backlog for this sprint      |
| Pagination or multiple profiles           | Single authenticated user only      |

---

## Cross-Service Dependencies

| Dependency              | Type     | Notes                                                                 |
|-------------------------|----------|-----------------------------------------------------------------------|
| Authentication service  | Runtime  | `GET /profile` must validate the Bearer token; customer identity resolved from token claims |
| `CustomerRepository`    | Internal | Existing port; `findByExternalId` used to retrieve customer data      |
| `GlobalExceptionHandler`| Internal | Must handle `ResourceNotFoundException` → 404 and generic → 500      |
| Database (Customer table)| Internal | `customers` table already exists via `V1__init_schema.sql`           |

---

## Data & Privacy Considerations
- Email address must be masked in server-side logs (e.g., `j***@example.com`).
- Internal database `id` must never appear in the API response.
- Response must only contain data belonging to the authenticated customer (no cross-customer data leakage).