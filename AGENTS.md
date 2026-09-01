# AGENTS.md — Banking Core Service

> **Purpose:** This file is the authoritative scaffold specification for AI coding agents working on the Banking Core Service. Follow every section exactly. Do not deviate from the conventions, structure, or constraints defined here.

---

## 1. Stack

| Technology | Version (minimum) | Role |
|---|---|---|
| Java | 21 (LTS) | Primary language |
| Spring Boot | 3.3.x | Application framework, DI, web layer |
| Spring Data JPA | 3.3.x | Repository abstraction over Hibernate |
| Hibernate | 6.x (via Spring Boot) | ORM / persistence provider |
| MySQL | 8.0+ | Primary relational database |
| Flyway | 10.x | Database schema migration |
| Spring Security | 6.x | Authentication, authorisation, method-level security |
| Spring Validation | 3.x | Bean Validation (JSR-380) |
| MapStruct | 1.6.x | DTO ↔ Entity mapping (compile-time, no reflection) |
| Lombok | 1.18.x | Boilerplate reduction (`@Builder`, `@Data`, etc.) |
| Springdoc OpenAPI | 2.x | Auto-generated OpenAPI 3 docs (`/swagger-ui.html`) |
| JUnit 5 | 5.10.x | Unit and integration test framework |
| Mockito | 5.x | Mocking in unit tests |
| Testcontainers | 1.19.x | Real MySQL container for integration tests |
| AssertJ | 3.x | Fluent assertion library |
| JaCoCo | 0.8.x | Code coverage enforcement |
| Maven | 3.9.x | Build tool and dependency management |
| Docker / Docker Compose | 24+ / 2.x | Containerisation and local orchestration |
| GitHub Actions | — | CI/CD pipeline |

---

## 2. Project Structure

