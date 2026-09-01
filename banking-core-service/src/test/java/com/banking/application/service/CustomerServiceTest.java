package com.banking.application.service;

import com.banking.application.port.in.CustomerUseCase;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerUseCase.OnboardCustomerCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new CustomerUseCase.OnboardCustomerCommand(
                "Jane", "Doe", "jane.doe@example.com", "+1-555-0100"
        );
    }

    @Test
    @DisplayName("onboardCustomer — creates and returns a new ACTIVE customer")
    void onboardCustomer_createsActiveCustomer() {
        when(customerRepository.existsByEmail(validCommand.email())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return c.toBuilder().id(1L).build();
        });

        Customer result = customerService.onboardCustomer(validCommand);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.getExternalId()).isNotBlank();
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("onboardCustomer — throws BusinessRuleException when email already exists")
    void onboardCustomer_throwsWhenEmailDuplicate() {
        when(customerRepository.existsByEmail(validCommand.email())).thenReturn(true);

        assertThatThrownBy(() -> customerService.onboardCustomer(validCommand))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("jane.doe@example.com");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("findCustomerById — returns empty Optional when not found")
    void findCustomerById_returnsEmptyWhenNotFound() {
        when(customerRepository.findByExternalId("unknown-id")).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findCustomerById("unknown-id");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findCustomerById — returns customer when found")
    void findCustomerById_returnsCustomerWhenFound() {
        Customer existing = buildCustomer();
        when(customerRepository.findByExternalId(existing.getExternalId()))
                .thenReturn(Optional.of(existing));

        Optional<Customer> result = customerService.findCustomerById(existing.getExternalId());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    @DisplayName("deactivateCustomer — sets status to INACTIVE")
    void deactivateCustomer_setsStatusInactive() {
        Customer existing = buildCustomer();
        when(customerRepository.findByExternalId(existing.getExternalId()))
                .thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerService.deactivateCustomer(existing.getExternalId());

        verify(customerRepository).save(argThat(c -> c.getStatus() == CustomerStatus.INACTIVE));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Customer buildCustomer() {
        return Customer.builder()
                .id(1L)
                .externalId("ext-001")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("+1-555-0100")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
