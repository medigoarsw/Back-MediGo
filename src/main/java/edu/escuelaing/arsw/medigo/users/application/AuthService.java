package edu.escuelaing.arsw.medigo.users.application;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {
    private final UserRepositoryPort userRepository;
    @Override
    public String login(String email, String password) {
        throw new UnsupportedOperationException("TODO Anderson: JWT");
    }
    @Override
    public User getByToken(String token) {
        throw new UnsupportedOperationException("TODO Anderson: validate JWT");
    }
}