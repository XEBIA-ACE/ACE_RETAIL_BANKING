# US-028 — View and Filter Notification History in App

Source: `specs/US-028/spec.md`, `plan.md`, `tasks.md` (spec branch). Acceptance criteria US-028-AC1 … AC6.

- ⚠️ ASSUMPTION: The repository contained no application code; a minimal Spring Boot 3.3 / Java 21 scaffold
  (`pom.xml`, `BankingCoreApplication`, `SecurityConfig`, `GlobalExceptionHandler`) is created for this story only.
- ⚠️ ASSUMPTION: `V2__add_payees_table.sql` references `customers`, which no migration created; `V1__init_schema.sql`
  creates a baseline `customers` table. V2 is committed and is not edited (it uses `GENERATED ALWAYS AS IDENTITY`).
- ⚠️ ASSUMPTION: The authenticated customer is the `customer_id` claim of an HS256 JWT signed with `JWT_SECRET`.
- ⚠️ ASSUMPTION: Retention is `notification.history.retention` (default 90 days); the same cutoff drives reads and purge.
- ⚠️ ASSUMPTION: Dashboard, security center and bill-payments screens are outside this repo; they link to
  `/notification-history.html?from=<entry-point>` (links exposed by `GET /api/v1/notifications/history/labels`).
- ⚠️ ASSUMPTION: The host app stores the access token in `sessionStorage["bankingAccessToken"]`.
- ⚠️ ASSUMPTION: Integration tests use H2 in MySQL mode instead of Testcontainers (no Docker daemon in CI sandbox).

## Common
- [x] Create `pom.xml`, `BankingCoreApplication`, `application.yml`, `application-test.yml`
- [x] Create `ErrorResponseDto`, `BusinessException`, `GlobalExceptionHandler` (localized messages)
- [x] Create `SecurityConfig` (stateless JWT resource server, CSP header) and `OpenApiConfig`

## Notification Module
- [x] Flyway `V1__init_schema.sql` (customers) and `V3__create_notification_log_table.sql`
- [x] `NotificationLog` entity, `NotificationType`, `NotificationCategory`, `NotificationHistoryEntryPoint`
- [x] `NotificationHistoryRepository` customer-scoped, retention-filtered, paged query + purge
- [x] `NotificationHistoryProperties`, `NotificationRetentionPolicy`, `NotificationRetentionJob`
- [x] `NotificationHistoryLocalizer` + en/de/fr/it message bundles, locale/time-zone timestamp formatting
- [x] `NotificationHistoryService` / `NotificationHistoryServiceImpl`
- [x] `NotificationHistoryController` (`GET /api/v1/notifications/history`, `GET /api/v1/notifications/history/labels`)
- [x] Accessible UI: `notification-history.html`, `css/notification-history.css`, `js/notification-history.js`
- [x] `NotificationHistoryServiceImplTest` (unit; AC1–AC5)
- [x] `NotificationHistoryLocalizationTest` (unit; AC5)
- [x] `NotificationHistoryControllerTest` (`@WebMvcTest`; AC1, AC2, AC4, AC5)
- [x] `NotificationHistoryRepositoryIT` (`@DataJpaTest` + Flyway; AC1–AC4)
- [x] `NotificationHistoryApiIT` (`@SpringBootTest`, signed JWT; AC1–AC6)
- [x] `NotificationHistoryAccessibilityTest` (Jsoup/CSS contrast; AC1, AC2, AC4–AC6)

## Docker, CI and Documentation
- [x] `.env.example` documenting environment variables
- [x] README setup instructions and environment variable table
- [ ] Dockerfile / docker-compose (out of scope for US-028; no Docker daemon available to validate)
