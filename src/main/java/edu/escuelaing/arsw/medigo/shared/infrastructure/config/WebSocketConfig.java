package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint base para WebSockets. 
        // Permitimos todos los orígenes para eliminar bloqueos de CORS en esta fase.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic para mensajes broadcast (1 a muchos)
        // /queue para mensajes privados (1 a 1)
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para mensajes que van del cliente al servidor (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para mensajes dirigidos a usuarios específicos
        registry.setUserDestinationPrefix("/user");
    }
}
