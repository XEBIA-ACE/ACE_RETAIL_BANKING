# Quality Principles & Architecture Guardrails

## Architecture Style
- Hexagonal (Ports & Adapters) architecture must be preserved. Domain models stay pure (no JPA/framework annotations). All new code follows the existing package structure: `domain/model`, `application/service`, `application/port/in`, `application/port/out`, `adapter/in/web`, `adapter/out/persistence`.
- No business logic in controllers or persistence adapters.
- All new use-case methods must be defined as port interfaces before being implemented in services.

## Coding Standards
- Java 17+, Spring Boot conventions.
- Lombok `@Value`, `@Builder(toBuilder = true)` for domain models; `@RequiredArgsConstructor` for services.
- `@Transactional(readOnly = true)` on all read-only service methods.
- All public API methods must have Javadoc.
- No raw types; use generics and `Optional` where appropriate.
- Currency amounts: always `BigDecimal`, never `double`/`float`.
- Currency formatting: locale-aware, always include currency symbol (e.g., `NumberFormat.getCurrencyInstance`).

## API Contract Standards
- RESTful JSON endpoints; HTTP 200 for success, 404 for not found, 400 for validation errors.
- Response DTOs must not expose internal database IDs (`Long id`); use `externalId` only.
- New endpoint: `GET /api/v1/customers/{customerExternalId}/accounts/balances` returns a list of account balance summaries.
- Existing endpoint `GET /api/v1/customers/{customerExternalId}/accounts` may be enhanced to include balance inline.

## Non-Functional Requirements
- **Performance**: Balance retrieval for all accounts of a customer must complete within 2 seconds under normal load (p95).
- **Accessibility**: All balance figures rendered in the UI must meet WCAG 2.1 AA contrast ratio (≥ 4.5:1). High-contrast mode must not break balance readability.
- **Security**: Balance data is customer-scoped; no cross-customer data leakage. Authentication/authorization checks must be applied on the balance endpoint.
- **Observability**: Log balance retrieval at INFO level with customer external ID and account count; log errors at ERROR level.

## Testing Standards
- Unit tests for all new service methods using JUnit 5 + Mockito.
- Integration/controller tests using `@WebMvcTest` or `@SpringBootTest` for new endpoints.
- Test coverage for: happy path (single account), multiple accounts, correct currency formatting.
- No test should rely on real database state; use mocks or in-memory H2 (as per `application-test.yml`).

## Out-of-Scope Guardrails
- Do NOT implement stale-data/fallback handling (US-04).
- Do NOT implement aggregated balance totals (US-03).
- Do NOT modify loan, payment, or transaction flows.