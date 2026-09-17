package com.bank.core.account.service;

import com.bank.core.account.dto.BalanceResponse;
import java.math.BigDecimal;

public interface BalanceService {

    BalanceResponse getBalance(String accountId);

    void publishBalanceUpdate(String accountId);

    BalanceResponse applyBalanceChange(String accountId, BigDecimal delta);
}
