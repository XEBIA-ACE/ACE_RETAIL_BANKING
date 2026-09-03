```java
package com.bank.core.account.service;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import com.bank.core.account.dto.DashboardResponseDto;
import com.bank.core.account.mapper.AccountMapper;
import com.bank.core.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public DashboardResponseDto getAccountsByCustomer(String customerExternalId) {
        log.info("Fetching accounts for customer externalId={}", customerExternalId);

        List<Account> accounts = accountRepository.findAllByCustomerExternalId(customerExternalId);

        Map<AccountType, List<AccountSummaryDto>> groupedAccounts = accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.groupingBy(AccountSummaryDto::getAccountType));

        DashboardResponseDto response = DashboardResponseDto.builder()
                .customerId(customerExternalId)
                .accounts(groupedAccounts)
                .build();

        log.info("Returning dashboard for customer externalId={} with {} account type(s)",
                customerExternalId, groupedAccounts.size());

        return response;
    }
}
```