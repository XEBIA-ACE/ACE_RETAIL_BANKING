package com.bank.core.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.BalanceResponse;
import com.bank.core.account.dto.BalanceUpdateMessage;
import com.bank.core.account.event.BalanceChangedEvent;
import com.bank.core.account.exception.AccountNotFoundException;
import com.bank.core.account.mapper.BalanceMapper;
import com.bank.core.account.repository.AccountRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BalanceMapper balanceMapper;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private SimpleMeterRegistry meterRegistry;
    private BalanceServiceImpl service;
    private Account account;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new BalanceServiceImpl(accountRepository, balanceMapper, messagingTemplate,
                eventPublisher, meterRegistry);
        account = Account.builder().externalId("account-1").balance(new BigDecimal("10.00"))
                .currency("USD").lastBalanceUpdatedAt(Instant.parse("2024-01-01T00:00:00Z")).build();
    }

    @Test
    void getBalanceMapsFields() {
        BalanceResponse response = new BalanceResponse("account-1", new BigDecimal("10.00"),
                "USD", account.getLastBalanceUpdatedAt());
        when(accountRepository.findByExternalId("account-1")).thenReturn(Optional.of(account));
        when(balanceMapper.toResponse(account)).thenReturn(response);

        assertThat(service.getBalance("account-1")).isEqualTo(response);
        verify(balanceMapper).toResponse(account);
    }

    @Test
    void getBalanceUnknownThrows() {
        when(accountRepository.findByExternalId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance("missing"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found: missing");
    }

    @Test
    void applyBalanceChangeUpdatesAndPublishesEvent() {
        when(accountRepository.findByExternalId("account-1")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        BalanceResponse response = new BalanceResponse("account-1", new BigDecimal("12.34"),
                "USD", account.getLastBalanceUpdatedAt());
        when(balanceMapper.toResponse(account)).thenReturn(response);

        assertThat(service.applyBalanceChange("account-1", new BigDecimal("2.345")))
                .isEqualTo(response);
        assertThat(account.getBalance()).isEqualByComparingTo("12.34");
        assertThat(account.getLastBalanceUpdatedAt()).isAfter(Instant.parse("2024-01-01T00:00:00Z"));
        verify(eventPublisher).publishEvent(new BalanceChangedEvent("account-1"));
    }

    @Test
    void publishBalanceUpdateSendsMessageAndIncrementsCounter() {
        BalanceUpdateMessage message = new BalanceUpdateMessage("account-1", account.getBalance(),
                "USD", account.getLastBalanceUpdatedAt());
        when(accountRepository.findByExternalId("account-1")).thenReturn(Optional.of(account));
        when(balanceMapper.toUpdateMessage(account)).thenReturn(message);

        service.publishBalanceUpdate("account-1");

        verify(messagingTemplate).convertAndSend(eq("/topic/accounts/account-1/balance"), eq(message));
        assertThat(meterRegistry.get("balance.updates.published").counter().count()).isEqualTo(1);
    }
}
