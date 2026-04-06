error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/InMemoryUserRepository.java:edu/escuelaing/arsw/medigo/users/infrastructure/config/TestDataConfig#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/InMemoryUserRepository.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/config/TestDataConfig#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 337
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/InMemoryUserRepository.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.users.infrastructure.config.@@TestDataConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ADAPTADOR DE SALIDA: Implementación en memoria del repositorio
 * 
 * Este es un ADAPTADOR (patrón hexagonal):
 * - Implementa UserRepositoryPort (interfaz del dominio)
 * - Almacena datos en memoria (HashMap)
 * - NO contiene lógica de negocio
 * 
 * CUANDO MIGRAR A BD:
 * 1. Crear JpaUserRepository extends UserRepositoryPort
 * 2. Usar Spring Data JPA @Repository
 * 3. Cambiar solo el @Bean en AuthConfig
 * 4. El dominio + AuthService no cambian
 * 
 * EJEMPLO DE FUTURO:
 *
 * @Repository
 * public class JpaUserRepository implements UserRepositoryPort {
 *     @Autowired private UserJpaRepository jpaRepo;
 *     
 *     @Override
 *     public Optional<User> findByUsername(String username) {
 *         return jpaRepo.findByUsername(username)
 *                 .map(entity -> entity.toDomain()); // EntityMapper
 *     }
 * }
 */
@Slf4j
public class InMemoryUserRepository implements UserRepositoryPort {
    
    private final Map<Long, User> usersById = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();
    
    // Counter para IDs
    private long nextId = 1L;

    /**
     * Constructor: Inicializa con usuarios de prueba
     * En MVP es manual, en producción vendría de BD
     */
    public InMemoryUserRepository() {
        initializeMockUsers();
    }

    /**
     * Carga usuarios mock en memoria desde TestDataConfig
     * 
     * Las credenciales reales están centralizadas en TestDataConfig
     * para mejor seguridad y auditabilidad de datos de test.
     */
    private void initializeMockUsers() {
        TestDataConfig.TEST_USERS.forEach(testData -> {
            User user = testData.toDomainUser();
            addUserToMaps(user);
        });
        log.info("InMemoryUserRepository inicializado con {} usuarios de prueba", 
                 TestDataConfig.TEST_USERS.size());
    }

    /**
     * Helper para añadir usuario a todos los mapas
     */
    private void addUserToMaps(User user) {
        usersById.put(user.getId(), user);
        usersByUsername.put(user.getUsername(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    /**
     * IMPLEMENTACIÓN: findByUsername
     */
    @Override
    public Optional<User> findByUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * IMPLEMENTACIÓN: findByEmail
     */
    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return Optional.ofNullable(usersByEmail.get(email));
    }

    /**
     * IMPLEMENTACIÓN: findById
     */
    @Override
    public Optional<User> findById(Long id) {
        log.debug("Buscando usuario por id: {}", id);
        return Optional.ofNullable(usersById.get(id));
    }

    /**
     * IMPLEMENTACIÓN: save
     */
    @Override
    public User save(User user) {
        // Si no tiene ID, asignar uno
        if (user.getId() == null) {
            // Crear nuevo User con ID asignado
            User userWithId = User.create(nextId++, user.getUsername(), user.getEmail(), user.getPassword(), user.getRole());
            addUserToMaps(userWithId);
            log.info("Usuario guardado en memoria: {}", userWithId.getEmail());
            return userWithId;
        } else {
            // Actualizar usuario existente
            addUserToMaps(user);
            log.info("Usuario actualizado en memoria: {}", user.getEmail());
            return user;
        }
    }

    /**
     * Helper para crear nuevos usuarios (útil para test)
     */
    public User createUser(String username, String email, String password, Role role) {
        User user = User.create(nextId++, username, email, password, role);
        addUserToMaps(user);
        log.info("Usuario creado en memoria: {}", username);
        return user;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/config/TestDataConfig#