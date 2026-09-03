package com.banking.application.service;

import com.banking.adapter.in.web.dto.ProfileResponse;
import com.banking.application.port.out.CustomerRepository;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Customer;
import com.banking.domain.model.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link ProfileService}.
 * No Spring context is loaded — uses Mockito only.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ProfileService profileService;

    // -----------------------------------------------------------------------
    // Success path
    // -----------------------------------------------------------------------

    @Test
    void getProfile_returnsProfileResponse_whenCustomerExists() {
        // Arrange
        String externalId = "ext-123";
        LocalDateTime createdAt = LocalDateTime.of(2023, 4, 15, 10, 30, 0);

        Customer customer = Customer.builder()
                .id(42L)
                .externalId(externalId)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("+1-555-0100")
                .status(CustomerStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        when(customerRepository.findByExternalId(externalId))
                .thenReturn(Optional.of(customer));

        // Act
        ProfileResponse response = profileService.getProfile(externalId);

        // Assert — all five mapped fields
        assertThat(response.getExternalId()).isEqualTo(externalId);
        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(response.getRegistrationDate()).isEqualTo(createdAt.toString());
        assertThat(response.getAccountStatus()).isEqualTo(CustomerStatus.ACTIVE.name());

        // Assert — internal id must NOT be present in the response
        // ProfileResponse has no 'id' field by design; verify via reflection that no such field exists
        assertThat(response.getClass().getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("id");
    }

    // -----------------------------------------------------------------------
    // Not-found path
    // -----------------------------------------------------------------------

    @Test
    void getProfile_throwsResourceNotFoundException_whenCustomerNotFound() {
        // Arrange
        String unknownId = "unknown-id";
        when(customerRepository.findByExternalId(unknownId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> profileService.getProfile(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(unknownId);
    }
}
