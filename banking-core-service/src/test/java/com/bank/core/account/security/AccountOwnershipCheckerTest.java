package com.bank.core.account.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.Customer;
import com.bank.core.account.repository.AccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AccountOwnershipCheckerTest {

    @Mock
    private AccountRepository accountRepository;

    @Test
    void trueWhenClaimMatches() {
        AccountOwnershipChecker checker = new AccountOwnershipChecker(accountRepository);
        Account account = Account.builder().customer(Customer.builder().externalId("customer-1").build()).build();
        when(accountRepository.findWithCustomerByExternalId("account-1")).thenReturn(Optional.of(account));

        assertThat(checker.check("account-1", jwt("customer-1"))).isTrue();
    }

    @Test
    void falseWhenClaimMismatches() {
        AccountOwnershipChecker checker = new AccountOwnershipChecker(accountRepository);
        Account account = Account.builder().customer(Customer.builder().externalId("customer-1").build()).build();
        when(accountRepository.findWithCustomerByExternalId("account-1")).thenReturn(Optional.of(account));

        assertThat(checker.check("account-1", jwt("customer-2"))).isFalse();
    }

    @Test
    void falseWhenAccountMissing() {
        AccountOwnershipChecker checker = new AccountOwnershipChecker(accountRepository);
        when(accountRepository.findWithCustomerByExternalId("missing")).thenReturn(Optional.empty());

        assertThat(checker.check("missing", jwt("customer-1"))).isFalse();
    }

    @Test
    void falseWhenAuthenticationIsNotJwt() {
        AccountOwnershipChecker checker = new AccountOwnershipChecker(accountRepository);

        assertThat(checker.check("account-1", new UsernamePasswordAuthenticationToken("user", "password")))
                .isFalse();
    }

    private JwtAuthenticationToken jwt(String customerId) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256")
                .claim("customerId", customerId).subject("user").build();
        return new JwtAuthenticationToken(jwt);
    }
}
