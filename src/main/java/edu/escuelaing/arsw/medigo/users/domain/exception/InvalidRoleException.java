package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * EXCEPCIÓN DE DOMINIO: Rol inválido
 */
public class InvalidRoleException extends DomainException {
    
    public InvalidRoleException(String role) {
        super("El rol " + role + " no es válido. Solo se permiten: USER (Usuario), DELIVERY (Repartidor)");
    }
    
    public static InvalidRoleException withRole(String role) {
        return new InvalidRoleException(role);
    }
}
