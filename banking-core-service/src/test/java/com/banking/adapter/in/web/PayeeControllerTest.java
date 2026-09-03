```java
package com.banking.adapter.in.web;

import com.banking.application.port.in.PayeeUseCase;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Payee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level tests for {@link PayeeController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer; {@link PayeeUseCase} is mocked.
 * {@link GlobalExceptionHandler} is imported so that exception-to-HTTP-status mappings are
 * exercised exactly as they would be in production.
 */
@WebMvcTest(PayeeController.class)
@Import(GlobalExceptionHandler.class)
class PayeeControllerTest {

    private static final String BASE_URL = "/api/v1/customers/{customerExternalId}/payees";
    private static final String CUSTOMER_EXT_ID = "cust-ext-001";
    private static final String PAYEE_EXT_ID = "payee-ext-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayeeUseCase payeeUseCase;

    private Payee samplePayee;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2024, 6, 1, 12, 0, 0);
        samplePayee = Payee.builder()
                .id(1L)
                .externalId(PAYEE_EXT_ID)
                .customerId(42L)
                .payeeName("John Doe")
                .accountNumber("123456789")
                .bankCode("021000021")
                .bankName("Chase Bank")
                .nickname("John")
                .currency("USD")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // =========================================================================
    // POST /api/v1/customers/{customerExternalId}/payees
    // =========================================================================

    @Test
    @DisplayName("POST /payees — success → HTTP 201 with PayeeResponse JSON")
    void addPayee_success_returns201() throws Exception {
        // Arrange
        when(payeeUseCase.addPayee(eq(CUSTOMER_EXT_ID), any(PayeeUseCase.AddPayeeCommand.class)))
                .thenReturn(samplePayee);

        Map<String, String> requestBody = Map.of(
                "payeeName", "John Doe",
                "accountNumber", "123456789",
                "bankCode", "021000021",
                "bankName", "Chase Bank",
                "nickname", "John",
                "currency", "USD"
        );

        // Act & Assert
        mockMvc.perform(post(BASE_URL, CUSTOMER_EXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId", is(PAYEE_EXT_ID)))
                .andExpect(jsonPath("$.payeeName", is("John Doe")))
                .andExpect(jsonPath("$.accountNumber", is("123456789")))
                .andExpect(jsonPath("$.bankCode", is("021000021")))
                .andExpect(jsonPath("$.bankName", is("Chase Bank")))
                .andExpect(jsonPath("$.nickname", is("John")))
                .andExpect(jsonPath("$.currency", is("USD")));

        verify(payeeUseCase, times(1))
                .addPayee(eq(CUSTOMER_EXT_ID), any(PayeeUseCase.AddPayeeCommand.class));
    }

    @Test
    @DisplayName("POST /payees — missing accountNumber → HTTP 400")
    void addPayee_missingAccountNumber_returns400() throws Exception {
        // Arrange — omit required 'accountNumber' field
        Map<String, String> requestBody = Map.of(
                "payeeName", "John Doe",
                "bankCode", "021000021",
                "currency", "USD"
        );

        // Act & Assert
        mockMvc.perform(post(BASE_URL, CUSTOMER_EXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());

        verify(payeeUseCase, never()).addPayee(any(), any());
    }

    @Test
    @DisplayName("POST /payees — duplicate payee → HTTP 422")
    void addPayee_duplicate_returns422() throws Exception {
        // Arrange
        when(payeeUseCase.addPayee(eq(CUSTOMER_EXT_ID), any(PayeeUseCase.AddPayeeCommand.class)))
                .thenThrow(new BusinessRuleException(
                        "Payee with account number 123456789 already exists for this customer"));

        Map<String, String> requestBody = Map.of(
                "payeeName", "John Doe",
                "accountNumber", "123456789",
                "bankCode", "021000021",
                "currency", "USD"
        );

        // Act & Assert
        mockMvc.perform(post(BASE_URL, CUSTOMER_EXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnprocessableEntity());

        verify(payeeUseCase, times(1))
                .addPayee(eq(CUSTOMER_EXT_ID), any(PayeeUseCase.AddPayeeCommand.class));
    }

    // =========================================================================
    // GET /api/v1/customers/{customerExternalId}/payees
    // =========================================================================

    @Test
    @DisplayName("GET /payees — success → HTTP 200 with JSON array")
    void listPayees_success_returns200() throws Exception {
        // Arrange
        Payee secondPayee = samplePayee.toBuilder()
                .externalId("payee-ext-002")
                .payeeName("Jane Smith")
                .accountNumber("987654321")
                .build();

        when(payeeUseCase.listPayees(CUSTOMER_EXT_ID))
                .thenReturn(List.of(samplePayee, secondPayee));

        // Act & Assert
        mockMvc.perform(get(BASE_URL, CUSTOMER_EXT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].externalId", is(PAYEE_EXT_ID)))
                .andExpect(jsonPath("$[0].payeeName", is("John Doe")))
                .andExpect(jsonPath("$[1].externalId", is("payee-ext-002")))
                .andExpect(jsonPath("$[1].payeeName", is("Jane Smith")));

        verify(payeeUseCase, times(1)).listPayees(CUSTOMER_EXT_ID);
    }

    @Test
    @DisplayName("GET /payees — customer not found → HTTP 404")
    void listPayees_customerNotFound_returns404() throws Exception {
        // Arrange
        when(payeeUseCase.listPayees(CUSTOMER_EXT_ID))
                .thenThrow(new ResourceNotFoundException(
                        "Customer not found: " + CUSTOMER_EXT_ID));

        // Act & Assert
        mockMvc.perform(get(BASE_URL, CUSTOMER_EXT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(payeeUseCase, times(1)).listPayees(CUSTOMER_EXT_ID);
    }

    // =========================================================================
    // PUT /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}
    // =========================================================================

    @Test
    @DisplayName("PUT /payees/{payeeExternalId} — success → HTTP 200 with updated PayeeResponse")
    void updatePayee_success_returns200() throws Exception {
        // Arrange
        Payee updatedPayee = samplePayee.toBuilder()
                .payeeName("John Updated")
                .nickname("JohnU")
                .updatedAt(now.plusHours(1))
                .build();

        when(payeeUseCase.updatePayee(
                eq(CUSTOMER_EXT_ID),
                eq(PAYEE_EXT_ID),
                any(PayeeUseCase.UpdatePayeeCommand.class)))
                .thenReturn(updatedPayee);

        Map<String, String> requestBody = Map.of(
                "payeeName", "John Updated",
                "accountNumber", "123456789",
                "bankCode", "021000021",
                "currency", "USD",
                "nickname", "JohnU"
        );

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{payeeExternalId}", CUSTOMER_EXT_ID, PAYEE_EXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalId", is(PAYEE_EXT_ID)))
                .andExpect(jsonPath("$.payeeName", is("John Updated")))
                .andExpect(jsonPath("$.nickname", is("JohnU")));

        verify(payeeUseCase, times(1))
                .updatePayee(eq(CUSTOMER_EXT_ID), eq(PAYEE_EXT_ID),
                        any(PayeeUseCase.UpdatePayeeCommand.class));
    }

    @Test
    @DisplayName("PUT /payees/{payeeExternalId} — payee not found → HTTP 404")
    void updatePayee_notFound_returns404() throws Exception {
        // Arrange
        when(payeeUseCase.updatePayee(
                eq(CUSTOMER_EXT_ID),
                eq(PAYEE_EXT_ID),
                any(PayeeUseCase.UpdatePayeeCommand.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Payee not found: " + PAYEE_EXT_ID));

        Map<String, String> requestBody = Map.of(
                "payeeName", "John Updated",
                "accountNumber", "123456789",
                "bankCode", "021000021",
                "currency", "USD"
        );

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/{payeeExternalId}", CUSTOMER_EXT_ID, PAYEE_EXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(payeeUseCase, times(1))
                .updatePayee(eq(CUSTOMER_EXT_ID), eq(PAYEE_EXT_ID),
                        any(PayeeUseCase.UpdatePayeeCommand.class));
    }

    // =========================================================================
    // DELETE /api/v1/customers/{customerExternalId}/payees/{payeeExternalId}
    // =========================================================================

    @Test
    @DisplayName("DELETE /payees/{payeeExternalId} — success → HTTP 204 with no body")
    void removePayee_success_returns204() throws Exception {
        // Arrange
        doNothing().when(payeeUseCase).removePayee(CUSTOMER_EXT_ID, PAYEE_EXT_ID);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{payeeExternalId}", CUSTOMER_EXT_ID, PAYEE_EXT_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(is(emptyOrNullString())));

        verify(payeeUseCase, times(1)).removePayee(CUSTOMER_EXT_ID, PAYEE_EXT_ID);
    }

    @Test
    @DisplayName("DELETE /payees/{payeeExternalId} — payee not found → HTTP 404")
    void removePayee_notFound_returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Payee not found: " + PAYEE_EXT_ID))
                .when(payeeUseCase).removePayee(CUSTOMER_EXT_ID, PAYEE_EXT_ID);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/{payeeExternalId}", CUSTOMER_EXT_ID, PAYEE_EXT_ID))
                .andExpect(status().isNotFound());

        verify(payeeUseCase, times(1)).removePayee(CUSTOMER_EXT_ID, PAYEE_EXT_ID);
    }
}
```