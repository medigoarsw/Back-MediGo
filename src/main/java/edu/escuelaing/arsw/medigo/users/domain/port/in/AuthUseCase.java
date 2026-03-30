package edu.escuelaing.arsw.medigo.users.domain.port.in;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
public interface AuthUseCase {
    String login(String email, String password);
    User getByToken(String token);
}