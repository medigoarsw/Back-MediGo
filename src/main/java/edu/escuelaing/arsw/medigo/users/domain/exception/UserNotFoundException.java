package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * Excepción de dominio lanzada cuando no se encuentra un usuario
 * 
 * Esta excepción pertenece al dominio porque representa una falla
 * en una regla de negocio, no una falla técnica.
 */
public class UserNotFoundException extends DomainException {
    
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("Usuario no encontrado con email: " + email);
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("Usuario no encontrado con username: " + username);
    }

    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("Usuario no encontrado con id: " + id);
    }
}
