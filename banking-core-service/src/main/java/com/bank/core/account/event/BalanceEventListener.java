package com.bank.core.account.event;

import com.bank.core.account.service.BalanceService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BalanceEventListener {

    private final BalanceService balanceService;

    public BalanceEventListener(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBalanceChanged(BalanceChangedEvent event) {
        balanceService.publishBalanceUpdate(event.accountId());
    }
}
