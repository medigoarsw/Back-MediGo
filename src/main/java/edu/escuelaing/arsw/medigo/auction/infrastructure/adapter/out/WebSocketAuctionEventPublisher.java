package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionEventPublisherPort;
import edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto.AuctionPriceUpdateMessage;
import edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto.BidPlacedMessage;
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
        try {
            // 1. Notificar a los participantes de la subasta específica
            String auctionTopic = "/topic/auction/" + auctionId;
            messagingTemplate.convertAndSend(auctionTopic, AuctionPriceUpdateMessage.from(event));

            // 2. Notificar al mercado global de subastas
            messagingTemplate.convertAndSend("/topic/auctions", AuctionPriceUpdateMessage.from(event));

            // 3. Si hay una nueva puja, notificar al historial de pujas
            if (event.getType() == AuctionEvent.EventType.BID_PLACED) {
                String bidsTopic = "/topic/auction/" + auctionId + "/bids";
                messagingTemplate.convertAndSend(bidsTopic, BidPlacedMessage.from(event));
            }

            log.debug("Evento {} publicado exitosamente para subasta {}", event.getType(), auctionId);
        } catch (Exception e) {
            log.error("Error publicando evento de subasta via WebSocket: {}", e.getMessage());
            // No relanzamos la excepción para cumplir con los tests de resiliencia (HU-20)
        }
    }
}
