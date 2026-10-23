# Tasks: US-009 – View Smart Notification History List

Spec: `specs/US-009/{spec,plan,tasks}.md` (run `8b9cb496-7190-514c-af95-dac72a1437f9`, accepted impact
`sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`). Net-new capability; baseline `3feadfcc`.

> ⚠️ ASSUMPTION: `main` contained no application code, only `V2__add_payees_table.sql`, which references a
> `customers` table. A minimal `V1__create_customers_table.sql` and a Spring Boot scaffold were added so Flyway
> and the test suite can run.
> ⚠️ ASSUMPTION: the authenticated customer is identified by the JWT `customerId` claim; the Smart Notifications
> capability authenticates with a JWT carrying scope `notification-history:write`.
> ⚠️ ASSUMPTION: smart-notification preferences ("enabled types") are not in this repo; history lists every
> notification recorded for the customer.

## Common
- [x] Spring Boot scaffold (`pom.xml`, `application.yml`, `application-test.yml`, `.env.example`)
- [x] `SecurityConfig` (JWT HS256 resource server), `OpenApiConfig`, `AuditableEntity`, `GlobalExceptionHandler`
- [x] `V1__create_customers_table.sql` (prerequisite of existing V2)

## Notification Module (US-009)
- [x] **T001** `V3__create_notification_history_table.sql` + `NotificationHistoryRecord` entity (FR-001, FR-003)
- [x] **T002** Idempotent `PUT /internal/v1/notification-history/{sourceNotificationId}` (FR-003)
- [x] **T003** `GET /api/v1/notifications/history` – authenticated, customer-scoped, newest first, empty list on no history (FR-001, FR-002)
- [x] **T004** `notification-history.html` screen with list, neutral empty state and separate error banner (FR-001, FR-002)
- [ ] **T004** performance validation against NFR-001..NFR-003 – ⛔ BLOCKED on open gap: NFR thresholds (FR-004)
- [ ] **T005** Retention enforcement – ⛔ BLOCKED on open gap: retention rules (FR-003); `retention_expires_at` stays null

## Verification
- [x] **T006** `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-1` – `NotificationHistoryIT#us009Ac1_*`
- [x] **T007** `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-2` – `NotificationHistoryIT#us009Ac2_*`
- [ ] **T008** `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-3` – persistence/idempotency covered; retention assertion `@Disabled` (retention rules gap)
- [ ] **T009** `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-4` – implemented, skipped unless `-Dus009.nfr.max-load-ms=<NFR value>` (NFR thresholds gap)

## Build
- [x] `cd banking-core-service && mvn clean verify` passes (26 tests, 2 skipped on open gaps)
