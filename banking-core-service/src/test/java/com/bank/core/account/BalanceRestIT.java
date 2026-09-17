package com.bank.core.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.Customer;
import com.bank.core.account.repository.AccountRepository;
import com.bank.core.account.repository.CustomerRepository;
import com.bank.core.support.TestJwtFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BalanceRestIT {

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        customer = customerRepository.save(Customer.builder().externalId("customer-rest")
                .firstName("Test").lastName("Customer").email("rest@example.com").status("ACTIVE")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        account = accountRepository.save(Account.builder().externalId("account-rest").customer(customer)
                .accountNumber("123456").accountType("CHECKING").balance(new BigDecimal("100.00"))
                .currency("USD").status("ACTIVE").createdAt(Instant.now()).updatedAt(Instant.now())
                .lastBalanceUpdatedAt(Instant.now()).build());
    }

    @Test
    void returnsBalanceAndMeetsLatencyTarget() {
        HttpEntity<Void> request = authorized("customer-rest");
        ResponseEntity<Map> first = restTemplate.exchange(url(), HttpMethod.GET, request, Map.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).containsEntry("accountId", "account-rest")
                .containsEntry("currency", "USD");
        assertThat(new BigDecimal(first.getBody().get("balance").toString()))
                .isEqualByComparingTo("100.00");
        long[] elapsed = new long[20];
        for (int i = 0; i < elapsed.length; i++) {
            long started = System.nanoTime();
            restTemplate.exchange(url(), HttpMethod.GET, request, Map.class);
            elapsed[i] = System.nanoTime() - started;
        }
        java.util.Arrays.sort(elapsed);
        assertThat(elapsed[18] / 1_000_000).isLessThan(1000);
    }

    @Test
    void rejectsDifferentCustomer() {
        ResponseEntity<String> response = restTemplate.exchange(url(), HttpMethod.GET,
                authorized("different-customer"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectsMissingToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(url(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String url() {
        return "http://localhost:" + port + "/api/accounts/" + account.getExternalId() + "/balance";
    }

    private HttpEntity<Void> authorized(String customerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TestJwtFactory.tokenForCustomer(customerId));
        return new HttpEntity<>(headers);
    }
}
