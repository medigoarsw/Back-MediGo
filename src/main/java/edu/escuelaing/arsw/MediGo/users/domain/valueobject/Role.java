package edu.escuelaing.arsw.medigo.users.domain.valueobject;

/**
 * Value Object que representa los roles disponibles en el sistema.
 * 
 * Esta es una enumeración inmutable, perfecta para un value object.
 * En el futuro, podría migrarse a una tabla de roles en la BD.
 */
public enum Role {
    ADMIN("admin", "Administrator with full access"),
    STUDENT("student", "Student with basic access"),
    VENDOR("vendor", "Vendor managing auctions"),
    LOGISTICS("logistics", "Logistics operator");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Obtiene un Role a partir de su código
     */
    public static Role fromCode(String code) {
        for (Role role : Role.values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + code);
    }
}
