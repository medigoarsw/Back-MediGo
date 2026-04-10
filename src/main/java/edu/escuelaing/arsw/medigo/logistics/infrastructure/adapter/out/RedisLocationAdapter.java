package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LocationStatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLocationAdapter implements LocationStatePort {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, LocationUpdate> currentLocations = new ConcurrentHashMap<>();

    @Override
    public void saveLocation(LocationUpdate location) {
        currentLocations.put(location.getDeliveryId(), location);
        log.debug("Location saved in-memory for delivery {}", location.getDeliveryId());
    }

    @Override
    public Optional<LocationUpdate> getLocation(Long deliveryId) {
        return Optional.ofNullable(currentLocations.get(deliveryId));
    }

    @Override
    public void publishLocationUpdate(LocationUpdate location) {
        log.debug("Broadcasting location update to STOMP topic /topic/deliveries");
        // Convertimos el modelo de dominio al formato que espera el frontend
        messagingTemplate.convertAndSend("/topic/deliveries", Map.of(
                "id", "delivery-" + location.getDeliveryId(),
                "latitude", location.getLat(),
                "longitude", location.getLng(),
                "status", "active",
                "timestamp", location.getTimestamp()
        ));
    }
}
