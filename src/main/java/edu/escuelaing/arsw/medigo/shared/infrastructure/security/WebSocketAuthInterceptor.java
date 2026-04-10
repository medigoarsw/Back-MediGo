package edu.escuelaing.arsw.medigo.shared.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

/**
 * Interceptor de autenticación para WebSockets.
 * Solo parsea el JWT, NO accede a la base de datos para evitar corromper transacciones.
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String FAKE_JWT_PREFIX = "fake-jwt";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length()).trim();
                
                try {
                    String role = extractRole(token);
                    String userId = extractUserId(token);

                    if (role != null) {
                        String normalized = role.toUpperCase().startsWith("ROLE_")
                                ? role.substring(5).toUpperCase()
                                : role.toUpperCase();

                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + normalized);
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId != null ? userId : "user", 
                                null, 
                                List.of(authority)
                        );
                        
                        accessor.setUser(auth);
                        log.info("WS Auth: Connection authenticated for user {} with role {}", userId, role);
                    }
                } catch (Exception e) {
                    log.error("WS Auth: Silent error during token parsing", e);
                }
            }
        }
        return message;
    }

    private String extractRole(String token) {
        if (token.startsWith(FAKE_JWT_PREFIX)) {
            String[] parts = token.split("\\.");
            return (parts.length >= 3) ? parts[2] : null;
        }
        return extractClaimFromRealJwt(token, "role");
    }

    private String extractUserId(String token) {
        if (token.startsWith(FAKE_JWT_PREFIX)) {
            String[] parts = token.split("\\.");
            return (parts.length >= 2) ? parts[1] : "0";
        }
        return extractClaimFromRealJwt(token, "sub");
    }

    private String extractClaimFromRealJwt(String token, String claimName) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = MAPPER.readTree(decoded);
            JsonNode node = payload.get(claimName);
            return (node != null) ? node.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
