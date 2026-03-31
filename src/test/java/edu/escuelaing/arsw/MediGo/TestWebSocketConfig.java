package edu.escuelaing.arsw.MediGo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@TestConfiguration
public class TestWebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TextWebSocketHandler() {}, "/test").setAllowedOrigins("*");
    }
    
    @Bean
    public TextWebSocketHandler testWebSocketHandler() {
        return new TextWebSocketHandler();
    }
}