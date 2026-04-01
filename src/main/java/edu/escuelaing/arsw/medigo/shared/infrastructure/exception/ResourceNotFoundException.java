package edu.escuelaing.arsw.medigo.shared.infrastructure.exception;

/**
 * Excepción para recursos no encontrados
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceId = null;
    }

    public ResourceNotFoundException(String message, String resourceId) {
        super(message);
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceId = null;
    }

    public String getResourceId() {
        return resourceId;
    }
}
