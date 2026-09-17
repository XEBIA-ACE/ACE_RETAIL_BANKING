# US-002 Real-Time Account Balance Display

## Common Module
- [x] Create RFC 7807 global exception handling and account not-found exception
- [x] Create Spring Boot application entry point and shared configuration

## Customer Module
- [x] Create Customer JPA entity and V1 Flyway migration

## Account Module
- [x] Create Account entity and immutable-compatible V3 Flyway migration
- [x] Create AccountRepository and balance DTOs/MapStruct mapper
- [x] Implement BalanceService and transaction/event publication flow
- [x] Implement balance REST controller and ownership security
- [x] Implement WebSocket broker, JWT channel authorization, and balance topic
- [x] Add unit, MVC, and Spring Boot REST/WebSocket integration tests

## Transaction Module
- [x] ⚠️ ASSUMPTION: No transaction module exists in the recovered project, so balance changes are applied via BalanceService.applyBalanceChange.

## Payment Module
- [x] No payment functionality is in scope for US-002.

## Loan Module
- [x] No loan functionality is in scope for US-002.

## Reporting Module
- [x] No reporting functionality is in scope for US-002.

## Frontend
- [x] ⚠️ ASSUMPTION: No frontend SPA existed, so a static dashboard page was added.
- [x] Implement dashboard REST snapshot and STOMP live balance updates with reconnect degradation state

## Docker, CI, and Documentation
- [x] Create Maven build, Dockerfile, docker-compose, and environment example
- [x] Update README with setup, API, WebSocket, and dashboard instructions
- [x] Run Maven verification and packaged endpoint smoke checks
- [x] Attempt Docker image build and record any environment limitation
- [x] Commit and push the implementation branch

## Assumptions
- [x] ⚠️ ASSUMPTION: The main branch contained no Maven project, so a minimal complete Spring Boot project was bootstrapped.
- [x] ⚠️ ASSUMPTION: Existing V2 migration is not MySQL-compatible because it uses GENERATED ALWAYS AS IDENTITY; integration tests use H2 MySQL mode instead of Testcontainers.
