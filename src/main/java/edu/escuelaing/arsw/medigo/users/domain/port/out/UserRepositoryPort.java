package edu.escuelaing.arsw.medigo.users.domain.port.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import java.util.Optional;

/**
 * PUERTO DE SALIDA: Repositorio de Usuarios
 * 
 * Define el contrato para acceder a usuarios.
 * Los adaptadores (InMemoryUserRepository, JpaUserRepository) lo implementan.
 */
public interface UserRepositoryPort {
    
    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca un usuario por su ID
     */
    Optional<User> findById(Long id);
}
