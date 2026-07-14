package com.elitecart.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger documentation configuration.
 * Adds JWT bearer auth support so protected endpoints can be tested directly from Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EliteCart API",
                version = "1.0.0",
                description = "REST API documentation for EliteCart - Professional E-Commerce Platform",
                contact = @Contact(name = "EliteCart Team", email = "support@elitecart.com"),
                license = @License(name = "MIT License")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT access token obtained from /auth/login"
)
public class OpenApiConfig {
}
