# Banking Core Service

US-002 provides an authenticated account-balance REST snapshot and real-time STOMP updates.

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8 for local production-like runs, or Docker Compose

## Build and run

```bash
cd banking-core-service
mvn verify
export JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run
```

The packaged service listens on port 8080 by default. `docker compose --env-file .env.example up --build`
starts MySQL and the service (replace the example credentials first).

## Environment variables

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | JDBC database URL | MySQL on localhost |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | empty |
| `JWT_SECRET` | HS256 secret, at least 32 UTF-8 bytes | none (required) |
| `SERVER_PORT` | HTTP port | `8080` |

JWTs must include a `customerId` claim matching the customer's external ID. Call
`GET /api/accounts/{accountId}/balance` with `Authorization: Bearer <jwt>`.

## Dashboard and WebSocket

Open [http://localhost:8080/dashboard.html](http://localhost:8080/dashboard.html), enter a JWT and
account external ID, then select **Connect**. The page loads the REST snapshot first and subscribes
to `/topic/accounts/{accountId}/balance` through STOMP at `/ws`. Live payloads include
`accountId`, `balance`, `currency`, and ISO-8601 `balanceTimestamp`. When the feed is unavailable,
the dashboard keeps the last known value visible and retries with capped exponential backoff.

OpenAPI is available at `/swagger-ui.html`; health is available at `/actuator/health`.
