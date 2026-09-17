package com.bank.core.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.Customer;
import com.bank.core.account.dto.BalanceUpdateMessage;
import com.bank.core.account.repository.AccountRepository;
import com.bank.core.account.repository.CustomerRepository;
import com.bank.core.account.service.BalanceService;
import com.bank.core.support.TestJwtFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BalanceWebSocketIT {

    @LocalServerPort
    private int port;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BalanceService balanceService;
    private WebSocketStompClient client;
    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        accountRepository.deleteAll();
        customerRepository.deleteAll();
        customer = customerRepository.save(Customer.builder().externalId("customer-ws")
                .firstName("Web").lastName("Socket").email("ws@example.com").status("ACTIVE")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        account = accountRepository.save(Account.builder().externalId("account-ws").customer(customer)
                .accountNumber("654321").accountType("CHECKING").balance(new BigDecimal("10.00"))
                .currency("USD").status("ACTIVE").createdAt(Instant.now()).updatedAt(Instant.now())
                .lastBalanceUpdatedAt(Instant.now()).build());
    }

    @AfterEach
    void tearDown() {
        client.stop();
    }

    @Test
    void publishesBalanceUpdateAfterChange() throws Exception {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        StompSession session = connect("customer-ws");
        session.subscribe(topic(account), handler(messages));

        balanceService.applyBalanceChange(account.getExternalId(), new BigDecimal("25.50"));

        String payload = messages.poll(5, TimeUnit.SECONDS);
        assertThat(payload).isNotNull();
        BalanceUpdateMessage message = new ObjectMapper().findAndRegisterModules()
                .readValue(payload, BalanceUpdateMessage.class);
        assertThat(message.balance()).isEqualByComparingTo("35.50");
        assertThat(message.balanceTimestamp()).isNotNull();
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    @Test
    void rejectsConnectWithoutAuthorization() {
        CompletableFuture<StompSession> future = client.connectAsync(
                "ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), new StompHeaders(),
                new StompSessionHandlerAdapter() { });

        assertThatThrownBy(() -> future.get(3, TimeUnit.SECONDS))
                .isInstanceOfAny(ExecutionException.class, java.util.concurrent.TimeoutException.class);
    }

    @Test
    void doesNotDeliverAnotherCustomersTopic() throws Exception {
        Customer otherCustomer = customerRepository.save(Customer.builder().externalId("other-customer")
                .firstName("Other").lastName("Customer").email("other@example.com").status("ACTIVE")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        Account otherAccount = accountRepository.save(Account.builder().externalId("other-account")
                .customer(otherCustomer).accountNumber("777777").accountType("CHECKING")
                .balance(new BigDecimal("4.00")).currency("USD").status("ACTIVE")
                .createdAt(Instant.now()).updatedAt(Instant.now()).lastBalanceUpdatedAt(Instant.now()).build());
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        StompSession session = connect("customer-ws");
        session.subscribe(topic(otherAccount), handler(messages));

        balanceService.applyBalanceChange(otherAccount.getExternalId(), new BigDecimal("1.00"));

        assertThat(messages.poll(2, TimeUnit.SECONDS)).isNull();
        if (session.isConnected()) {
            session.disconnect();
        }
    }

    private StompSession connect(String customerId) throws Exception {
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", "Bearer " + TestJwtFactory.tokenForCustomer(customerId));
        return client.connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), headers,
                        new StompSessionHandlerAdapter() { })
                .get(3, TimeUnit.SECONDS);
    }

    private StompFrameHandler handler(BlockingQueue<String> messages) {
        return new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.add(new String((byte[]) payload, java.nio.charset.StandardCharsets.UTF_8));
            }
        };
    }

    private String topic(Account value) {
        return "/topic/accounts/" + value.getExternalId() + "/balance";
    }
}
