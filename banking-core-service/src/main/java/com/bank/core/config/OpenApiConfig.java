```java
package com.bank.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Springdoc OpenAPI configuration.
 *
 * <p>Registers API groups and ensures the Bearer JWT security scheme is applied
 * globally. The "accounts" group surfaces the consolidated dashboard endpoint
 * in the generated Swagger UI / OpenAPI spec at {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Global OpenAPI metadata and security scheme definition.
     *
     * <p>The Bearer JWT security scheme is registered here once and referenced by
     * individual endpoint operations (or applied globally via the top-level
     * {@link SecurityRequirement}).
     */
    @Bean
    public OpenAPI bankingCoreOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Provide a valid JWT access token obtained from the authentication service. "
                        + "Include it in the Authorization header as: Bearer <token>");

        SecurityRequirement globalSecurityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(new Info()
                        .title("Banking Core Service API")
                        .version("v1")
                        .description("REST API for the ACE Retail Banking Core Service. "
                                + "All endpoints require a valid Bearer JWT unless stated otherwise.")
                        .contact(new Contact()
                                .name("ACE Banking Platform Team")
                                .email("platform@bank.com")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme))
                .addSecurityItem(globalSecurityRequirement);
    }

    /**
     * OpenAPI group for the Accounts domain.
     *
     * <p>This group matches all paths under {@code /api/v1/accounts/**} and ensures
     * the {@code GET /api/v1/accounts/dashboard} endpoint appears in the generated
     * spec with the correct summary, description, and security requirement.
     *
     * <p>The {@code customiseAccountsDashboardOperation} customiser bean below
     * enriches the operation metadata for the dashboard endpoint specifically.
     */
    @Bean
    public GroupedOpenApi accountsApiGroup() {
        return GroupedOpenApi.builder()
                .group("accounts")
                .displayName("Accounts API")
                .pathsToMatch("/api/v1/accounts/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    if ("getDashboard".equals(handlerMethod.getMethod().getName())
                            || (handlerMethod.getMethodAnnotation(
                                    io.swagger.v3.oas.annotations.Operation.class) == null
                                && "/api/v1/accounts/dashboard".equals(
                                    extractPathFromHandler(handlerMethod)))) {
                        enrichDashboardOperation(operation);
                    }
                    ensureBearerSecurity(operation);
                    return operation;
                })
                .build();
    }

    /**
     * OpenAPI group for the Customers domain.
     *
     * <p>Preserved from the original configuration; matches all paths under
     * {@code /api/v1/customers/**}.
     */
    @Bean
    public GroupedOpenApi customersApiGroup() {
        return GroupedOpenApi.builder()
                .group("customers")
                .displayName("Customers API")
                .pathsToMatch("/api/v1/customers/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    ensureBearerSecurity(operation);
                    return operation;
                })
                .build();
    }

    /**
     * Catch-all group that surfaces every public endpoint in a single view.
     *
     * <p>Useful for browsing the full API surface without switching groups.
     */
    @Bean
    public GroupedOpenApi allApisGroup() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    ensureBearerSecurity(operation);
                    return operation;
                })
                .build();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Enriches the dashboard operation with the required summary, description,
     * tags, and response documentation when the controller method has not already
     * provided them via {@code @Operation} annotations.
     */
    private void enrichDashboardOperation(Operation operation) {
        if (operation.getSummary() == null || operation.getSummary().isBlank()) {
            operation.setSummary("Get consolidated account dashboard");
        }

        if (operation.getDescription() == null || operation.getDescription().isBlank()) {
            operation.setDescription(
                    "Returns a consolidated view of all accounts linked to the authenticated customer. "
                            + "**Authentication required**: a valid Bearer JWT must be supplied in the "
                            + "`Authorization` header (e.g. `Authorization: Bearer <token>`). "
                            + "Requests without a valid token will receive HTTP 401 Unauthorized. "
                            + "Each account entry includes `accountId`, `accountTypeName`, "
                            + "`maskedAccountNumber`, `currentBalance`, `currencyCode`, and an "
                            + "`ariaLabel` suitable for screen-reader announcements.");
        }

        if (operation.getTags() == null || operation.getTags().isEmpty()) {
            operation.addTagsItem("Accounts");
        }

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        responses.putIfAbsent("200", new ApiResponse()
                .description("Consolidated dashboard returned successfully. "
                        + "When the customer has no linked accounts, `hasAccounts` is `false` "
                        + "and `accounts` is an empty array."));
        responses.putIfAbsent("401", new ApiResponse()
                .description("Unauthorized — missing or invalid Bearer JWT."));
        responses.putIfAbsent("403", new ApiResponse()
                .description("Forbidden — authenticated but not authorised for this resource."));
        responses.putIfAbsent("500", new ApiResponse()
                .description("Internal Server Error — unexpected server-side failure."));
    }

    /**
     * Guarantees the Bearer security requirement is listed on the operation.
     * Idempotent — will not add a duplicate entry if it is already present.
     */
    private void ensureBearerSecurity(Operation operation) {
        if (operation.getSecurity() == null) {
            operation.setSecurity(List.of(new SecurityRequirement().addList(SECURITY_SCHEME_NAME)));
            return;
        }

        boolean alreadyPresent = operation.getSecurity().stream()
                .anyMatch(req -> req.containsKey(SECURITY_SCHEME_NAME));

        if (!alreadyPresent) {
            operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        }
    }

    /**
     * Best-effort extraction of the request-mapping path from a handler method.
     * Returns an empty string when the path cannot be determined.
     */
    private String extractPathFromHandler(
            org.springdoc.core.customizers.OperationCustomizer handlerMethod) {
        // Path extraction is best-effort; the primary matching is done via
        // method name ("getDashboard") in the operation customiser above.
        return "";
    }

    /**
     * Overloaded helper used by the GroupedOpenApi customiser lambdas where the
     * actual HandlerMethod is provided by Springdoc.
     */
    @SuppressWarnings("unused")
    private String extractPathFromHandler(
            org.springframework.web.method.HandlerMethod handlerMethod) {
        org.springframework.web.bind.annotation.RequestMapping classMapping =
                handlerMethod.getBeanType()
                        .getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
        org.springframework.web.bind.annotation.GetMapping methodGetMapping =
                handlerMethod.getMethodAnnotation(
                        org.springframework.web.bind.annotation.GetMapping.class);
        org.springframework.web.bind.annotation.RequestMapping methodRequestMapping =
                handlerMethod.getMethodAnnotation(
                        org.springframework.web.bind.annotation.RequestMapping.class);

        String classPath = (classMapping != null && classMapping.value().length > 0)
                ? classMapping.value()[0] : "";
        String methodPath = "";

        if (methodGetMapping != null && methodGetMapping.value().length > 0) {
            methodPath = methodGetMapping.value()[0];
        } else if (methodRequestMapping != null && methodRequestMapping.value().length > 0) {
            methodPath = methodRequestMapping.value()[0];
        }

        return classPath + methodPath;
    }
}
```