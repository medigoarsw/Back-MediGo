package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Recibe actualizaciones GPS del repartidor y las retransmite a los suscriptores del tópico.
 *
 * Flujo:
 *   Repartidor → STOMP /app/location/{deliveryId}  →  handleLocationUpdate()
 *             → broadcast → /topic/delivery/{deliveryId}/location
 *   Afiliado   ← suscribe /topic/delivery/{deliveryId}/location
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location/{deliveryId}")
    public void handleLocationUpdate(
            @DestinationVariable Long deliveryId,
            LocationPayload payload) {

        log.debug("GPS update deliveryId={} lat={} lng={}", deliveryId, payload.lat(), payload.lng());
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
     * @param ts   timestamp epoch-ms (opcional, el cliente puede ignorarlo)
     */
    public record LocationPayload(double lat, double lng, Long ts) {}
}
