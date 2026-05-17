package edu.escuelaing.arsw.medigo.shared.infrastructure.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio centralizado para enviar telemetría a Azure Application Insights.
 * Evita la duplicación de código y centraliza el manejo de TelemetryClient.
 */
@Service
public class TelemetryService {

    private final TelemetryClient telemetryClient;

    public TelemetryService() {
        // En un entorno de Spring Boot con Application Insights, 
        // TelemetryClient se inicializa automáticamente cuando la dependencia o agente están presentes.
        this.telemetryClient = new TelemetryClient();
    }

    /**
     * Registra un evento de login exitoso.
     *
     * @param userId ID del usuario autenticado
     * @param role   Rol del usuario
     */
    public void trackUserLogin(Long userId, String role) {
        Map<String, String> properties = new HashMap<>();
        properties.put("userId", userId != null ? userId.toString() : "unknown");
        properties.put("role", role != null ? role : "unknown");

        telemetryClient.trackEvent("user_login", properties, null);
    }

    /**
     * Registra un evento de registro exitoso.
     *
     * @param userId ID del usuario registrado
     * @param role   Rol asignado al usuario
     */
    public void trackUserRegistered(Long userId, String role) {
        Map<String, String> properties = new HashMap<>();
        properties.put("userId", userId != null ? userId.toString() : "unknown");
        properties.put("role", role != null ? role : "unknown");

        telemetryClient.trackEvent("user_registered", properties, null);
    }
}
