package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.users.infrastructure.entity.UserEntity;
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
            .phone(user.getPhone())
                .role(user.getRole().getCode())
                .address(user.getAddress())
                .active(user.isActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
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
        return User.fromPersistence(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getPhone(),
            entity.getAddress(),
            Role.valueOf(entity.getRole().toUpperCase()),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
