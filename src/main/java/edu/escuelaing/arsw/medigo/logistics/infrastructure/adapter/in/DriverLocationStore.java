package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Store en Redis de la última posición GPS de cada repartidor activo.
 *
 * Un repartidor se considera "online" si envió una actualización en los
 * últimos ACTIVE_THRESHOLD_MS milisegundos.
 */
@Component
public class DriverLocationStore {

    private static final String REDIS_KEY_PREFIX = "driver:location:";
    private static final long ACTIVE_THRESHOLD_MS = 60_000; // 60 segundos
    private static final long REDIS_TTL_SECONDS = 60; // Auto-expiración

    public record DriverPosition(
            Long deliveryPersonId,
            Long deliveryId,
            double lat,
            double lng,
            long updatedAt
    ) {
        public boolean isActive() {
            return (Instant.now().toEpochMilli() - updatedAt) < ACTIVE_THRESHOLD_MS;
        }
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public DriverLocationStore(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void update(Long deliveryPersonId, Long deliveryId, double lat, double lng) {
        String key = REDIS_KEY_PREFIX + deliveryPersonId;
        DriverPosition pos = new DriverPosition(deliveryPersonId, deliveryId, lat, lng, Instant.now().toEpochMilli());
        redisTemplate.opsForValue().set(key, pos, REDIS_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /** Retorna solo los repartidores que enviaron posición en los últimos 60s. */
    public Collection<DriverPosition> getActiveDrivers() {
        Collection<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        
        return keys.stream()
                .map(key -> {
                    Object obj = redisTemplate.opsForValue().get(key);
                    if (obj == null) return null;
                    if (obj instanceof DriverPosition dp) {
                        return dp;
                    }
                    try {
                        return objectMapper.convertValue(obj, DriverPosition.class);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(DriverPosition::isActive)
                .collect(Collectors.toList());
    }

    public void remove(Long deliveryPersonId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + deliveryPersonId);
    }
}
