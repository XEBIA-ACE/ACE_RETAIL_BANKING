```java
package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.ProfileResponse;
import com.banking.application.port.in.ProfileUseCase;
import com.banking.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest slice tests for {@link ProfileController}.
 * Covers HTTP 200 (success), HTTP 404 (customer not found), and HTTP 401 (unauthenticated).
 */
@WebMvcTest(ProfileController.class)
@Import(GlobalExceptionHandler.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileUseCase profileUseCase;

    @Test
    @DisplayName("GET /profile returns HTTP 200 with full ProfileResponse when authenticated")
    @WithMockUser(username = "test-external-id")
    void getProfile_returns200_withProfileResponse_whenAuthenticated() throws Exception {
        ProfileResponse profileResponse = ProfileResponse.builder()
                .externalId("test-external-id")
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .registrationDate(LocalDateTime.of(2023, 4, 15, 10, 30, 0))
                .accountStatus("ACTIVE")
                .build();

        when(profileUseCase.getProfile(anyString())).thenReturn(profileResponse);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.externalId").value("test-external-id"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.registrationDate").value("2023-04-15T10:30:00"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /profile returns HTTP 404 with structured error body when customer not found")
    @WithMockUser(username = "test-id")
    void getProfile_returns404_whenCustomerNotFound() throws Exception {
        when(profileUseCase.getProfile(anyString()))
                .thenThrow(new ResourceNotFoundException("Customer not found: test-id"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(not(emptyString())));
    }

    @Test
    @DisplayName("GET /profile returns HTTP 401 when request is unauthenticated")
    void getProfile_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }
}
```