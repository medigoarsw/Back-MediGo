package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import edu.escuelaing.arsw.medigo.logistics.application.DeliveryBroadcastService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketHandler {

    private final DeliveryBroadcastService broadcastService;

    @MessageMapping("/location/{deliveryId}")
    public void handleLocationUpdate(@DestinationVariable String deliveryId, Object payload, Principal principal) {
        log.info("Location update received for delivery {}: {}", deliveryId, payload);
        
        // Regla Tarea 5: Validar que el repartidor solo publica su propia ubicación
        // El principal.getName() devuelve el userId extraído del token en el interceptor
        if (principal == null || !deliveryId.equals(principal.getName())) {
            log.warn("Unauthorized location update attempt: User {} tried to update location for delivery {}", 
                    principal != null ? principal.getName() : "Anonymous", deliveryId);
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para actualizar esta ubicación");
        }
        
        // Update state and broadcast via service
        broadcastService.updateLocation(deliveryId, payload);
    }

    @MessageExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @SendToUser("/queue/errors")
    public String handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e) {
        return e.getMessage();
    }
}