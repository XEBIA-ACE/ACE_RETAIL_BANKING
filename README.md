# ACE Retail Banking – Banking Core Service

## Build and test
Requires JDK 21 and Maven 3.9.

```bash
cd banking-core-service
mvn clean verify   # unit tests (*Test) + integration tests (*IT, H2 in MySQL mode, Flyway applied)
```

## Environment variables
| Variable | Required | Description |
|---|---|---|
| `DB_URL` | yes | MySQL JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | yes | Database credentials |
| `JWT_SECRET` | yes | HS256 bearer-token secret, at least 32 bytes |
| `SERVER_PORT` | no | HTTP port (default `8080`) |

## Smart notification history (US-009)
- `GET /api/v1/notifications/history` – signed-in customer's notifications, most recent first (bearer token with a `customerId` claim). Returns `{"notifications": []}` when there is no history.
- `PUT /internal/v1/notification-history/{sourceNotificationId}` – idempotent hook called by Smart Notifications when delivery processing completes (bearer token with scope `notification-history:write`).
- Screen: `/notification-history.html`.
- Open gaps: retention rules (T005) and NFR-001..NFR-003 thresholds (T009) are undefined; see `tasks.md`.

OpenAPI docs: `/swagger-ui.html`.
