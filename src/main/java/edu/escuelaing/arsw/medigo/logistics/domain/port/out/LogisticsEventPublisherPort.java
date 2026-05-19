package edu.escuelaing.arsw.medigo.logistics.domain.port.out;

import java.util.Map;

/**
 * Puerto para publicar eventos de logística (ej. cambios de estado de pedidos)
 * hacia servicios externos (vía Redis Pub/Sub).
 */
public interface LogisticsEventPublisherPort {
    void publishOrderStatusUpdate(Long orderId, Map<String, Object> payload);
}
