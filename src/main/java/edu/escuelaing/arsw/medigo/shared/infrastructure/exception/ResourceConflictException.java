package edu.escuelaing.arsw.medigo.shared.infrastructure.exception;

public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
