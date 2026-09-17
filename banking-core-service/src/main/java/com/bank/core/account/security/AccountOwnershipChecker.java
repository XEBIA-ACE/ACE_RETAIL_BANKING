package com.bank.core.account.security;

import com.bank.core.account.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("accountOwnershipChecker")
public class AccountOwnershipChecker {

    private final AccountRepository accountRepository;

    public AccountOwnershipChecker(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Missing accounts return false so authorization does not enumerate account identifiers.
     */
    public boolean check(String accountId, Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }
        String customerId = jwtAuthentication.getToken().getClaimAsString("customerId");
        if (customerId == null) {
            return false;
        }
        return accountRepository.findWithCustomerByExternalId(accountId)
                .map(account -> account.getCustomer().getExternalId().equals(customerId))
                .orElse(false);
    }
}
