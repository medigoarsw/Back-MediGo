error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java:edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/TestDataConfig#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/TestDataConfig#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 484
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.infrastructure.config;

import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.InMemoryUserRepository;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.JpaUserRepositoryAdapter;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.@@TestDataConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
     * PERFIL: test
     * Uso: Tests unitarios sin dependencia de BD
     * - InMemoryUserRepository carga datos de TestDataConfig
     * - Tests están aislados y son rápidos
     * - Si cambias BD, los tests siguen funcionando
     */
    @Bean
    @Profile("test")
    public UserRepositoryPort testUserRepository(TestDataConfig testDataConfig) {
        return new InMemoryUserRepository(testDataConfig.loadTestUsers());
    }
    
    /**
     * PERFIL: local, default (producción)
     * Uso: Desarrollo y producción con BD real
     * - JpaUserRepositoryAdapter accede a PostgreSQL
     * - Lee usuarios reales de la BD
     * - Requiere que existan usuarios en PostgreSQL
     */
    @Bean
    @Profile({"local", "default", "!"})
    public UserRepositoryPort productionUserRepository(UserJpaRepository userJpaRepository) {
        return new JpaUserRepositoryAdapter(userJpaRepository);
    }
    
    /**
     * Aquí irían otras configuraciones como:
     * - JwtService (cuando implementes JWT real)
     * - PasswordEncoder (cuando implementes hash real)
     * - AuthenticationManager (para Spring Security)
     */
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/TestDataConfig#