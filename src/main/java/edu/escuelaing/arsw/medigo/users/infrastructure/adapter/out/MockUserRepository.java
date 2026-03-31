package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * DEPRECATED: Usar InMemoryUserRepository en su lugar
 * 
 * Este archivo se mantiene por compatibilidad pero está siendo reemplazado
 * por InMemoryUserRepository que tiene mejor arquitectura.
 */
@Slf4j
@Component
@Deprecated(since = "1.0", forRemoval = true)
public class MockUserRepository implements UserRepositoryPort {
    
    private final Map<String, User> users = new HashMap<>();
    
    public MockUserRepository() {
        // Usuarios de prueba
        User admin = User.create(1L, "admin", "admin@medigo.com", "123", Role.ADMIN);
        User user = User.create(2L, "user", "user@medigo.com", "123", Role.USER);
        User delivery = User.create(3L, "delivery", "delivery@medigo.com", "123", Role.DELIVERY);
        
        users.put(admin.getEmail(), admin);
        users.put(user.getEmail(), user);
        users.put(delivery.getEmail(), delivery);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(users.get(email));
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return users.values().stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();
    }
}
