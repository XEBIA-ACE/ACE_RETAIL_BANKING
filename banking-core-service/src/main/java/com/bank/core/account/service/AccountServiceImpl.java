```java
package com.bank.core.account.service;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponse;
import com.bank.core.account.mapper.AccountMapper;
import com.bank.core.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String EMPTY_STATE_MESSAGE =
            "You have no linked accounts. Please visit a branch or contact support to link your accounts.";

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long customerId) {
        log.info("Fetching account dashboard for customerId={}", customerId);

        List<Account> accounts = accountRepository.findAllByCustomerId(customerId);
        List<AccountSummaryDto> accountDtos = accountMapper.toDtoList(accounts);

        boolean hasAccounts = !accountDtos.isEmpty();

        log.info("Dashboard retrieved for customerId={}: accountCount={}", customerId, accountDtos.size());

        return DashboardResponse.builder()
                .hasAccounts(hasAccounts)
                .emptyStateMessage(hasAccounts ? null : EMPTY_STATE_MESSAGE)
                .accounts(accountDtos)
                .build();
    }
}
```