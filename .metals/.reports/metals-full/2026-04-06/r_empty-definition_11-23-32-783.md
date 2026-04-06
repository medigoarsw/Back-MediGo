error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/JpaUserRepositoryAdapter.java:edu/escuelaing/arsw/medigo/users/infrastructure/entity/UserEntity#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/JpaUserRepositoryAdapter.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/entity/UserEntity#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 337
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/JpaUserRepositoryAdapter.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.users.infrastructure.entity.@@UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ADAPTADOR DE SALIDA: Implementación JPA del repositorio de usuarios
 * 
 * Implementa UserRepositoryPort (puerto del dominio)
 * Usa UserJpaRepository (Spring Data JPA)
 * Traduce entre UserEntity (BD) y User (dominio)
 * 
 * VENTAJA: El dominio no conoce JPA, podemos cambiar de BD sin toca el dominio
 */
@Slf4j
public class JpaUserRepositoryAdapter implements UserRepositoryPort {
    
    private final UserJpaRepository userJpaRepository;
    
    public JpaUserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        log.debug("Buscando usuario por username: {}", username);
        return userJpaRepository.findByName(username)
                .map(this::toDomainUser);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Buscando usuario por email: {}", email);
        return userJpaRepository.findByEmail(email)
                .map(this::toDomainUser);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        log.debug("Buscando usuario por id: {}", id);
        return userJpaRepository.findById(id)
                .map(this::toDomainUser);
    }
    
    @Override
    public User save(User user) {
        log.info("Guardando usuario con email: {}", user.getEmail());
        
        // Traducir de User (dominio) a UserEntity (BD)
        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .passwordHash(user.getPassword())
                .role(user.getRole().getCode())
                .active(user.isActive())
                .build();
        
        // Guardar en BD
        UserEntity savedEntity = userJpaRepository.save(entity);
        
        // Traducir de vuelta a dominio
        return toDomainUser(savedEntity);
    }
    
    /**
     * Traduce UserEntity (infraestructura/BD) a User (dominio)
     */
    private User toDomainUser(UserEntity entity) {
        return User.create(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getPasswordHash(),
            Role.valueOf(entity.getRole().toUpperCase())
        );
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/infrastructure/entity/UserEntity#