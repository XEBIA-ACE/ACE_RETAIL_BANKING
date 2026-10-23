# tasks.md — US-028 View and Filter Notification History in App

Spec: `specs/US-028/` (bundle `395373f0-e91b-4d10-9cbe-6825ce51d385`, Spec Agent run `ad7cb7df-59fa-59f0-96ff-d061d6d89412`).

> ⚠️ ASSUMPTION: `customer_id` on `notification_log` is the customer's external id, taken from the JWT `customerId` claim.
> ⚠️ ASSUMPTION: The baseline retention period is 90 days (`NOTIFICATION_HISTORY_RETENTION`); the same value drives read filtering and purge.
> ⚠️ ASSUMPTION: Entry points (dashboard, security center, bill payments) share one endpoint; `from` only selects the localized return link.
> ⚠️ ASSUMPTION: Timestamps use the browser's IANA time zone (`tz`), falling back to `Europe/Zurich`.
> ⚠️ ASSUMPTION: Existing error-logging components (e.g. `9500-LOG-ERROR`) are out of scope and untouched.

## Common / Scaffold
- [x] Spring Boot 3.3 / Java 21 `pom.xml`, `application.yml`, `application-test.yml`, `.env.example`
- [x] `AuditableEntity`, `GlobalExceptionHandler` (400 ProblemDetail for invalid history requests)
- [x] `SecurityConfig` — JWT bearer, `/api/v1/notifications/**` requires a `customerId` claim

## Notification History Module
- [x] T001 Flyway `V3__create_notification_log_table.sql` (customer FK, customer/time index)
- [x] T002 `NotificationLog` entity, `NotificationType` (LARGE_TRANSACTION, LOW_BALANCE, BILL_REMINDER), `NotificationCategory`
- [x] T003 `NotificationHistoryRepository` — customer- and retention-scoped query, purge query
- [x] T004 `NotificationHistoryProperties` — configurable retention (default 90d) and default time zone
- [x] T005 DTOs (`NotificationHistoryItemDto`, `NotificationHistoryResponse`, `NotificationHistoryLabelsResponse`, `NotificationHistoryQuery`)
- [x] T006 `NotificationHistoryLocalizer` — de/fr/it/en resolution and locale-aware timestamp formatting
- [x] T007 `NotificationHistoryService` / `NotificationHistoryServiceImpl` — retrieval, type filtering, retention
- [x] T008 `NotificationRetentionJob` — scheduled purge using the same retention window
- [x] T009 `NotificationHistoryController` — `GET /api/v1/notifications/history`, `GET /api/v1/notifications/history/labels`
- [x] T010 Message bundles `messages/notification-history{,_de,_fr,_it}.properties`
- [x] T011 Accessible screen `static/notification-history.html` + `js/notification-history.js` + `css/notification-history.css`

## Tests
- [x] T016 [US-028-AC1] entry points, own history, type/description/localized timestamp
- [x] T017 [US-028-AC2] type/category distinction and type filtering
- [x] T018 [US-028-AC3] retention window hiding, configuration and purge
- [x] T019 [US-028-AC4] customer isolation, token-only customer id, 401/403
- [x] T020 [US-028-AC5] de/fr/it/en labels, dates, times and time zones; bundle key parity
- [x] T021 [US-028-AC6] labels, landmarks, live regions, contrast ≥ 4.5:1, text-resize-safe CSS

## Build / Docs
- [x] `mvn clean verify` passes (H2 MySQL mode + Flyway)
- [x] `mvn clean package -DskipTests` passes
- [ ] Docker image / docker-compose health check — no Dockerfile/docker-compose in this repository baseline
- [x] README updated with US-028 endpoints and environment variables
