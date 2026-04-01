package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ADAPTADOR DE SALIDA: Spring Data JPA para UserEntity
 * 
 * Define las consultas al repositorio de BD
 * Se inyecta en JpaUserRepository (que implementa el puerto del dominio)
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * Busca usuario por email
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Busca usuario por nombre (username)
     */
    Optional<UserEntity> findByName(String name);
}
