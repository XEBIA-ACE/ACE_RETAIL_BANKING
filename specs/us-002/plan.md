# Plan: View Read-Only Account Details on Profile Page (US-002)

## Architecture Decision

This feature follows the existing hexagonal architecture pattern already established in the codebase. A new `GET /profile` endpoint will be added to a new `ProfileController` (or optionally co-located in `CustomerController` — preferred: new controller for separation of concerns). The endpoint resolves the authenticated customer's `externalId` from the security context, delegates to a new `ProfileUseCase` port and its implementation in `ProfileService` (or reuses `CustomerService.findCustomerById`), and returns a `ProfileResponse` DTO. No new domain model changes are required — the existing `Customer` entity contains all necessary fields.

## Layered Change Summary

### 1. Port (In) — New Use Case Interface
**File:** `banking-core-service/src/main/java/com/banking/application/port/in/ProfileUseCase.java`  
Define a `getProfile(String customerExternalId): ProfileResponse` method. This keeps the web adapter decoupled from the service implementation and follows the existing `AccountUseCase` / `CustomerUseCase` pattern.

### 2. DTO — Response Object
**File:** `banking-core-service/src/main/java/com/banking/adapter/in/web/dto/ProfileResponse.java`  
A Lombok `@Value` record-style DTO containing: `externalId`, `name` (concatenated), `email`, `registrationDate` (ISO-8601), `accountStatus` (string representation of `CustomerStatus`). Internal `id` is never included.

### 3. Application Service — Profile Service
**File:** `banking-core-service/src/main/java/com/banking/application/service/ProfileService.java`  
Implements `ProfileUseCase`. Calls `CustomerRepository.findByExternalId(externalId)`, throws `ResourceNotFoundException` if absent, maps `Customer` → `ProfileResponse`. Annotated `@Transactional(readOnly = true)`. Logs at `INFO` on success (masking email), `WARN` on not-found.

### 4. Web Adapter — Profile Controller
**File:** `banking-core-service/src/main/java/com/banking/adapter/in/web/ProfileController.java`  
`@RestController` with `@GetMapping("/profile")`. Extracts `customerExternalId` from the security principal (Spring Security `Authentication` object). Delegates to `ProfileUseCase`. Returns `ResponseEntity<ProfileResponse>`. Error cases are handled by the existing `GlobalExceptionHandler`.

### 5. Exception Handler — Verify Coverage
**File:** `banking-core-service/src/main/java/com/banking/adapter/in/web/GlobalExceptionHandler.java`  
Verify (and add if missing) handlers for `ResourceNotFoundException` → HTTP 404 and generic `Exception` → HTTP 500 with structured JSON body matching the API contract.

### 6. Tests
- **Unit test:** `banking-core-service/src/test/java/com/banking/application/service/ProfileServiceTest.java` — success path, not-found path.
- **Controller slice test:** `banking-core-service/src/test/java/com/banking/adapter/in/web/ProfileControllerTest.java` — HTTP 200 with mocked service, HTTP 404 when service throws `ResourceNotFoundException`, HTTP 401 when unauthenticated.

## API Contract (Summary)
- `GET /profile` — authenticated, returns `ProfileResponse` JSON, HTTP 200 on success, 404 on missing customer, 500 on unexpected error.
- No request body; customer identity resolved from Bearer token / security context.

## Data Flow
```
HTTP GET /profile
  → ProfileController (extract externalId from SecurityContext)
  → ProfileUseCase.getProfile(externalId)
  → ProfileService → CustomerRepository.findByExternalId(externalId)
  → Customer domain object → ProfileResponse DTO
  → ResponseEntity<ProfileResponse> HTTP 200
```

## No Database Schema Changes
The `customers` table created by `V1__init_schema.sql` already contains all required columns (`first_name`, `last_name`, `email`, `status`, `created_at`). No Flyway migration is needed for this story.

## Security Consideration
The `externalId` must be extracted from the authenticated principal — never accepted as a query parameter or path variable from the client — to prevent horizontal privilege escalation (customer A accessing customer B's profile).