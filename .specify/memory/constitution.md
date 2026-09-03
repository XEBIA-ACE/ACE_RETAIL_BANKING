# Constitution — Quality Principles & Architecture Guardrails

## Technology Stack (Authoritative)
All implementation MUST conform to the stack defined in `AGENTS.md`:
- **Java 21 (LTS)** — language level; use records, sealed classes, pattern matching where appropriate
- **Spring Boot 3.3.x** — web layer, DI, auto-configuration
- **Spring Data JPA 3.3.x / Hibernate 6.x** — persistence; no native SQL unless Flyway migration
- **MySQL 8.0+** — primary datastore; all schema changes via **Flyway 10.x** migrations
- **MapStruct 1.6.x** — ALL DTO ↔ Entity mapping; no manual field-by-field mapping in service layer
- **Lombok 1.18.x** — `@Builder`, `@Data`, `@Value`, `@Slf4j`; no Lombok on JPA entities for equals/hashCode
- **Spring Security 6.x** — method-level `@PreAuthorize`; endpoints require authentication
- **Spring Validation 3.x** — JSR-380 annotations on request DTOs; no manual null-checks
- **Springdoc OpenAPI 2.x** — all public endpoints annotated with `@Operation`, `@ApiResponse`
- **JUnit 5 / Mockito 5.x / Testcontainers 1.19.x / AssertJ 3.x** — test toolchain
- **JaCoCo 0.8.x** — minimum 80% line coverage enforced in CI

## Coding Standards
- Package structure: `com.bank.core.<domain>.(controller|service|repository|domain|dto|mapper)`
- Interfaces for all service classes; implementation suffix `Impl`
- Controllers are thin: delegate all logic to service layer
- Entities use `BIGINT` PKs with `GENERATED ALWAYS AS IDENTITY`; `snake_case` columns
- All audit columns: `created_at TIMESTAMP NOT NULL`, `updated_at TIMESTAMP NOT NULL`
- No `SELECT *`; projections via DTOs or Spring Data Projections
- `@Transactional` on service implementation methods, not interfaces
- Flyway scripts: sequential `V{n}__description.sql`; never alter existing scripts

## Security & Privacy
- Account numbers MUST be masked server-side before transmission; never expose full number in any API response
- All dashboard endpoints require a valid JWT / session; enforced via Spring Security filter chain
- `@PreAuthorize("isAuthenticated()")` minimum on all account-facing endpoints
- PII (account numbers) must not appear in logs at INFO or above

## Performance Non-Functional Requirements
- Dashboard API response time **≤ 500 ms** at p95 under normal load
- Front-end page load **< 2 s**, time-to-interactive **< 3 s** (p95) — tracked via integration/smoke test
- Database queries for account listing must use indexed columns; no full-table scans

## Accessibility
- All API responses include human-readable `accountTypeName` and `maskedAccountNumber` fields suitable for screen reader announcement
- No abbreviation-only labels; full semantic labels must be present in response metadata

## Extensibility
- `AccountType` modelled as a Java `enum` (or a reference table) so new types (e.g., LOAN, INVESTMENT) require zero structural schema changes
- The `accounts` table schema must not hard-code product-specific columns outside a nullable `metadata JSON` column

## Testing Standards
- Unit tests for all service methods; mock repository layer with Mockito
- Integration test for the GET /accounts/dashboard endpoint using Testcontainers + real MySQL
- At least one negative test: unauthenticated request returns 401
- At least one empty-state test: customer with no accounts returns 200 with empty list and empty-state flag

## Code Review Gates
- PR must pass CI (build, test, coverage ≥ 80%)
- No `@SuppressWarnings("unchecked")` without justification comment
- No hardcoded credentials or connection strings
- Flyway migration must be reviewed by a second engineer before merge