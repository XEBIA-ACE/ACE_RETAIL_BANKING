# Constitution: Quality Principles & Architecture Guardrails

## Architecture Principles

### Hexagonal Architecture (Ports & Adapters)
- All business logic lives in `application/service` and `domain` packages only.
- Web adapters (`adapter/in/web`) must never contain business logic — they translate HTTP to/from domain commands/responses.
- Persistence adapters (`adapter/out/persistence`) must never be called directly from web adapters.
- New use cases must be expressed as a port interface under `application/port/in/`.

### Domain Model Integrity
- Domain models (`domain/model`) are immutable Lombok `@Value` objects with `toBuilder`.
- No JPA annotations on domain models — persistence concerns belong in the persistence adapter layer.
- Enums (`CustomerStatus`, `AccountStatus`, etc.) are the single source of truth for lifecycle states.

### API Design
- All REST endpoints follow existing conventions: `externalId` as path variable, JSON request/response bodies.
- HTTP 200 for successful reads; HTTP 404 via `ResourceNotFoundException`; HTTP 500 for unexpected failures.
- Error responses must be structured (not plain strings) and handled by `GlobalExceptionHandler`.
- The `GET /profile` endpoint must be authenticated — no unauthenticated access permitted.

## Coding Standards

### Java / Spring Boot
- Java 17+; Spring Boot conventions already in place.
- Use `@Slf4j` + `log.info/warn/error` for observability; no `System.out.println`.
- `@Transactional(readOnly = true)` on all read-only service methods.
- Lombok `@RequiredArgsConstructor` for constructor injection; no field-level `@Autowired`.
- All new classes must have Javadoc at the class level.

### Naming Conventions
- Controller methods: `getXxx`, `createXxx`, `updateXxx`, `deleteXxx`.
- Service methods: verb-noun (`findProfileByCustomerId`).
- DTOs: suffix `Request` for inbound, `Response` for outbound.
- Port interfaces: suffix `UseCase` (in-ports), `Repository` (out-ports).

### Testing
- Unit tests for every new service method using JUnit 5 + Mockito.
- Integration/controller slice tests (`@WebMvcTest`) for new endpoints.
- Test class naming: `<ClassName>Test` in the same package structure under `src/test`.
- Minimum coverage target: 80% line coverage on new code.

## Non-Functional Requirements

### Performance
- Profile page response time must be under 1 second under normal load (p95).
- The `GET /profile` endpoint must not perform N+1 queries; fetch all required data in a single service call.

### Security
- Endpoint must be protected by the existing authentication mechanism.
- No PII must appear in log statements (mask email in logs).
- Response must not expose internal database IDs (`id` field); use `externalId` only.

### Error Handling
- Never return blank or null field values silently; always surface errors via structured error response.
- `GlobalExceptionHandler` must handle `ResourceNotFoundException` → HTTP 404 and generic exceptions → HTTP 500.

### Observability
- Log at `INFO` level on successful profile fetch (include `externalId`, omit PII).
- Log at `WARN` level when profile is not found.

## Out-of-Scope Guardrails
- Name field editing (US-03) must NOT be implemented in this story.
- Account Settings and Account Actions sections are out of scope.
- No write operations (POST/PUT/PATCH/DELETE) on the profile endpoint.