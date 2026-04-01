package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * EXCEPCIÓN DE DOMINIO: Email ya registrado
 */
public class EmailAlreadyRegisteredException extends DomainException {
    
    public EmailAlreadyRegisteredException(String email) {
        super("El email " + email + " ya se encuentra registrado");
    }
    
    public static EmailAlreadyRegisteredException withEmail(String email) {
        return new EmailAlreadyRegisteredException(email);
    }
}
