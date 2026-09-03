# Constitution: Quality Principles & Architecture Guardrails

## Architecture Style
- The project follows **Hexagonal Architecture (Ports & Adapters)**. All new code must respect this layering:
  - `domain/model` — pure domain entities (no framework annotations)
  - `domain/exception` — domain-specific exceptions
  - `application/port/in` — inbound use-case interfaces (commands/queries)
  - `application/port/out` — outbound repository interfaces
  - `application/service` — use-case implementations
  - `adapter/in/web` — REST controllers (thin, delegate to use cases)
  - `adapter/out/persistence` — JPA/DB adapters implementing repository ports

## Coding Standards
- **Java 17+** with records for commands/queries where applicable.
- Use **Lombok** (`@Value`, `@Builder(toBuilder = true)`, `@RequiredArgsConstructor`, `@Slf4j`) consistently with existing models.
- Domain models must remain **pure POJOs** — no JPA, Spring, or framework annotations inside `domain/model`.
- All service methods that read data must be annotated `@Transactional(readOnly = true)`.
- All write operations must be wrapped in `@Transactional`.
- Use `UUID.randomUUID().toString()` for `externalId` generation.
- Log significant operations at `INFO` level using SLF4J.
- Throw `ResourceNotFoundException` for missing entities, `BusinessRuleException` for rule violations.

## Naming Conventions
- Commands: `<Verb><Entity>Command` (e.g., `AddPayeeCommand`, `UpdatePayeeCommand`)
- Use cases (ports): `PayeeUseCase`
- Repository port: `PayeeRepository`
- Service: `PayeeService implements PayeeUseCase`
- Controller: `PayeeController`
- Persistence adapter: `PayeePersistenceAdapter`
- DB migration: `V<next>__add_payees_table.sql`

## Non-Functional Requirements
- **Response time**: All payee CRUD operations (add, edit, delete) must complete within **2 seconds** end-to-end.
- **Database**: Use Flyway migration scripts for all schema changes; never modify existing migration files.
- **Validation**: Input validation must be performed at the controller layer using Bean Validation (`@Valid`, `@NotBlank`, etc.).
- **Error handling**: All exceptions must be handled by `GlobalExceptionHandler`; no raw stack traces to clients.
- **Test coverage**: Every new service class must have a corresponding unit test class under `src/test/java`.
- **Security**: Payee operations must be scoped to the authenticated customer — no cross-customer access.
- **Idempotency**: Duplicate payee detection (same customer + account number) must raise a `BusinessRuleException`.

## Review Standards
- PRs must include unit tests for `PayeeService` covering all happy paths and key error paths.
- Acceptance criteria must be verified by automated integration or service-layer tests.
- No direct SQL in Java code — use repository port abstractions.
- Migration scripts must be reviewed for index coverage on `customer_id` and `external_id`.