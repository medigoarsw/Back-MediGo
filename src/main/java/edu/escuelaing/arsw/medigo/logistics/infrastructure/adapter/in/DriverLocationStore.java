package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store en memoria de la última posición GPS de cada repartidor activo.
 *
 * Un repartidor se considera "online" si envió una actualización en los
 * últimos ACTIVE_THRESHOLD_MS milisegundos.
 */
@Component
public class DriverLocationStore {

    private static final long ACTIVE_THRESHOLD_MS = 60_000; // 60 segundos

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

    private final ConcurrentHashMap<Long, DriverPosition> store = new ConcurrentHashMap<>();

    public void update(Long deliveryPersonId, Long deliveryId, double lat, double lng) {
        store.put(deliveryPersonId, new DriverPosition(
                deliveryPersonId, deliveryId, lat, lng, Instant.now().toEpochMilli()
        ));
    }

    /** Retorna solo los repartidores que enviaron posición en los últimos 60s. */
    public Collection<DriverPosition> getActiveDrivers() {
        store.entrySet().removeIf(e -> !e.getValue().isActive());
        return store.values();
    }

    public void remove(Long deliveryPersonId) {
        store.remove(deliveryPersonId);
    }
}
