```java
package com.bank.core.account.mapper;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.AccountSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "accountId", source = "externalId")
    @Mapping(target = "accountTypeName", expression = "java(account.getAccountType().getDisplayName())")
    @Mapping(target = "maskedAccountNumber", expression = "java(com.bank.core.common.util.MaskingUtils.maskAccountNumber(account.getAccountNumber()))")
    @Mapping(target = "currentBalance", source = "currentBalance")
    @Mapping(target = "currencyCode", source = "currency")
    @Mapping(target = "ariaLabel", expression = "java(account.getAccountType().getDisplayName() + \" ending in \" + com.bank.core.common.util.MaskingUtils.lastFour(account.getAccountNumber()))")
    AccountSummaryDto toDto(Account account);

    List<AccountSummaryDto> toDtoList(List<Account> accounts);
}
```