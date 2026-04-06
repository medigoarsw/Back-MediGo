error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java:org/springframework/security/crypto/password/PasswordEncoder#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
empty definition using pc, found symbol in pc: org/springframework/security/crypto/password/PasswordEncoder#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 714
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.infrastructure.config;

import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.InMemoryUserRepository;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.JpaUserRepositoryAdapter;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.@@PasswordEncoder;

@Configuration
public class AuthConfig {
    
    /**
     * PasswordEncoder Bean para encriptar contraseñas
     * Usa BCrypt con strength por defecto (10 rounds)
     * Se inyecta automáticamente en AuthService
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * ESTRATEGIA DE REPOSITORIOS POR PERFIL:
     * - ci, test: InMemoryUserRepository (tests)
     * - otros: JpaUserRepositoryAdapter (producción)
     */
    
    /**
     * PERFIL: ci, test - Tests unitarios
     * Uso: Tests unitarios sin dependencia de BD
     * Se crea SOLO cuando el perfil es ci o test
     */
    @Bean
    @Profile({"ci", "test"})
    public UserRepositoryPort testUserRepository() {
        return new InMemoryUserRepository();
    }
    
    /**
     * PERFIL: default/local - Producción
     * Uso: Desarrollo y producción con BD real
     * Se crea SOLO en perfiles locales/default (NO en test/ci)
     */
    @Bean
    @Profile({"local", "default"})
    public UserRepositoryPort productionUserRepository(UserJpaRepository userJpaRepository) {
        return new JpaUserRepositoryAdapter(userJpaRepository);
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/security/crypto/password/PasswordEncoder#