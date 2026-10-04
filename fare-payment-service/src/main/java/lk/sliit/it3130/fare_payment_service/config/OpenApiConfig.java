package lk.sliit.it3130.fare_payment_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Fare & Payment Service API",
                version = "1.0.0",
                description = "RESTful Microservice for Fare Estimation, Ride Payment Processing, and Receipt Generation.",
                contact = @Contact(
                        name = "RideLink Platform Team",
                        email = "support@ridelink.sliit.lk"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        security = {
                @SecurityRequirement(name = "Bearer Authentication")
        },
        servers = {
                @Server(url = "/", description = "Default Server URL")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Enter JWT Bearer token obtained from Account / Auth Service (e.g. 'Bearer <token>')"
)
public class OpenApiConfig {
}
