package com.hean.consigueventas.oonabe.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI oonaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Oona API")
                        .version("1.0.0")
                        .description("API REST para autenticacion, catalogos, ubicaciones, eventos, reservas y pagos de Oona.")
                        .contact(new Contact().name("Oona Backend Team")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT enviado en el header Authorization como Bearer token.")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/auth/**",
                        "/api/v1/categories/**",
                        "/api/v1/locations/**",
                        "/api/v1/citys/**"
                        )
                .build();
    }

    @Bean
    public GroupedOpenApi protectedApi() {
        return GroupedOpenApi.builder()
                .group("protected")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/v1/auth/**", "/api/v1/categories/**", "/api/v1/locations/**")
                .build();
    }
}
