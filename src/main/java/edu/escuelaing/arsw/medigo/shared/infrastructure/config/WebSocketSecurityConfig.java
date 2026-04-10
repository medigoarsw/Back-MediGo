package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Seguridad de WebSocket desactivada temporalmente para pruebas de conectividad.
 * Una vez confirmada la conexión, habilitaremos las reglas de acceso.
 */
@Configuration
public class WebSocketSecurityConfig {
    // Clase vacía sin @EnableWebSocketSecurity para permitir paso libre
}
