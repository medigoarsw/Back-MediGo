package edu.escuelaing.arsw.medigo.users.domain.port.in;

import edu.escuelaing.arsw.medigo.users.domain.model.User;

/**
 * PUERTO DE ENTRADA (Inbound Port)
 * 
 * Define el contrato de la característica de autenticación.
 * La aplicación implementará este puerto (caso de uso).
 * Los adaptadores de entrada (ej: Controller REST) usarán este puerto.
 * 
 * 🔑 BENEFICIO PRINCIPAL:
 * Si mañana necesitas autenticación por GraphQL, REST, gRPC, WebSocket, etc.,
 * solo creas un nuevo adaptador que implementa este puerto.
 * El dominio + app NO cambian.
 * 
 * FLUJO HEXAGONAL:
 * [Adaptador REST Controller] --(implementa)--> [AuthUseCase]
 *                                                     ↑
 *                                              [AuthService en Application]
 */
public interface AuthUseCase {
    
    /**
     * Autentica un usuario basándose en username y password
     * 
     * @param username identificador único del usuario
     * @param password contraseña en texto plano
     * @return Usuario autenticado con todas sus propiedades
     * @throws edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException si el usuario no existe
     * @throws edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException si las credenciales son incorrectas
     */
    User authenticate(String username, String password);
    
    /**
     * Obtiene un usuario por su ID
     */
    User getUserById(Long userId);
    
    /**
     * Obtiene un usuario por su email
     */
    User getUserByEmail(String email);
}