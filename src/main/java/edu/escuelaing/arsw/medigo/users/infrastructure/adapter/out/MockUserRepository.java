package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class MockUserRepository implements UserRepositoryPort {
    private static final String HASH = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
    private static final Map<String, User> USERS = Map.of(
        "admin@medigo.com", User.builder().id(1L).email("admin@medigo.com").passwordHash(HASH).name("Admin MediGo").role(User.UserRole.ADMIN).active(true).build(),
        "afiliado@medigo.com", User.builder().id(2L).email("afiliado@medigo.com").passwordHash(HASH).name("Juan Afiliado").role(User.UserRole.AFFILIATE).active(true).build(),
        "repartidor@medigo.com", User.builder().id(3L).email("repartidor@medigo.com").passwordHash(HASH).name("Carlos Repartidor").role(User.UserRole.DELIVERY).active(true).build()
    );
    @Override public Optional<User> findByEmail(String email) { return Optional.ofNullable(USERS.get(email)); }
    @Override public Optional<User> findById(Long id) { return USERS.values().stream().filter(u -> u.getId().equals(id)).findFirst(); }
}