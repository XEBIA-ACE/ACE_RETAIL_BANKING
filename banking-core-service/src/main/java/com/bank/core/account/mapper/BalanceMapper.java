package com.bank.core.account.mapper;

import com.bank.core.account.domain.Account;
import com.bank.core.account.dto.BalanceResponse;
import com.bank.core.account.dto.BalanceUpdateMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BalanceMapper {

    @Mapping(source = "externalId", target = "accountId")
    @Mapping(source = "lastBalanceUpdatedAt", target = "balanceTimestamp")
    BalanceResponse toResponse(Account account);

    @Mapping(source = "externalId", target = "accountId")
    @Mapping(source = "lastBalanceUpdatedAt", target = "balanceTimestamp")
    BalanceUpdateMessage toUpdateMessage(Account account);
}
