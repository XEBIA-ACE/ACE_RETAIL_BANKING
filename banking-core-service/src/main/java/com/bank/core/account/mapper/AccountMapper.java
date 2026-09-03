```java
package com.bank.core.account.mapper;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.AccountSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "accountNumber", source = "accountNumber", qualifiedByName = "maskAccountNumber")
    AccountSummaryDto toDto(Account account);

    @Named("maskAccountNumber")
    default String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****" + lastFour;
    }
}
```