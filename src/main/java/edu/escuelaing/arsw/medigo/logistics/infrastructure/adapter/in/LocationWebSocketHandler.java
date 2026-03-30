package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
@Controller
public class LocationWebSocketHandler {
    @MessageMapping("/location/{deliveryId}")
    public void handleLocationUpdate(@DestinationVariable Long deliveryId, Object payload) {
        // TODO Anderson: broadcast GPS via SignalR
    }
}