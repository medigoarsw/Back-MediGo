package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * EXCEPCIÓN DE DOMINIO: Contraseña débil
 */
public class WeakPasswordException extends DomainException {
    
    public WeakPasswordException(String message) {
        super(message);
    }
    
    public static WeakPasswordException withMessage(String message) {
        return new WeakPasswordException(message);
    }
}
