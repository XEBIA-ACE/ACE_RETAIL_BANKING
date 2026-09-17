package com.bank.core.account.service;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.BalanceResponse;
import com.bank.core.account.event.BalanceChangedEvent;
import com.bank.core.account.exception.AccountNotFoundException;
import com.bank.core.account.mapper.BalanceMapper;
import com.bank.core.account.repository.AccountRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class BalanceServiceImpl implements BalanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceServiceImpl.class);
    private static final String UPDATES_COUNTER = "balance.updates.published";
    private static final String RESPONSE_TIMER = "balance.rest.response.time";

    private final AccountRepository accountRepository;
    private final BalanceMapper balanceMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public BalanceServiceImpl(
            AccountRepository accountRepository,
            BalanceMapper balanceMapper,
            SimpMessagingTemplate messagingTemplate,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry) {
        this.accountRepository = accountRepository;
        this.balanceMapper = balanceMapper;
        this.messagingTemplate = messagingTemplate;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return balanceMapper.toResponse(findAccount(accountId));
        } finally {
            sample.stop(meterRegistry.timer(RESPONSE_TIMER));
        }
    }

    @Override
    @Transactional
    public BalanceResponse applyBalanceChange(String accountId, BigDecimal delta) {
        Account account = findAccount(accountId);
        BigDecimal updatedBalance = account.getBalance().add(delta).setScale(2, RoundingMode.HALF_EVEN);
        account.setBalance(updatedBalance);
        account.setLastBalanceUpdatedAt(Instant.now());
        Account saved = accountRepository.save(account);
        eventPublisher.publishEvent(new BalanceChangedEvent(accountId));
        return balanceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void publishBalanceUpdate(String accountId) {
        long started = System.nanoTime();
        Account account = findAccount(accountId);
        messagingTemplate.convertAndSend(
                "/topic/accounts/" + accountId + "/balance",
                balanceMapper.toUpdateMessage(account));
        meterRegistry.counter(UPDATES_COUNTER).increment();
        LOGGER.debug("Published balance update for accountId={} in {} ms",
                accountId, (System.nanoTime() - started) / 1_000_000);
    }

    private Account findAccount(String accountId) {
        return accountRepository.findByExternalId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
