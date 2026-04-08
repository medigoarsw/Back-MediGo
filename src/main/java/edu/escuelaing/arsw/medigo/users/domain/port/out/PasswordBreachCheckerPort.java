package edu.escuelaing.arsw.medigo.users.domain.port.out;

/**
 * Puerto de salida para validar si una contraseña ya apareció en brechas públicas.
 */
public interface PasswordBreachCheckerPort {

    /**
     * @param plainPassword contraseña en texto plano
     * @return true si la contraseña fue comprometida en una brecha conocida
     */
    boolean isCompromised(String plainPassword);
}
