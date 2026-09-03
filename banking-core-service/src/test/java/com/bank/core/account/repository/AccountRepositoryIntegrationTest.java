package com.bank.core.account.repository;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.AccountStatus;
import com.bank.core.account.domain.AccountType;
import com.bank.core.customer.domain.Customer;
import com.bank.core.customer.repository.CustomerRepository;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testcontainers integration tests for {@link AccountRepository}.
 *
 * <p>A real MySQL 8.0 container is started once for the entire test class.
 * Flyway migrations V1, V2, and V3 run automatically before any test executes,
 * ensuring the schema (including CHECK constraints and indexes) is identical to
 * the production schema.</p>
 *
 * <p>Each test method runs inside a transaction that is rolled back after the
 * test completes (default {@code @DataJpaTest} behaviour), guaranteeing full
 * test isolation without manual cleanup.</p>
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryIntegrationTest {

    // -------------------------------------------------------------------------
    // Shared MySQL container — started once, reused across all test methods
    // -------------------------------------------------------------------------

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("banking_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    /**
     * Propagates the Testcontainers JDBC URL, username, and password into the
     * Spring {@code Environment} before the application context is created.
     * Flyway picks up these properties and runs all migrations against the
     * container database.
     */
    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    // -------------------------------------------------------------------------
    // Injected collaborators
    // -------------------------------------------------------------------------

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private Customer customerAlpha;
    private Customer customerBeta;

    @BeforeEach
    void setUp() {
        customerAlpha = persistCustomer("alpha-" + UUID.randomUUID(), "Alice", "Alpha", "alice." + UUID.randomUUID() + "@example.com");
        customerBeta  = persistCustomer("beta-"  + UUID.randomUUID(), "Bob",   "Beta",  "bob."   + UUID.randomUUID() + "@example.com");
    }

    // =========================================================================
    // Test 1 — Customer isolation: only accounts for the requested customer
    // =========================================================================

    @Test
    @DisplayName("findAllByCustomerExternalId — returns only accounts belonging to the requested customer")
    void findAllByCustomerExternalId_returnsOnlyAccountsForRequestedCustomer() {
        // Arrange — two accounts for Alpha, one account for Beta
        Account alphaChecking = persistAccount(customerAlpha, AccountType.CHECKING,  AccountStatus.ACTIVE,   "ACC-A1-" + UUID.randomUUID());
        Account alphaSavings  = persistAccount(customerAlpha, AccountType.SAVINGS,   AccountStatus.ACTIVE,   "ACC-A2-" + UUID.randomUUID());
        Account betaChecking  = persistAccount(customerBeta,  AccountType.CHECKING,  AccountStatus.ACTIVE,   "ACC-B1-" + UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Account> result = accountRepository.findAllByCustomerExternalId(customerAlpha.getExternalId());

        // Assert — exactly Alpha's two accounts are returned; Beta's account is absent
        assertThat(result)
                .as("Should return exactly 2 accounts for customerAlpha")
                .hasSize(2);

        assertThat(result)
                .extracting(a -> a.getExternalId())
                .as("Returned external IDs must match Alpha's accounts only")
                .containsExactlyInAnyOrder(alphaChecking.getExternalId(), alphaSavings.getExternalId());

        assertThat(result)
                .extracting(a -> a.getExternalId())
                .as("Beta's account must NOT appear in Alpha's result")
                .doesNotContain(betaChecking.getExternalId());
    }

    // =========================================================================
    // Test 2 — Non-active accounts are included in results
    // =========================================================================

    @Test
    @DisplayName("findAllByCustomerExternalId — includes INACTIVE accounts alongside ACTIVE accounts")
    void findAllByCustomerExternalId_includesNonActiveAccounts() {
        // Arrange — one ACTIVE and one INACTIVE account for the same customer
        Account activeAccount   = persistAccount(customerAlpha, AccountType.CHECKING,  AccountStatus.ACTIVE,   "ACC-ACT-"  + UUID.randomUUID());
        Account inactiveAccount = persistAccount(customerAlpha, AccountType.SAVINGS,   AccountStatus.INACTIVE, "ACC-INACT-" + UUID.randomUUID());
        Account frozenAccount   = persistAccount(customerAlpha, AccountType.CREDIT_CARD, AccountStatus.FROZEN, "ACC-FRZ-"  + UUID.randomUUID());
        Account closedAccount   = persistAccount(customerAlpha, AccountType.SAVINGS,   AccountStatus.CLOSED,   "ACC-CLS-"  + UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Account> result = accountRepository.findAllByCustomerExternalId(customerAlpha.getExternalId());

        // Assert — all four accounts are returned regardless of status
        assertThat(result)
                .as("All accounts regardless of status must be returned")
                .hasSize(4);

        assertThat(result)
                .extracting(Account::getStatus)
                .as("Result must contain all four status values")
                .containsExactlyInAnyOrder(
                        AccountStatus.ACTIVE,
                        AccountStatus.INACTIVE,
                        AccountStatus.FROZEN,
                        AccountStatus.CLOSED);
    }

    // =========================================================================
    // Test 3 — Empty list for customer with no accounts
    // =========================================================================

    @Test
    @DisplayName("findAllByCustomerExternalId — returns empty list (not null) for customer with no accounts")
    void findAllByCustomerExternalId_returnsEmptyListForCustomerWithNoAccounts() {
        // Arrange — customerBeta has no accounts; customerAlpha has one (to confirm no cross-contamination)
        persistAccount(customerAlpha, AccountType.CHECKING, AccountStatus.ACTIVE, "ACC-ALPHA-" + UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Account> result = accountRepository.findAllByCustomerExternalId(customerBeta.getExternalId());

        // Assert — result is an empty list, never null, and no exception is thrown
        assertThat(result)
                .as("Result must be an empty list, not null")
                .isNotNull()
                .isEmpty();
    }

    // =========================================================================
    // Test 4 — Single query / no N+1 (Hibernate statistics)
    // =========================================================================

    @Test
    @DisplayName("findAllByCustomerExternalId — executes exactly one SQL query (no N+1)")
    void findAllByCustomerExternalId_executesExactlyOneQuery() {
        // Arrange — several accounts to make an N+1 scenario observable
        persistAccount(customerAlpha, AccountType.CHECKING,   AccountStatus.ACTIVE, "ACC-Q1-" + UUID.randomUUID());
        persistAccount(customerAlpha, AccountType.SAVINGS,    AccountStatus.ACTIVE, "ACC-Q2-" + UUID.randomUUID());
        persistAccount(customerAlpha, AccountType.CREDIT_CARD, AccountStatus.ACTIVE, "ACC-Q3-" + UUID.randomUUID());

        entityManager.flush();
        entityManager.clear();

        // Capture Hibernate statistics before the query
        Statistics stats = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        long queryCountBefore = stats.getQueryExecutionCount();

        // Act
        List<Account> result = accountRepository.findAllByCustomerExternalId(customerAlpha.getExternalId());

        // Assert — result is correct
        assertThat(result)
                .as("Three accounts should be returned")
                .hasSize(3);

        // Assert — exactly one additional query was fired (the JOIN FETCH query)
        long queryCountAfter = stats.getQueryExecutionCount();
        assertThat(queryCountAfter - queryCountBefore)
                .as("Exactly one SQL query must be executed — no N+1 pattern")
                .isEqualTo(1L);
    }

    // =========================================================================
    // Test 5 — CHECK constraint enforcement for invalid account_type
    // =========================================================================

    @Test
    @DisplayName("CHECK constraint — inserting an account with an invalid account_type raises a constraint violation")
    void insertAccount_withInvalidAccountType_throwsConstraintViolation() {
        // We bypass the enum mapping and use native SQL to attempt inserting an invalid value,
        // which exercises the database-level CHECK constraint defined in V3__add_accounts_table.sql.
        assertThatThrownBy(() -> {
            entityManager.getEntityManager()
                    .createNativeQuery("""
                            INSERT INTO accounts
                                (external_id, customer_id, account_type, account_number,
                                 balance, currency, status, created_at, updated_at)
                            VALUES
                                (:extId, :custId, 'INVALID_TYPE', '0000111122223333',
                                 0.0000, 'USD', 'ACTIVE', NOW(), NOW())
                            """)
                    .setParameter("extId",  "bad-type-" + UUID.randomUUID())
                    .setParameter("custId", customerAlpha.getId())
                    .executeUpdate();

            // Force the INSERT to be sent to the database so the CHECK constraint fires
            entityManager.flush();
        })
                .as("Inserting an account with an invalid account_type must throw a persistence/constraint exception")
                .isInstanceOfAny(
                        PersistenceException.class,
                        jakarta.validation.ConstraintViolationException.class,
                        org.springframework.dao.DataIntegrityViolationException.class);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Persists a {@link Customer} via {@link TestEntityManager} and returns the
     * managed instance with its generated {@code id} populated.
     */
    private Customer persistCustomer(String externalId, String firstName, String lastName, String email) {
        Customer customer = Customer.builder()
                .externalId(externalId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber("+1-555-000-0000")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return entityManager.persist(customer);
    }

    /**
     * Persists an {@link Account} linked to the given {@link Customer} via
     * {@link TestEntityManager} and returns the managed instance.
     */
    private Account persistAccount(Customer customer,
                                   AccountType accountType,
                                   AccountStatus status,
                                   String externalId) {
        Account account = Account.builder()
                .externalId(externalId)
                .customer(customer)
                .accountType(accountType)
                .accountNumber("****" + (1000 + (int) (Math.random() * 9000)))
                .balance(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .status(status)
                .nickname(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return entityManager.persist(account);
    }
}