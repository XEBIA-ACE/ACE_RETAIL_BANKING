package com.banking.adapter.in.web;

import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.InsufficientFundsException;
import com.banking.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that {@link GlobalExceptionHandler} correctly maps domain exceptions
 * to the expected HTTP status codes and structured JSON response bodies.
 *
 * <p>Acceptance criteria covered:
 * <ul>
 *   <li>AC: ResourceNotFoundException → 404 with JSON body (no stack trace, no internal IDs)</li>
 *   <li>AC: InsufficientFundsException → 422 (existing handling not broken)</li>
 *   <li>AC: BusinessRuleException → 400 (existing handling not broken)</li>
 *   <li>AC: Unhandled Exception → 500 (existing handling not broken)</li>
 * </ul>
 */
@WebMvcTest(controllers = {
        GlobalExceptionHandlerTest.StubController.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // -----------------------------------------------------------------------
    // 404 — ResourceNotFoundException (primary acceptance criterion for US-002)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ResourceNotFoundException results in HTTP 404 with structured JSON body")
    void resourceNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/resource-not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with identifier: unknown-id"));
    }

    @Test
    @DisplayName("404 response body does not contain a stack trace or internal IDs")
    void resourceNotFound_bodyContainsNoStackTrace() throws Exception {
        mockMvc.perform(get("/test/resource-not-found")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                // Stack trace would contain "at com." — assert it is absent
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // Existing handlers must not be broken
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("InsufficientFundsException still results in HTTP 422")
    void insufficientFunds_returns422() throws Exception {
        mockMvc.perform(get("/test/insufficient-funds")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("BusinessRuleException still results in HTTP 400")
    void businessRule_returns400() throws Exception {
        mockMvc.perform(get("/test/business-rule")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Unhandled exception still results in HTTP 500 with generic message")
    void unhandledException_returns500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    // -----------------------------------------------------------------------
    // Minimal stub controller used only within this test class
    // -----------------------------------------------------------------------

    /**
     * Stub controller that deliberately throws domain exceptions so the
     * {@link GlobalExceptionHandler} can be exercised in isolation.
     */
    @RestController
    @RequestMapping("/test")
    static class StubController {

        @GetMapping("/resource-not-found")
        public void throwResourceNotFound() {
            throw new ResourceNotFoundException("Customer", "unknown-id");
        }

        @GetMapping("/insufficient-funds")
        public void throwInsufficientFunds() {
            throw new InsufficientFundsException("Insufficient funds for account ACC-001");
        }

        @GetMapping("/business-rule")
        public void throwBusinessRule() {
            throw new BusinessRuleException("Loan is already in APPROVED state");
        }

        @GetMapping("/generic-error")
        public void throwGeneric() {
            throw new RuntimeException("Something went wrong internally");
        }
    }
}
