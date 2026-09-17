package com.bank.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bankingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Core Service")
                        .version("1.0")
                        .description("Account balance REST API. WebSocket clients connect to /ws and "
                                + "subscribe to /topic/accounts/{accountId}/balance. Payloads contain "
                                + "accountId, balance, currency, and ISO-8601 balanceTimestamp."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")));
    }
}
