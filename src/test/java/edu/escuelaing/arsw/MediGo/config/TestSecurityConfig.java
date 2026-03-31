package edu.escuelaing.arsw.medigo.config;

import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.InMemoryUserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * TEST SECURITY CONFIGURATION
 * 
 * Proporciona los Beans de seguridad necesarios para los tests
 * Como AuthService requiere PasswordEncoder, este Bean asegura que esté disponible
 */
@TestConfiguration
public class TestSecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Proporciona el repositorio de usuarios para tests
     */
    @Bean
    @Primary
    public UserRepositoryPort userRepository() {
        return new InMemoryUserRepository();
    }
}
