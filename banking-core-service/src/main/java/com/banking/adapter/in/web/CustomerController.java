package com.banking.adapter.in.web;

import com.banking.adapter.in.web.dto.CustomerResponse;
import com.banking.adapter.in.web.dto.OnboardCustomerRequest;
import com.banking.application.port.in.CustomerUseCase;
import com.banking.domain.model.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer onboarding and management")
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    @PostMapping
    @Operation(summary = "Onboard a new customer")
    public ResponseEntity<CustomerResponse> onboardCustomer(
            @Valid @RequestBody OnboardCustomerRequest request) {
        Customer customer = customerUseCase.onboardCustomer(
                new CustomerUseCase.OnboardCustomerCommand(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPhone()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(customer));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get customer by external ID")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String externalId) {
        return customerUseCase.findCustomerById(externalId)
                .map(c -> ResponseEntity.ok(toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all customers (paginated)")
    public ResponseEntity<List<CustomerResponse>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<CustomerResponse> customers = customerUseCase.listCustomers(page, size)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @DeleteMapping("/{externalId}")
    @Operation(summary = "Deactivate a customer")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable String externalId) {
        customerUseCase.deactivateCustomer(externalId);
        return ResponseEntity.noContent().build();
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .externalId(customer.getExternalId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .status(customer.getStatus().name())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
