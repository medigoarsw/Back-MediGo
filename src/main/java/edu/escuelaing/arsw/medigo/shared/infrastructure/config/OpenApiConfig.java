package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger para MediGo API
 * 
 * Esta clase define la información general de la API que se mostrará en Swagger UI
 * y permite documentar esquemas de seguridad como JWT.
 * 
 * Acceso a Swagger UI:
 * http://localhost:8080/swagger-ui.html
 * 
 * Acceso a OpenAPI JSON:
 * http://localhost:8080/v3/api-docs
 * http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("MediGo API")
                                .version("1.0.0")
                                .description(
                                        "Documentación de la API REST de MediGo - " +
                                        "Plataforma de gestión de medicamentos, pedidos, logística y subastas.\n\n" +
                                        "Esta API proporciona endpoints para:\n" +
                                        "- Autenticación y gestión de usuarios\n" +
                                        "- Catálogo de medicamentos\n" +
                                        "- Órdenes y pedidos\n" +
                                        "- Logistics y envíos\n" +
                                        "- Sistema de subastas\n\n" +
                                        "**Nota:** En MVP, los tokens de autenticación son 'fake tokens' para testing."
                                )
                                .contact(
                                        new Contact()
                                                .name("MediGo Team")
                                                .email("support@medigo.com")
                                                .url("https://medigo.com")
                                )
                                .license(
                                        new License()
                                                .name("Apache License 2.0")
                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                                )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "JWT token obtenido después de autenticarse.\n\n" +
                                                        "Ejemplo de uso:\n" +
                                                        "Authorization: Bearer fake-jwt.1.student.1774978129358"
                                                )
                                )
                );
    }
}
