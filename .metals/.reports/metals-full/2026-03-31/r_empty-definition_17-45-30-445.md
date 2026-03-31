error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java:org/springframework/context/annotation/Profile#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
empty definition using pc, found symbol in pc: org/springframework/context/annotation/Profile#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 578
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.infrastructure.config;

import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.InMemoryUserRepository;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.JpaUserRepositoryAdapter;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.@@Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * CONFIGURACIÓN: Inyección de dependencias del módulo Users
 * 
 * ESTRATEGIA DE REPOSITORIOS POR PERFIL:
 * - test: InMemoryUserRepository con datos mock (para tests unitarios)
 * - local/default: JpaUserRepositoryAdapter con PostgreSQL (para desarrollo/producción)
 */
@Configuration
public class AuthConfig {
    
    /**
     * PERFIL: test (default)
     * Uso: Tests unitarios sin dependencia de BD
     * - InMemoryUserRepository carga datos de TestDataConfig
     * - Tests están aislados y son rápidos
     * - Si cambias BD, los tests siguen funcionando
     * 
     * Se usa también como FALLBACK cuando no hay perfil especificado
     */
    @Bean
    @Profile("!local & !default")
    public UserRepositoryPort testUserRepository() {
        return new InMemoryUserRepository();
    }
    
    /**
     * PERFIL: local, default (producción)
     * Uso: Desarrollo y producción con BD real
     * - JpaUserRepositoryAdapter accede a PostgreSQL
     * - Lee usuarios reales de la BD
     * - Requiere que existan usuarios en PostgreSQL
     */
    @Bean
    @Profile({"local", "default"})
    public UserRepositoryPort productionUserRepository(UserJpaRepository userJpaRepository) {
        return new JpaUserRepositoryAdapter(userJpaRepository);
    }
    
    /**
     * Aquí irían otras configuraciones como:
     * - JwtService (cuando implementes JWT real)
     * - AuthenticationManager (para Spring Security)
     */
    
    /**
     * PasswordEncoder Bean para encriptar contraseñas
     * Usa BCrypt con strength por defecto (10 rounds)
     * Se inyecta automáticamente en AuthService
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/context/annotation/Profile#