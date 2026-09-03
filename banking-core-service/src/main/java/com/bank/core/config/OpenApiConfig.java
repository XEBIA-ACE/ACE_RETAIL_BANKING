package com.bank.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI bankingCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Core Service API")
                        .description("REST API for the Banking Core Service — customers, accounts, payees, and dashboard.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
                                .name(BEARER_AUTH_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addTagsItem(new Tag()
                        .name("Customer")
                        .description("Customer onboarding and management endpoints"))
                .addTagsItem(new Tag()
                        .name("Dashboard")
                        .description("Consolidated account dashboard endpoints"));
    }

    @Bean
    public GroupedOpenApi customerApiGroup() {
        return GroupedOpenApi.builder()
                .group("customer")
                .displayName("Customer API")
                .pathsToMatch("/api/v1/customers/**")
                .build();
    }

    @Bean
    public GroupedOpenApi dashboardApiGroup() {
        return GroupedOpenApi.builder()
                .group("dashboard")
                .displayName("Dashboard API")
                .pathsToMatch("/api/v1/dashboard/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApiGroup() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}