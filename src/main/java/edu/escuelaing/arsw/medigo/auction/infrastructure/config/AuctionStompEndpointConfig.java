package edu.escuelaing.arsw.medigo.auction.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Configuración STOMP dedicada a subastas.
 *
 * Responsabilidades:
 *  1. Agrega el endpoint /ws/auctions con SockJS para clientes React/Angular.
 *  2. Configura el MappingJackson2MessageConverter del broker STOMP para usar
 *     el ObjectMapper auto-configurado por Spring Boot (con JavaTimeModule,
 *     soporte para records Java, etc.).
 *
 * Por qué es necesario el punto 2:
 *   Spring Boot auto-configura el ObjectMapper global con JavaTimeModule, pero
 *   WebSocketMessagingAutoConfiguration se salta cuando ya hay un bean de tipo
 *   WebSocketMessageBrokerConfigurer propio (WebSocketConfig). Esto hace que el
 *   MappingJackson2MessageConverter del broker STOMP se cree con un ObjectMapper
 *   vacío que NO soporta LocalDateTime ni java.time en general.
 *   El resultado es que convertAndSend() lanza InvalidDefinitionException al
 *   serializar los DTOs, excepción que el publisher captura silenciosamente →
 *   el mensaje nunca llega a los clientes.
 *
 * Solución: inyectar el ObjectMapper de Spring Boot y registrarlo aquí.
 *
 * Seguridad: /ws/auctions está cubierto por la regla /ws/** de SecurityConfig.
 */
@Configuration
public class AuctionStompEndpointConfig implements WebSocketMessageBrokerConfigurer {

    /** ObjectMapper auto-configurado por Spring Boot (con JavaTimeModule, etc.). */
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/auctions")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // No-op: el broker ya está configurado en WebSocketConfig.
    }

    /**
     * Registra el converter JSON del broker STOMP con el ObjectMapper correcto.
     *
     * Retorna false para indicar que no se deben agregar los converters por defecto
     * (que usarían un ObjectMapper sin JavaTimeModule). Agregamos manualmente los
     * tres converters estándar, con el JSON usando el ObjectMapper de Spring Boot.
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Converters de texto y binario (necesarios para el protocolo STOMP)
        messageConverters.add(new StringMessageConverter());
        messageConverters.add(new ByteArrayMessageConverter());

        // Converter JSON con el ObjectMapper de Spring Boot
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        jsonConverter.setContentTypeResolver(resolver);
        messageConverters.add(jsonConverter);

        return false; // No agregar converters por defecto; usamos los de arriba
    }
}
