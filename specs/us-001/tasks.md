# Tasks: US-001 — Manage Payees

## Repository: XEBIA-ACE/ACE_RETAIL_BANKING

### Domain Layer

- [ ] Create `banking-core-service/src/main/java/com/banking/domain/model/Payee.java`: implement `@Value @Builder(toBuilder = true)` domain entity with fields `id`, `externalId`, `customerId`, `payeeName`, `accountNumber`, `bankCode`, `bankName`, `nickname`, `currency`, `createdAt`, `updatedAt`

### Application Ports

- [ ] Create `banking-core-service/src/main/java/com/banking/application/port/in/PayeeUseCase.java`: define interface with methods `addPayee(AddPayeeCommand)`, `updatePayee(String customerExternalId, String payeeExternalId, UpdatePayeeCommand)`, `removePayee(String customerExternalId, String payeeExternalId)`, `listPayees(String customerExternalId)`; define `AddPayeeCommand` and `UpdatePayeeCommand` as inner records
- [ ] Create `banking-core-service/src/main/java/com/banking/application/port/out/PayeeRepository.java`: define interface with methods `save(Payee)`, `findByExternalId(String)`, `findByCustomerId(Long)`, `deleteByExternalId(String)`, `existsByCustomerIdAndAccountNumber(Long, String)`

### Application Service

- [ ] Create `banking-core-service/src/main/java/com/banking/application/service/PayeeService.java`: implement `PayeeUseCase`; inject `PayeeRepository` and `CustomerRepository`; validate customer existence; enforce duplicate detection via `existsByCustomerIdAndAccountNumber`; enforce cross-customer ownership check on update/delete; use `@Transactional` on writes and `@Transactional(readOnly = true)` on reads; log operations at INFO level

### Persistence Adapter

- [ ] Create `banking-core-service/src/main/java/com/banking/adapter/out/persistence/PayeePersistenceAdapter.java`: implement `PayeeRepository` port using Spring Data JDBC or JPA consistent with `CustomerPersistenceAdapter` and `AccountPersistenceAdapter`; map between JPA entity/row and `Payee` domain model

### Database Migration

- [ ] Create `banking-core-service/src/main/resources/db/migration/V2__add_payees_table.sql`: create `payees` table with columns matching the `Payee` domain model; add unique constraint on `(customer_id, account_number)`; add indexes on `customer_id` and `external_id`

### REST Controller

- [ ] Create `banking-core-service/src/main/java/com/banking/adapter/in/web/PayeeController.java`: expose `POST /api/v1/customers/{customerExternalId}/payees` (201), `GET /api/v1/customers/{customerExternalId}/payees` (200), `PUT /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}` (200), `DELETE /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}` (204); use `@Valid` on request bodies; define `AddPayeeRequest`, `UpdatePayeeRequest`, and `PayeeResponse` DTOs (can be inner records or separate files)
- [ ] Verify `banking-core-service/src/main/java/com/banking/adapter/in/web/GlobalExceptionHandler.java`: confirm `ResourceNotFoundException` maps to HTTP 404 and `BusinessRuleException` maps to HTTP 422; add handlers if missing

### Tests

- [ ] Create `banking-core-service/src/test/java/com/banking/application/service/PayeeServiceTest.java`: unit tests covering — add payee happy path, add payee duplicate rejection, update payee happy path, update payee cross-customer rejection, remove payee happy path, remove payee not found, list payees for customer
- [ ] Add integration/controller-level test or update existing test suite to verify all four REST endpoints return correct HTTP status codes and response bodies for both success and error scenarios

### Validation & NFR

- [ ] Add Bean Validation annotations (`@NotBlank`, `@Size`, etc.) to `AddPayeeRequest` and `UpdatePayeeRequest` DTOs to enforce required fields at the controller layer
- [ ] Review and confirm database indexes on `payees.customer_id` and `payees.external_id` in the migration script are sufficient to meet the 2-second response time NFR under expected load