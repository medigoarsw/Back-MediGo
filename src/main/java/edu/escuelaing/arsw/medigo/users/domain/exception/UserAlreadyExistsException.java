package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * EXCEPCIÓN DE DOMINIO: Usuario ya existe (username o email)
 */
public class UserAlreadyExistsException extends DomainException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public static UserAlreadyExistsException usernameExists(String username) {
        return new UserAlreadyExistsException("El usuario " + username + " ya se encuentra registrado");
    }
    
    public static UserAlreadyExistsException emailExists(String email) {
        return new UserAlreadyExistsException("El email " + email + " ya se encuentra registrado");
    }
}