```
banking-core-service/
├── .github/
│   └── workflows/
│       └── ci.yml                        # CI pipeline definition
├── docker/
│   └── mysql/
│       └── init.sql                      # Optional DB bootstrap (Flyway takes precedence)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bank/core/
│   │   │       ├── BankingCoreApplication.java          # Spring Boot entry point (@SpringBootApplication)
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java              # Spring Security filter chain and method security
│   │   │       │   ├── JpaConfig.java                   # JPA / Hibernate tuning (auditing, naming strategy)
│   │   │       │   ├── FlywayConfig.java                # Flyway bean customisation if needed
│   │   │       │   └── OpenApiConfig.java               # Springdoc grouping and security schemes
│   │   │       ├── customer/
│   │   │       │   ├── controller/
│   │   │       │   │   └── CustomerController.java      # REST endpoints for customer onboarding
│   │   │       │   ├── service/
│   │   │       │   │   ├── CustomerService.java         # Interface
│   │   │       │   │   └── CustomerServiceImpl.java     # Business logic implementation
│   │   │       │   ├── repository/
│   │   │       │   │   └── CustomerRepository.java      # Spring Data JPA repository
│   │   │       │   ├── domain/
│   │   │       │   │   └── Customer.java                # JPA entity
│   │   │       │   ├── dto/
│   │   │       │   │   ├── CustomerRequestDto.java      # Inbound payload with validation annotations
│   │   │       │   │   └── CustomerResponseDto.java     # Outbound payload
│   │   │       │   ├── mapper/
│   │   │       │   │   └── CustomerMapper.java          # MapStruct interface
│   │   │       │   └── exception/
│   │   │       │       └── CustomerNotFoundException.java
│   │   │       ├── account/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AccountController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── AccountService.java
│   │   │       │   │   └── AccountServiceImpl.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── AccountRepository.java
│   │   │       │   ├── domain/
│   │   │       │   │   └── Account.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── AccountRequestDto.java
│   │   │       │   │   └── AccountResponseDto.java
│   │   │       │   └── mapper/
│   │   │       │       └── AccountMapper.java
│   │   │       ├── transaction/
│   │   │       │   ├── controller/
│   │   │       │   │   └── TransactionController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── TransactionService.java
│   │   │       │   │   └── TransactionServiceImpl.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── TransactionRepository.java
│   │   │       │   ├── domain/
│   │   │       │   │   └── Transaction.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── TransactionRequestDto.java
│   │   │       │   │   └── TransactionResponseDto.java
│   │   │       │   └── mapper/
│   │   │       │       └── TransactionMapper.java
│   │   │       ├── loan/
│   │   │       │   ├── controller/
│   │   │       │   │   └── LoanController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── LoanService.java
│   │   │       │   │   └── LoanServiceImpl.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── LoanRepository.java
│   │   │       │   ├── domain/
│   │   │       │   │   └── Loan.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── LoanRequestDto.java
│   │   │       │   │   └── LoanResponseDto.java
│   │   │       │   └── mapper/
│   │   │       │       └── LoanMapper.java
│   │   │       ├── payment/
│   │   │       │   ├── controller/
│   │   │       │   │   └── PaymentController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── PaymentService.java
│   │   │       │   │   └── PaymentServiceImpl.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── PaymentRepository.java
│   │   │       │   ├── domain/
│   │   │       │   │   └── Payment.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── PaymentRequestDto.java
│   │   │       │   │   └── PaymentResponseDto.java
│   │   │       │   └── mapper/
│   │   │       │       └── PaymentMapper.java
│   │   │       ├── reporting/
│   │   │       │   ├── controller/
│   │   │       │   │   └── ReportController.java
│   │   │       │   ├── service/
│   │   │       │   │   ├── ReportService.java
│   │   │       │   │   └── ReportServiceImpl.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ReportRequestDto.java
│   │   │       │   │   └── ReportResponseDto.java
│   │   │       │   └── config/
│   │   │       │       └── ReportingProperties.java     # @ConfigurationProperties for report toggles
│   │   │       ├── common/
│   │   │       │   ├── audit/
│   │   │       │   │   └── AuditableEntity.java         # @MappedSuperclass with createdAt/updatedAt
│   │   │       │   ├── exception/
│   │   │       │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │   │       │   │   ├── BusinessException.java       # Base checked/unchecked hierarchy
│   │   │       │   │   └── ErrorResponseDto.java        # Standardised error envelope
│   │   │       │   ├── pagination/
│   │   │       │   │   └── PageResponseDto.java         # Generic paginated response wrapper
│   │   │       │   └── util/
│   │   │       │       └── MoneyUtils.java              # BigDecimal rounding / currency helpers
│   │   ├── resources/
│   │   │   ├── application.yml                          # Base configuration
│   │   │   ├── application-local.yml                    # Local dev overrides (never committed with secrets)
│   │   │   ├── application-prod.yml                     # Prod profile skeleton (secrets via env vars)
│   │   │   └── db/
│   │   │       └── migration/
│   │   │           ├── V1__create_customer_table.sql
│   │   │           ├── V2__create_account_table.sql
│   │   │           ├── V3__create_transaction_table.sql
│   │   │           ├── V4__create_loan_table.sql
│   │   │           └── V5__create_payment_table.sql
│   └── test/
│       ├── java/
│       │   └── com/bank/core/
│       │       ├── customer/
│       │       │   ├── controller/
│       │       │   │   └── CustomerControllerTest.java   # @WebMvcTest slice
│       │       │   ├── service/
│       │       │   │   └── CustomerServiceImplTest.java  # Pure unit test with Mockito
│       │       │   └── repository/
│       │       │       └── CustomerRepositoryIT.java     # @DataJpaTest with Testcontainers
│       │       ├── account/ ...                          # Mirror pattern for every domain
│       │       ├── transaction/ ...
│       │       ├── loan/ ...
│       │       ├── payment/ ...
│       │       ├── reporting/ ...
│       │       └── integration/
│       │           └── BankingCoreServiceIT.java         # Full @SpringBootTest with Testcontainers
│       └── resources/
│           ├── application-test.yml                      # Test-specific config (Testcontainers JDBC URL)
│           └── db/
│               └── testdata/
│                   └── insert_test_fixtures.sql          # Reusable test data
├── Dockerfile
├── docker-compose.yml
├── docker-compose.override.yml                           # Local dev mounts / hot-reload
├── .env.example                                          # Document all required env vars
├── pom.xml
├── .gitignore
├── tasks.md                                              # AGENT CREATES THIS — implementation task list
└── AGENTS.md                                             # This file
```

