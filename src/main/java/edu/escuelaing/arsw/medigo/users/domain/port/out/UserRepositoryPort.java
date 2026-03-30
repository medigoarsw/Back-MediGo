package edu.escuelaing.arsw.medigo.users.domain.port.out;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import java.util.Optional;
public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
}