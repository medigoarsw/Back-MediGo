package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
@Controller
public class AuctionWebSocketHandler {
    @MessageMapping("/auction/{auctionId}/bid")
    public void handleBid(@DestinationVariable Long auctionId, Object payload) {
        // TODO Juana: procesar puja y broadcast
    }
}