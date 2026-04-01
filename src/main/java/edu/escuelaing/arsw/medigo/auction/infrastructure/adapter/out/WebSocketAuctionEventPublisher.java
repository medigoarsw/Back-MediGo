package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de subasta via WebSocket STOMP.
 * Topic: /topic/auction/{auctionId}
 * Los clientes React se suscriben a este topic para recibir
 * actualizaciones en tiempo real (HU-20).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuctionEventPublisher implements AuctionEventPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(Long auctionId, AuctionEvent event) {
        String destination = "/topic/auction/" + auctionId;
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Evento {} publicado en {}", event.getType(), destination);
    }
}
