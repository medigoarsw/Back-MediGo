package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * Excepción lanzada cuando las credenciales proporcionadas son inválidas
 */
public class InvalidCredentialsException extends DomainException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public static InvalidCredentialsException withMessage(String username) {
        return new InvalidCredentialsException("Credenciales inválidas para el usuario: " + username);
    }
}
