package edu.escuelaing.arsw.medigo.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * TEST SECURITY CONFIGURATION  
 * 
 * Contenedor para configuraciones específicas de tests
 * Los Beans (PasswordEncoder, UserRepositoryPort) se proporcionan desde AuthConfig
 */
@TestConfiguration
public class TestSecurityConfig {
    // Todos los Beans se importan desde AuthConfig
}
