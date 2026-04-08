package edu.escuelaing.arsw.medigo.users.domain.exception;

/**
 * EXCEPCIÓN DE DOMINIO: Entrada inválida (validación)
 */
public class InvalidInputException extends DomainException {
    
    public InvalidInputException(String message) {
        super(message);
    }
    
    public static InvalidInputException invalidEmail(String email) {
        return new InvalidInputException("El email " + email + " no es válido");
    }
    
    public static InvalidInputException emptyField(String fieldName) {
        return new InvalidInputException("El campo " + fieldName + " no puede estar vacío");
    }
    
    public static InvalidInputException weakPassword() {
        return new InvalidInputException("La contraseña debe tener al menos 8 caracteres, incluir mayúscula, minúscula y número");
    }

    public static InvalidInputException compromisedPassword() {
        return new InvalidInputException("La contraseña elegida aparece en brechas de seguridad conocidas. Elige una diferente.");
    }

    public static InvalidInputException invalidPhone(String phone) {
        return new InvalidInputException("El teléfono '" + phone + "' no es válido. Use el formato +57-322-5555555");
    }
    
    public static InvalidInputException invalidRole(String role) {
        return new InvalidInputException("El rol '" + role + "' no es válido. Solo se permiten: AFFILIATE (Usuario/Cliente), DELIVERY (Repartidor). ADMIN se crea por administradores.");
    }
}
