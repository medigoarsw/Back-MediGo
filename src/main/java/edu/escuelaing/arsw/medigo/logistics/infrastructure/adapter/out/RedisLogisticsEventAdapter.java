package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LogisticsEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLogisticsEventAdapter implements LogisticsEventPublisherPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishOrderStatusUpdate(Long orderId, Map<String, Object> payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            // Publicamos en un canal que el servicio WebSocket escuchará
            redisTemplate.convertAndSend("logistics:order:status", message);
            log.debug("Evento de estado de pedido publicado en Redis para orderId: {}", orderId);
        } catch (Exception e) {
            log.error("Error al publicar evento de logística en Redis", e);
        }
    }
}
