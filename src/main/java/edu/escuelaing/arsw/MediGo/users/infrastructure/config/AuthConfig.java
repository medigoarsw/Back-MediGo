package edu.escuelaing.arsw.medigo.users.infrastructure.config;

import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.InMemoryUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURACIÓN: Inyección de dependencias del módulo Users
 * 
 * Este es el punto central donde configuramos qué implementación usar
 * para cada interfaz de puerto.
 * 
 * 🔑 VENTAJA PRINCIPAL:
 * Cuando migres a JPA, SOLO cambias esta clase:
 * 
 * // De:
 * @Bean
 * public UserRepositoryPort userRepository() {
 *     return new InMemoryUserRepository();
 * }
 * 
 * // A:
 * @Bean
 * public UserRepositoryPort userRepository() {
 *     return new JpaUserRepository(userJpaRepository);
 * }
 * 
 * ¡Y TODO LO DEMÁS FUNCIONA SIN CAMBIOS!
 */
@Configuration
public class AuthConfig {
    
    /**
     * DEF INICIÓN DEL PUERTO: UserRepositoryPort
     * 
     * En MVP: Usa InMemoryUserRepository
     * En futuro: Cambia a JpaUserRepository
     * 
     * El nombre del bean no importa, solo que Spring sepa
     * cómo inyectarlo cuando vea @Autowired UserRepositoryPort
     */
    @Bean
    public UserRepositoryPort userRepository() {
        return new InMemoryUserRepository();
    }
    
    /**
     * Aquí irían otras configuraciones como:
     * - JwtService (cuando implementes JWT real)
     * - PasswordEncoder (cuando implementes hash real)
     * - AuthenticationManager (para Spring Security)
     */
}
