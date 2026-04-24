package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out.SpringDeliveryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Recibe actualizaciones GPS del repartidor, las persiste en el store en memoria
 * y las retransmite al tópico del afiliado.
 *
 * Flujo:
 *   Repartidor → STOMP /app/location/{deliveryId}  →  handleLocationUpdate()
 *             → store en memoria (DriverLocationStore)
 *             → broadcast → /topic/delivery/{deliveryId}/location
 *   Afiliado   ← suscribe /topic/delivery/{deliveryId}/location
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverLocationStore locationStore;
    private final SpringDeliveryJpaRepository deliveryRepo;

    @MessageMapping("/location/{deliveryId}")
    public void handleLocationUpdate(
            @DestinationVariable Long deliveryId,
            LocationPayload payload) {

        // Obtener deliveryPersonId desde la entrega para actualizar el store
        deliveryRepo.findById(deliveryId).ifPresent(delivery -> {
            locationStore.update(
                    delivery.getDeliveryPersonId(),
                    deliveryId,
                    payload.lat(),
                    payload.lng()
            );
            log.debug("GPS stored deliveryPersonId={} deliveryId={} lat={} lng={}",
                    delivery.getDeliveryPersonId(), deliveryId, payload.lat(), payload.lng());
        });

        // Rebroadcast al afiliado suscrito a este delivery
        messagingTemplate.convertAndSend(
                "/topic/delivery/" + deliveryId + "/location",
                payload
        );
    }

    /**
     * Payload GPS enviado por el repartidor.
     *
     * @param lat  latitud WGS-84
     * @param lng  longitud WGS-84
     * @param ts   timestamp epoch-ms (opcional)
     */
    public record LocationPayload(double lat, double lng, Long ts) {}
}
