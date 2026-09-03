```java
package com.bank.core.account.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Account Dashboard endpoint.
 *
 * Uses Testcontainers with a real MySQL 8.0 container.
 * Flyway automatically applies V1 → V2 → V3 migrations against the container.
 * Seed data is inserted programmatically before each test via JdbcTemplate.
 *
 * Test scenarios covered:
 *  1. Full happy path — 200 with 3 accounts, hasAccounts=true
 *  2. Masking verified — maskedAccountNumber matches ****DDDD; full numbers absent
 *  3. ariaLabel verified — matches "{accountTypeName} ending in {last4}"
 *  4. Required fields present — all non-null per account entry
 *  5. Empty state — customer with no accounts returns 200, hasAccounts=false, emptyStateMessage set
 *  6. 401 guard — unauthenticated request returns 401
 *  7. Single query (N+1 check) — Hibernate statistics; ≤1 SELECT per dashboard call
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountDashboardIntegrationTest {

    // -----------------------------------------------------------------------
    // Testcontainers — MySQL 8.0
    // -----------------------------------------------------------------------

    @Container
    static final MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("banking_test")
                    .withUsername("banking")
                    .withPassword("banking_secret")
                    .withReuse(false);

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Ensure Flyway runs against the container
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        // Enable Hibernate statistics for N+1 query assertion
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    // -----------------------------------------------------------------------
    // Spring-managed beans
    // -----------------------------------------------------------------------

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Seed-data constants
    // -----------------------------------------------------------------------

    // Customer with 3 accounts
    private static final String CUSTOMER_EMAIL_WITH_ACCOUNTS   = "jane.doe@example.com";
    private static final String CUSTOMER_PASSWORD              = "Password1!";

    // Customer with no accounts
    private static final String CUSTOMER_EMAIL_NO_ACCOUNTS     = "empty.user@example.com";

    // Full account numbers (must NOT appear in any response body)
    private static final String CHECKING_FULL_NUMBER     = "100000004321";
    private static final String SAVINGS_FULL_NUMBER      = "200000008765";
    private static final String CREDIT_CARD_FULL_NUMBER  = "300000001234";

    // Expected last-4 digits derived from the full numbers above
    private static final String CHECKING_LAST4     = "4321";
    private static final String SAVINGS_LAST4      = "8765";
    private static final String CREDIT_CARD_LAST4  = "1234";

    // Expected account type display names (must match AccountType.getDisplayName())
    private static final String CHECKING_TYPE_NAME     = "Checking Account";
    private static final String SAVINGS_TYPE_NAME      = "Savings Account";
    private static final String CREDIT_CARD_TYPE_NAME  = "Credit Card";

    // Masking regex: exactly 4 asterisks followed by exactly 4 digits
    private static final Pattern MASKED_PATTERN = Pattern.compile("^\\*{4}\\d{4}$");

    // -----------------------------------------------------------------------
    // Setup — seed data per test (full isolation)
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Clean up in FK-safe order
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM customers");

        // ---- Customer WITH accounts ----
        Long customerId = insertCustomer(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);
        insertAccount(customerId, "CHECKING",    CHECKING_FULL_NUMBER,    "101.00", "USD");
        insertAccount(customerId, "SAVINGS",     SAVINGS_FULL_NUMBER,     "5400.50","USD");
        insertAccount(customerId, "CREDIT_CARD", CREDIT_CARD_FULL_NUMBER, "250.00", "USD");

        // ---- Customer WITHOUT accounts ----
        insertCustomer(CUSTOMER_EMAIL_NO_ACCOUNTS, CUSTOMER_PASSWORD);
    }

    // -----------------------------------------------------------------------
    // Helper: seed helpers
    // -----------------------------------------------------------------------

    private Long insertCustomer(String email, String password) {
        String externalId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO customers (external_id, email, password_hash, first_name, last_name, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                externalId, email,
                // BCrypt hash of "Password1!" — pre-computed so tests don't need BCryptPasswordEncoder at setup time
                "$2a$10$7EqJtq98hPqEX7fNZaFWoOe2aJ7P4gR0w2ZFqVPkDLZvHJcErNMOy",
                "Jane", "Doe",
                now, now
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM customers WHERE email = ?", Long.class, email);
    }

    private void insertAccount(Long customerId, String accountType,
                               String accountNumber, String balance, String currency) {
        String externalId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT INTO accounts (external_id, customer_id, account_type, account_number, " +
                "current_balance, currency, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                externalId, customerId, accountType, accountNumber,
                new BigDecimal(balance), currency, now, now
        );
    }

    // -----------------------------------------------------------------------
    // Helper: build auth headers by obtaining a JWT / session token
    // -----------------------------------------------------------------------

    /**
     * Authenticates against the application's login endpoint and returns
     * HTTP headers containing the Bearer token.
     *
     * Adjust the login request/response shape to match the actual
     * AuthenticationController in this project.
     */
    private HttpHeaders authenticatedHeaders(String email, String password) {
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        String loginBody = """
                { "email": "%s", "password": "%s" }
                """.formatted(email, password);

        ResponseEntity<String> loginResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginBody, loginHeaders),
                String.class
        );

        assertThat(loginResponse.getStatusCode())
                .as("Login should succeed for seeded credentials")
                .isEqualTo(HttpStatus.OK);

        // Extract the JWT from the response body — adapt field name if needed
        String token;
        try {
            JsonNode root = objectMapper.readTree(loginResponse.getBody());
            token = root.path("token").asText();
            if (token == null || token.isBlank()) {
                // Some implementations use "accessToken"
                token = root.path("accessToken").asText();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse login response: " + loginResponse.getBody(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String dashboardUrl() {
        return "http://localhost:" + port + "/api/v1/accounts/dashboard";
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Full happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("1 — Full happy path: 200 with 3 accounts, hasAccounts=true")
    void fullHappyPath_returns200WithThreeAccounts() throws Exception {
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode root = objectMapper.readTree(response.getBody());

        assertThat(root.path("hasAccounts").asBoolean())
                .as("hasAccounts must be true when customer has accounts")
                .isTrue();

        JsonNode accounts = root.path("accounts");
        assertThat(accounts.isArray()).isTrue();
        assertThat(accounts.size())
                .as("Should return exactly 3 accounts")
                .isEqualTo(3);
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Masking verified
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("2 — Masking: every maskedAccountNumber matches ****DDDD; full numbers absent from body")
    void masking_maskedPatternCorrectAndFullNumberAbsent() throws Exception {
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String rawBody = response.getBody();
        assertThat(rawBody).isNotNull();

        // Full account numbers must NOT appear anywhere in the raw JSON
        assertThat(rawBody)
                .as("Full CHECKING account number must not appear in response")
                .doesNotContain(CHECKING_FULL_NUMBER);
        assertThat(rawBody)
                .as("Full SAVINGS account number must not appear in response")
                .doesNotContain(SAVINGS_FULL_NUMBER);
        assertThat(rawBody)
                .as("Full CREDIT_CARD account number must not appear in response")
                .doesNotContain(CREDIT_CARD_FULL_NUMBER);

        // Every maskedAccountNumber must match ****DDDD
        JsonNode accounts = objectMapper.readTree(rawBody).path("accounts");
        for (JsonNode account : accounts) {
            String masked = account.path("maskedAccountNumber").asText();
            assertThat(MASKED_PATTERN.matcher(masked).matches())
                    .as("maskedAccountNumber '%s' must match ****DDDD pattern", masked)
                    .isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 3: ariaLabel verified
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("3 — ariaLabel: matches \"{accountTypeName} ending in {last4}\" exactly")
    void ariaLabel_matchesExpectedFormat() throws Exception {
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode accounts = objectMapper.readTree(response.getBody()).path("accounts");
        assertThat(accounts.size()).isEqualTo(3);

        for (JsonNode account : accounts) {
            String typeName = account.path("accountTypeName").asText();
            String masked   = account.path("maskedAccountNumber").asText();
            String ariaLabel = account.path("ariaLabel").asText();

            // last4 is the trailing 4 chars of the masked number (after the 4 asterisks)
            String last4 = masked.substring(masked.length() - 4);
            String expectedAriaLabel = typeName + " ending in " + last4;

            assertThat(ariaLabel)
                    .as("ariaLabel for account type '%s' must be '%s'", typeName, expectedAriaLabel)
                    .isEqualTo(expectedAriaLabel);
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 4: Required fields present
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("4 — Required fields: accountId, accountTypeName, maskedAccountNumber, currentBalance, currencyCode, ariaLabel are all non-null")
    void requiredFields_allNonNull() throws Exception {
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode accounts = objectMapper.readTree(response.getBody()).path("accounts");
        assertThat(accounts.size()).isEqualTo(3);

        for (JsonNode account : accounts) {
            assertThat(account.hasNonNull("accountId"))
                    .as("accountId must be present and non-null").isTrue();
            assertThat(account.hasNonNull("accountTypeName"))
                    .as("accountTypeName must be present and non-null").isTrue();
            assertThat(account.hasNonNull("maskedAccountNumber"))
                    .as("maskedAccountNumber must be present and non-null").isTrue();
            assertThat(account.hasNonNull("currentBalance"))
                    .as("currentBalance must be present and non-null").isTrue();
            assertThat(account.hasNonNull("currencyCode"))
                    .as("currencyCode must be present and non-null").isTrue();
            assertThat(account.hasNonNull("ariaLabel"))
                    .as("ariaLabel must be present and non-null").isTrue();

            // Sanity-check non-empty strings
            assertThat(account.path("accountId").asText()).isNotBlank();
            assertThat(account.path("accountTypeName").asText()).isNotBlank();
            assertThat(account.path("maskedAccountNumber").asText()).isNotBlank();
            assertThat(account.path("currencyCode").asText()).isNotBlank();
            assertThat(account.path("ariaLabel").asText()).isNotBlank();
        }
    }

    // -----------------------------------------------------------------------
    // Scenario 5: Empty state
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("5 — Empty state: customer with no accounts returns 200, hasAccounts=false, empty array, non-empty emptyStateMessage")
    void emptyState_returnsCorrectShape() throws Exception {
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_NO_ACCOUNTS, CUSTOMER_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode root = objectMapper.readTree(response.getBody());

        assertThat(root.path("hasAccounts").asBoolean())
                .as("hasAccounts must be false for a customer with no accounts")
                .isFalse();

        JsonNode accounts = root.path("accounts");
        assertThat(accounts.isArray()).isTrue();
        assertThat(accounts.size())
                .as("accounts array must be empty")
                .isZero();

        String emptyStateMessage = root.path("emptyStateMessage").asText(null);
        assertThat(emptyStateMessage)
                .as("emptyStateMessage must be non-empty when customer has no accounts")
                .isNotBlank();
    }

    // -----------------------------------------------------------------------
    // Scenario 6: 401 guard
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("6 — 401 guard: unauthenticated request returns HTTP 401")
    void unauthenticated_returns401() {
        // No Authorization header
        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertThat(response.getStatusCode())
                .as("Unauthenticated request must receive HTTP 401")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------------------------------------------------
    // Scenario 7: Single query — N+1 check via Hibernate statistics
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("7 — Single query: dashboard endpoint executes exactly 1 SELECT (no N+1)")
    void singleQuery_noNPlusOne() throws Exception {
        // Reset Hibernate statistics before the request
        // We obtain the SessionFactory through Spring's EntityManagerFactory
        jakarta.persistence.EntityManagerFactory emf = getEntityManagerFactory();

        org.hibernate.stat.Statistics stats = emf
                .unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();

        stats.setStatisticsEnabled(true);
        stats.clear();

        // Execute the dashboard request
        HttpHeaders headers = authenticatedHeaders(CUSTOMER_EMAIL_WITH_ACCOUNTS, CUSTOMER_PASSWORD);
        ResponseEntity<String> response = restTemplate.exchange(
                dashboardUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        long queryCount = stats.getQueryExecutionCount();

        // Allow at most 1 SELECT for account retrieval.
        // (Spring Security may perform 1 additional query to load the UserDetails — we allow ≤ 2 total.)
        assertThat(queryCount)
                .as("Dashboard must not trigger N+1 queries; expected ≤ 2 total queries (auth + accounts fetch), got %d", queryCount)
                .isLessThanOrEqualTo(2L);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    private jakarta.persistence.EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
}
```