---

## 3. Required Workflow

The agent **must** execute steps in this exact order. Do not skip or reorder.

### Step 1 — Read and Understand Specifications
- Read every spec file provided in the repository before writing any code.
- Identify all domain entities, relationships, business rules, validation constraints, and security requirements.
- Note any ambiguities and add them as `⚠️ ASSUMPTION:` comments in `tasks.md`.

### Step 2 — Create `tasks.md`
- Create `tasks.md` at the repository root before touching any source file.
- Break the implementation into atomic tasks, each with a checkbox: `- [ ] Task description`.
- Group tasks by domain module (customer, account, transaction, loan, payment, reporting, common).
- Add a final section for Docker, CI, and documentation tasks.
- Example format:
  ```markdown
  ## Customer Module
  - [ ] Create Customer JPA entity with Flyway migration V1
  - [ ] Create CustomerRepository with custom query methods
  - [ ] Implement CustomerServiceImpl with onboarding logic
  - [ ] Create CustomerController with POST /api/v1/customers
  - [ ] Write CustomerServiceImplTest (unit)
  - [ ] Write CustomerControllerTest (@WebMvcTest)
  - [ ] Write CustomerRepositoryIT (@DataJpaTest)
  ```
- Tick each checkbox (`- [x]`) immediately after the task is fully implemented and its tests pass.

### Step 3 — Scaffold the Project
- Generate `pom.xml` with all dependencies listed in Section 1.
- Create `application.yml`, `application-test.yml`, and profile files.
- Create the full directory tree from Section 2 (empty placeholder classes are acceptable at this stage).
- Create `AuditableEntity`, `GlobalExceptionHandler`, `ErrorResponseDto`, and `MoneyUtils` first — every domain depends on them.

### Step 4 — Implement Domain Modules
Implement in this dependency order to avoid forward references:
1. `common` (audit, exception, pagination, util)
2. `customer`
3. `account`
4. `transaction`
5. `payment`
6. `loan`
7. `reporting`

For each module, implement in this layer order:
1. Flyway migration SQL
2. JPA entity (domain)
3. Repository interface
4. DTOs + MapStruct mapper
5. Service interface + implementation
6. Controller
7. Unit tests
8. Integration / slice tests

### Step 5 — Implement Cross-Cutting Concerns
- `SecurityConfig` — JWT bearer token validation, role-based access per endpoint.
- `GlobalExceptionHandler` — handle `ConstraintViolationException`, `BusinessException`, `EntityNotFoundException`, `DataIntegrityViolationException`.
- `ReportingProperties` — bind all report toggle flags via `@ConfigurationProperties(prefix = "reporting")`.

### Step 6 — Write and Run Tests
- Run: `mvn verify` — all tests must pass.
- Run: `mvn jacoco:report` — confirm ≥ 90% line and branch coverage.
- Fix any failures before proceeding.

### Step 7 — Validate the Build
```bash
mvn clean package -DskipTests          # Confirm clean compile
mvn verify                              # Full test suite + coverage gate
docker build -t banking-core-service . # Confirm image builds
docker-compose up --build -d           # Confirm services start healthy
curl http://localhost:8080/actuator/health  # Must return {"status":"UP"}
```
- All seven commands must succeed before the implementation is considered complete.

### Step 8 — Final Checklist
- [ ] `tasks.md` has every box ticked.
- [ ] No hardcoded secrets anywhere in source or config files.
- [ ] All Flyway migrations are immutable (never edit a committed migration).
- [ ] OpenAPI docs accessible at `http://localhost:8080/swagger-ui.html`.
- [ ] `README.md` updated with setup instructions and environment variable table.

---

## 4. Coding Conventions

### General
- Java 21 features are encouraged: records for DTOs, sealed classes for domain state enums, pattern matching, text blocks for SQL in tests.
- All source files must include the package declaration and organised imports (no wildcard imports).
- Maximum method length: **30 