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
        User student = User.create(2L, "student", "student@medigo.com", "123", Role.STUDENT);
        User vendor = User.create(3L, "vendor", "vendor@medigo.com", "123", Role.VENDOR);
        
        users.put(admin.getEmail(), admin);
        users.put(student.getEmail(), student);
        users.put(vendor.getEmail(), vendor);
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
