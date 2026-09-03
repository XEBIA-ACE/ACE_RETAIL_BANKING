```java
package com.bank.core.account.mapper;

import com.bank.core.account.domain.Account;
import com.bank.core.account.domain.AccountType;
import com.bank.core.account.dto.AccountSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AccountMapper}.
 *
 * <p>Uses the MapStruct-generated implementation directly (no Spring context required)
 * via {@link AccountMapperImpl}, which is generated at compile time.
 */
class AccountMapperTest {

    private AccountMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AccountMapperImpl();
    }

    // -------------------------------------------------------------------------
    // toDto — field mapping
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDto maps externalId to accountId")
    void toDto_mapsExternalIdToAccountId() {
        String externalId = UUID.randomUUID().toString();
        Account account = buildAccount(externalId, AccountType.CHECKING, "123456781234");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getAccountId()).isEqualTo(externalId);
    }

    @ParameterizedTest(name = "toDto maps accountType {0} to its display name")
    @EnumSource(AccountType.class)
    @DisplayName("toDto maps accountType display name for all types")
    void toDto_mapsAccountTypeDisplayName(AccountType accountType) {
        Account account = buildAccount(UUID.randomUUID().toString(), accountType, "987654321098");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getAccountTypeName()).isEqualTo(accountType.getDisplayName());
    }

    @Test
    @DisplayName("toDto masks account number — only last 4 digits visible")
    void toDto_maskAccountNumber() {
        Account account = buildAccount(UUID.randomUUID().toString(), AccountType.SAVINGS, "123456781234");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getMaskedAccountNumber()).isEqualTo("****1234");
    }

    @Test
    @DisplayName("toDto maps currentBalance")
    void toDto_mapsCurrentBalance() {
        BigDecimal balance = new BigDecimal("1250.75");
        Account account = buildAccount(UUID.randomUUID().toString(), AccountType.CHECKING, "000000001234");
        account.setCurrentBalance(balance);

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getCurrentBalance()).isEqualByComparingTo(balance);
    }

    @Test
    @DisplayName("toDto maps currency to currencyCode")
    void toDto_mapsCurrencyCode() {
        Account account = buildAccount(UUID.randomUUID().toString(), AccountType.CREDIT_CARD, "000000005678");
        account.setCurrency("EUR");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("toDto derives ariaLabel as '{accountTypeName} ending in {last4}'")
    void toDto_derivesAriaLabel() {
        Account account = buildAccount(UUID.randomUUID().toString(), AccountType.CHECKING, "123456784321");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getAriaLabel()).isEqualTo("Checking Account ending in 4321");
    }

    @ParameterizedTest(name = "toDto ariaLabel uses correct display name for {0}")
    @EnumSource(AccountType.class)
    @DisplayName("toDto ariaLabel follows pattern for all account types")
    void toDto_ariaLabelPatternForAllTypes(AccountType accountType) {
        Account account = buildAccount(UUID.randomUUID().toString(), accountType, "000000009999");

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getAriaLabel())
                .isEqualTo(accountType.getDisplayName() + " ending in 9999");
    }

    // -------------------------------------------------------------------------
    // toDto — security: full account number must NOT appear in any DTO field
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDto — full account number is absent from every DTO field")
    void toDto_fullAccountNumberAbsentFromAllFields() {
        String fullAccountNumber = "1234567890123456";
        Account account = buildAccount(UUID.randomUUID().toString(), AccountType.SAVINGS, fullAccountNumber);

        AccountSummaryDto dto = mapper.toDto(account);

        assertThat(dto.getAccountId()).doesNotContain(fullAccountNumber);
        assertThat(dto.getAccountTypeName()).doesNotContain(fullAccountNumber);
        assertThat(dto.getMaskedAccountNumber()).doesNotContain(fullAccountNumber);
        assertThat(dto.getCurrencyCode()).doesNotContain(fullAccountNumber);
        assertThat(dto.getAriaLabel()).doesNotContain(fullAccountNumber);
    }

    // -------------------------------------------------------------------------
    // toDtoList
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDtoList maps a list of accounts in order")
    void toDtoList_mapsListPreservingOrder() {
        Account checking = buildAccount(UUID.randomUUID().toString(), AccountType.CHECKING, "111111111111");
        Account savings  = buildAccount(UUID.randomUUID().toString(), AccountType.SAVINGS,  "222222222222");
        Account credit   = buildAccount(UUID.randomUUID().toString(), AccountType.CREDIT_CARD, "333333333333");

        List<AccountSummaryDto> dtos = mapper.toDtoList(List.of(checking, savings, credit));

        assertThat(dtos).hasSize(3);
        assertThat(dtos.get(0).getAccountId()).isEqualTo(checking.getExternalId());
        assertThat(dtos.get(1).getAccountId()).isEqualTo(savings.getExternalId());
        assertThat(dtos.get(2).getAccountId()).isEqualTo(credit.getExternalId());
    }

    @Test
    @DisplayName("toDtoList returns empty list for empty input")
    void toDtoList_emptyInputReturnsEmptyList() {
        List<AccountSummaryDto> dtos = mapper.toDtoList(List.of());

        assertThat(dtos).isEmpty();
    }

    @Test
    @DisplayName("toDtoList — full account numbers absent from all mapped DTOs")
    void toDtoList_fullAccountNumbersAbsentFromAllDtos() {
        String num1 = "1234567890000001";
        String num2 = "1234567890000002";
        Account a1 = buildAccount(UUID.randomUUID().toString(), AccountType.CHECKING, num1);
        Account a2 = buildAccount(UUID.randomUUID().toString(), AccountType.SAVINGS,  num2);

        List<AccountSummaryDto> dtos = mapper.toDtoList(List.of(a1, a2));

        for (AccountSummaryDto dto : dtos) {
            assertThat(dto.getMaskedAccountNumber()).doesNotContain(num1).doesNotContain(num2);
            assertThat(dto.getAriaLabel()).doesNotContain(num1).doesNotContain(num2);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Account buildAccount(String externalId, AccountType accountType, String accountNumber) {
        return Account.builder()
                .id(1L)
                .externalId(externalId)
                .customerId(42L)
                .accountType(accountType)
                .accountNumber(accountNumber)
                .currentBalance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
```