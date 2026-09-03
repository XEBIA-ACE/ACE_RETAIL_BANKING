```java
package com.banking.application.service;

import com.banking.application.port.in.PayeeUseCase;
import com.banking.application.port.in.PayeeUseCase.AddPayeeCommand;
import com.banking.application.port.in.PayeeUseCase.UpdatePayeeCommand;
import com.banking.application.port.out.CustomerRepository;
import com.banking.application.port.out.PayeeRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import com.banking.domain.model.Payee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayeeServiceTest {

    @Mock
    private PayeeRepository payeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PayeeService payeeService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final String CUSTOMER_EXTERNAL_ID = "cust-ext-001";
    private static final Long   CUSTOMER_ID          = 1L;

    private static final String PAYEE_EXTERNAL_ID    = "payee-ext-001";
    private static final Long   PAYEE_ID             = 10L;

    private static final String ACCOUNT_NUMBER       = "987654321";
    private static final String BANK_CODE            = "021000021";
    private static final String BANK_NAME            = "Chase Bank";
    private static final String PAYEE_NAME           = "John Doe";
    private static final String NICKNAME             = "John";
    private static final String CURRENCY             = "USD";

    private Customer customer;
    private Payee    existingPayee;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(CUSTOMER_ID)
                .externalId(CUSTOMER_EXTERNAL_ID)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+1234567890")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        existingPayee = Payee.builder()
                .id(PAYEE_ID)
                .externalId(PAYEE_EXTERNAL_ID)
                .customerId(CUSTOMER_ID)
                .payeeName(PAYEE_NAME)
                .accountNumber(ACCOUNT_NUMBER)
                .bankCode(BANK_CODE)
                .bankName(BANK_NAME)
                .nickname(NICKNAME)
                .currency(CURRENCY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── addPayee ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addPayee happy path: customer exists, no duplicate → payee saved and returned with UUID externalId")
    void addPayee_happyPath_savesAndReturnsPayeeWithUuidExternalId() {
        // Arrange
        AddPayeeCommand command = new AddPayeeCommand(
                CUSTOMER_EXTERNAL_ID,
                PAYEE_NAME,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                NICKNAME,
                CURRENCY
        );

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.existsByCustomerIdAndAccountNumber(CUSTOMER_ID, ACCOUNT_NUMBER))
                .thenReturn(false);
        when(payeeRepository.save(any(Payee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Payee result = payeeService.addPayee(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isNotBlank();
        // Verify it looks like a UUID
        assertThat(result.getExternalId()).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getPayeeName()).isEqualTo(PAYEE_NAME);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(result.getBankCode()).isEqualTo(BANK_CODE);
        assertThat(result.getBankName()).isEqualTo(BANK_NAME);
        assertThat(result.getNickname()).isEqualTo(NICKNAME);
        assertThat(result.getCurrency()).isEqualTo(CURRENCY);

        ArgumentCaptor<Payee> captor = ArgumentCaptor.forClass(Payee.class);
        verify(payeeRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    @DisplayName("addPayee duplicate rejection: existsByCustomerIdAndAccountNumber returns true → BusinessRuleException thrown")
    void addPayee_duplicateAccountNumber_throwsBusinessRuleException() {
        // Arrange
        AddPayeeCommand command = new AddPayeeCommand(
                CUSTOMER_EXTERNAL_ID,
                PAYEE_NAME,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                NICKNAME,
                CURRENCY
        );

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.existsByCustomerIdAndAccountNumber(CUSTOMER_ID, ACCOUNT_NUMBER))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> payeeService.addPayee(command))
                .isInstanceOf(BusinessRuleException.class);

        verify(payeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("addPayee customer not found: CustomerRepository returns empty → ResourceNotFoundException thrown")
    void addPayee_customerNotFound_throwsResourceNotFoundException() {
        // Arrange
        AddPayeeCommand command = new AddPayeeCommand(
                CUSTOMER_EXTERNAL_ID,
                PAYEE_NAME,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                NICKNAME,
                CURRENCY
        );

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> payeeService.addPayee(command))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(payeeRepository, never()).existsByCustomerIdAndAccountNumber(anyLong(), anyString());
        verify(payeeRepository, never()).save(any());
    }

    // ── updatePayee ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePayee happy path: customer and payee exist, ownership matches → updated payee returned")
    void updatePayee_happyPath_returnsUpdatedPayee() {
        // Arrange
        String updatedName     = "John Updated";
        String updatedNickname = "JohnU";

        UpdatePayeeCommand command = new UpdatePayeeCommand(
                updatedName,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                updatedNickname,
                CURRENCY
        );

        Payee updatedPayee = existingPayee.toBuilder()
                .payeeName(updatedName)
                .nickname(updatedNickname)
                .build();

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.of(existingPayee));
        when(payeeRepository.save(any(Payee.class)))
                .thenReturn(updatedPayee);

        // Act
        Payee result = payeeService.updatePayee(CUSTOMER_EXTERNAL_ID, PAYEE_EXTERNAL_ID, command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPayeeName()).isEqualTo(updatedName);
        assertThat(result.getNickname()).isEqualTo(updatedNickname);
        verify(payeeRepository).save(any(Payee.class));
    }

    @Test
    @DisplayName("updatePayee cross-customer rejection: payee customerId does not match resolved customer → ResourceNotFoundException thrown")
    void updatePayee_crossCustomerOwnershipMismatch_throwsResourceNotFoundException() {
        // Arrange
        Long   differentCustomerId         = 999L;
        String differentCustomerExternalId = "cust-ext-999";

        Customer differentCustomer = customer.toBuilder()
                .id(differentCustomerId)
                .externalId(differentCustomerExternalId)
                .build();

        UpdatePayeeCommand command = new UpdatePayeeCommand(
                PAYEE_NAME,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                NICKNAME,
                CURRENCY
        );

        // existingPayee belongs to CUSTOMER_ID (1L), but request comes from differentCustomer (999L)
        when(customerRepository.findByExternalId(differentCustomerExternalId))
                .thenReturn(Optional.of(differentCustomer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.of(existingPayee));

        // Act & Assert
        assertThatThrownBy(() ->
                payeeService.updatePayee(differentCustomerExternalId, PAYEE_EXTERNAL_ID, command))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(payeeRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePayee payee not found: findByExternalId returns empty → ResourceNotFoundException thrown")
    void updatePayee_payeeNotFound_throwsResourceNotFoundException() {
        // Arrange
        UpdatePayeeCommand command = new UpdatePayeeCommand(
                PAYEE_NAME,
                ACCOUNT_NUMBER,
                BANK_CODE,
                BANK_NAME,
                NICKNAME,
                CURRENCY
        );

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                payeeService.updatePayee(CUSTOMER_EXTERNAL_ID, PAYEE_EXTERNAL_ID, command))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(payeeRepository, never()).save(any());
    }

    // ── removePayee ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("removePayee happy path: customer and payee exist, ownership matches → deleteByExternalId called")
    void removePayee_happyPath_deletesPayee() {
        // Arrange
        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.of(existingPayee));

        // Act
        payeeService.removePayee(CUSTOMER_EXTERNAL_ID, PAYEE_EXTERNAL_ID);

        // Assert
        verify(payeeRepository).deleteByExternalId(PAYEE_EXTERNAL_ID);
    }

    @Test
    @DisplayName("removePayee not found: findByExternalId returns empty → ResourceNotFoundException thrown")
    void removePayee_payeeNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                payeeService.removePayee(CUSTOMER_EXTERNAL_ID, PAYEE_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(payeeRepository, never()).deleteByExternalId(anyString());
    }

    @Test
    @DisplayName("removePayee cross-customer rejection: ownership mismatch → ResourceNotFoundException thrown")
    void removePayee_crossCustomerOwnershipMismatch_throwsResourceNotFoundException() {
        // Arrange
        Long   differentCustomerId         = 888L;
        String differentCustomerExternalId = "cust-ext-888";

        Customer differentCustomer = customer.toBuilder()
                .id(differentCustomerId)
                .externalId(differentCustomerExternalId)
                .build();

        // existingPayee belongs to CUSTOMER_ID (1L), but request comes from differentCustomer (888L)
        when(customerRepository.findByExternalId(differentCustomerExternalId))
                .thenReturn(Optional.of(differentCustomer));
        when(payeeRepository.findByExternalId(PAYEE_EXTERNAL_ID))
                .thenReturn(Optional.of(existingPayee));

        // Act & Assert
        assertThatThrownBy(() ->
                payeeService.removePayee(differentCustomerExternalId, PAYEE_EXTERNAL_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(payeeRepository, never()).deleteByExternalId(anyString());
    }

    // ── listPayees ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listPayees: customer exists → returns list from findByCustomerId")
    void listPayees_customerExists_returnsPayeeList() {
        // Arrange
        Payee secondPayee = Payee.builder()
                .id(11L)
                .externalId(UUID.randomUUID().toString())
                .customerId(CUSTOMER_ID)
                .payeeName("Alice Wonder")
                .accountNumber("111222333")
                .bankCode("011000138")
                .bankName("Bank of America")
                .nickname("Alice")
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Payee> payees = List.of(existingPayee, secondPayee);

        when(customerRepository.findByExternalId(CUSTOMER_EXTERNAL_ID))
                .thenReturn(Optional.of(customer));
        when(payeeRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(payees);

        // Act
        List<Payee> result = payeeService.listPayees(CUSTOMER_EXTERNAL_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(existingPayee, secondPayee);
        verify(payeeRepository).findByCustomerId(CUSTOMER_ID);
    }
}
```