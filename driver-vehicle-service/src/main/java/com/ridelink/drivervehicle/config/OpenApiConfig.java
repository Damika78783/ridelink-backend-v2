package com.ridelink.drivervehicle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("RideLink – Driver & Vehicle Service API")
                        .version("1.0.0")
                        .description("IT3130 Application Development - Microservice 2: Driver & Vehicle Service\n\n"
                                + "Responsible for managing Driver operational profiles, registered vehicles, "
                                + "real-time availability states, simulated GPS locations, and candidate driver dispatching.\n\n"
                                + "Primary Inter-Service Consumer: Ride Management Service (Member 3)")
                        .contact(new Contact()
                                .name("RideLink Team - Member 2 (Driver & Vehicle Service)")
                                .email("driver-service@ridelink.local"))
                        .license(new License().name("Academic Project - IT3130")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token issued by Account Service. Format: Bearer <token>")));
    }
}
