package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * Clase base para excepciones del dominio
 * 
 * Todas las excepciones que representen violaciones de reglas de negocio
 * deben extender esta clase.
 */
public abstract class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
