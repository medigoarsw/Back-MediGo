package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out.SpringDeliveryJpaRepository;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
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
 * Dos canales:
 *   /app/location/{deliveryId}   — repartidor con pedido activo (deliveryId numérico)
 *   /app/location/u{userId}      — repartidor sin pedido activo (prefijo "u" + userId)
 *
 * Afiliado suscribe /topic/delivery/{deliveryId}/location para seguimiento en tiempo real.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverLocationStore locationStore;
    private final SpringDeliveryJpaRepository deliveryRepo;
    private final UserJpaRepository userRepo;

    /**
     * Canal con pedido activo: /app/location/{deliveryId}
     * Guarda posición y rebroadcastea al afiliado.
     */
    @MessageMapping("/location/{deliveryId}")
    public void handleLocationUpdate(
            @DestinationVariable Long deliveryId,
            LocationPayload payload) {

        deliveryRepo.findById(deliveryId).ifPresent(delivery -> {
            locationStore.update(
                    delivery.getDeliveryPersonId(),
                    deliveryId,
                    payload.lat(),
                    payload.lng()
            );
            log.debug("GPS(delivery) deliveryPersonId={} deliveryId={} lat={} lng={}",
                    delivery.getDeliveryPersonId(), deliveryId, payload.lat(), payload.lng());
        });

        messagingTemplate.convertAndSend(
                "/topic/delivery/" + deliveryId + "/location",
                payload
        );
    }

    /**
     * Canal sin pedido activo: /app/location/u{userId}
     * Solo guarda posición en el store — no hay afiliado suscrito todavía.
     */
    @MessageMapping("/location/u{userId}")
    public void handleFreeDriverLocationUpdate(
            @DestinationVariable Long userId,
            LocationPayload payload) {

        userRepo.findById(userId).ifPresent(user -> {
            locationStore.update(
                    userId,
                    null,   // sin deliveryId
                    payload.lat(),
                    payload.lng()
            );
            log.debug("GPS(free) userId={} lat={} lng={}", userId, payload.lat(), payload.lng());
        });
    }

    public record LocationPayload(double lat, double lng, Long ts) {}
}
