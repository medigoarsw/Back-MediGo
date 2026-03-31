package edu.escuelaing.arsw.medigo.users.domain.util;

/**
 * VALIDADOR DE CONTRASEÑA
 * 
 * Contiene lógica de negocio para validar que las contraseñas cumplan
 * con criterios de seguridad.
 */
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final String UPPERCASE_PATTERN = "[A-Z]";
    private static final String LOWERCASE_PATTERN = "[a-z]";
    private static final String DIGIT_PATTERN = "[0-9]";
    private static final String SPECIAL_CHAR_PATTERN = "[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~`]";
    
    /**
     * Valida que la contraseña sea fuerte
     * 
     * Requisitos:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un dígito
     * - Al menos un carácter especial
     * 
     * @param password contraseña a validar
     * @return true si es válida, false en caso contrario
     */
    public static boolean isStrong(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        
        return password.matches(".*" + UPPERCASE_PATTERN + ".*") &&
               password.matches(".*" + LOWERCASE_PATTERN + ".*") &&
               password.matches(".*" + DIGIT_PATTERN + ".*") &&
               password.matches(".*" + SPECIAL_CHAR_PATTERN + ".*");
    }
    
    /**
     * Retorna un mensaje descriptivo sobre los requisitos de contraseña
     */
    public static String getRequirementsMessage() {
        return "La contraseña debe tener mínimo 8 caracteres, " +
               "al menos una mayúscula, una minúscula, un dígito y un carácter especial (!@#$%^&*)";
    }
}
