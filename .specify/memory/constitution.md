# Constitution: Quality Principles & Architecture Guardrails

## 1. Technology Stack Compliance
- **Language & Framework:** Java 21 (LTS) with Spring Boot 3.3.x. All new backend code must target this stack.
- **Persistence:** Spring Data JPA 3.3.x / Hibernate 6.x over MySQL 8.0+. All schema changes must be delivered as Flyway migrations following the existing versioning sequence (V1, V2, V3…).
- **DTO Mapping:** MapStruct 1.6.x only — no manual mapping, no reflection-based mappers.
- **Boilerplate Reduction:** Lombok 1.18.x (`@Builder`, `@Data`, `@RequiredArgsConstructor`, etc.) is mandatory for entities and DTOs.
- **API Documentation:** All new REST endpoints must be annotated for Springdoc OpenAPI 2.x and visible at `/swagger-ui.html`.
- **Security:** Spring Security 6.x. Every new endpoint must be covered by the existing filter chain; no endpoint may be publicly accessible without explicit justification.
- **Validation:** Spring Validation (JSR-380) annotations on all request DTOs; never validate manually in service layer.

## 2. Coding Standards
- **Naming:** `snake_case` for SQL identifiers; `camelCase` for Java fields; `PascalCase` for classes; `UPPER_SNAKE_CASE` for constants.
- **Package Structure:** Follow the domain-per-package layout defined in `AGENTS.md` (`com.bank.core.<domain>.{controller,service,repository,domain,dto}`).
- **Interface/Implementation Split:** Every service must have a `<Name>Service` interface and a `<Name>ServiceImpl` implementation class.
- **No Business Logic in Controllers:** Controllers delegate entirely to the service layer; they only handle HTTP concerns (status codes, request/response mapping).
- **Immutability:** DTOs must be immutable (use Lombok `@Value` or Java records where appropriate).

## 3. Database / Migration Standards
- Flyway migration files: `V<n>__<snake_case_description>.sql` in `banking-core-service/src/main/resources/db/migration/`.
- `snake_case` column names; `BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY` primary keys; `TIMESTAMP NOT NULL` audit columns (`created_at`, `updated_at`).
- All foreign keys and unique constraints must be explicitly named.
- Indexes must be created for every foreign key column and every column used in `WHERE` clauses of list queries.

## 4. Testing Standards
- **Unit Tests:** JUnit 5 + Mockito 5.x for all service and controller layers; minimum 80% line coverage enforced by JaCoCo.
- **Integration Tests:** Testcontainers 1.19.x with a real MySQL container for repository and end-to-end slice tests.
- **Assertions:** AssertJ 3.x only — no JUnit `assertEquals` chains.
- Every acceptance criterion in the story must map to at least one automated test.

## 5. Non-Functional Requirements
- **NFR-001 / RB-NFR-001 — Performance:** Dashboard API response time ≤ 2 seconds (p95) under normal load; frontend time-to-interactive ≤ 3 seconds.
- **Availability:** Service must not introduce single points of failure; queries must be optimised (use indexes, avoid N+1).
- **Security:** Customer data must be scoped to the authenticated principal; no cross-customer data leakage is permitted.
- **Observability:** New endpoints must emit structured logs at INFO level for entry/exit and at ERROR level for exceptions.

## 6. Architecture Guardrails
- No direct cross-domain JPA joins; cross-domain data access goes through service interfaces.
- The dashboard endpoint aggregates data from existing account repositories — it must not duplicate account state.
- Response payloads must never expose internal database IDs directly; use `externalId` / UUID-based public identifiers.
- Pagination is not required for this story (account counts per customer are bounded), but the design must not preclude adding it later.

## 7. Review Standards
- All PRs require passing CI (build + tests + JaCoCo threshold).
- At least one peer review approval before merge.
- Acceptance criteria must be traceable to test method names via `@DisplayName` annotations.