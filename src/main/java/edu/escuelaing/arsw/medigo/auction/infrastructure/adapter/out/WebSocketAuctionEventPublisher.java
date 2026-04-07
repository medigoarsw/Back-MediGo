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
 * Publica eventos de subasta via WebSocket STOMP a tres topics:
 *
 *   /topic/auction/{id}        → precio y estado actual (todos los tipos de evento)
 *   /topic/auctions            → actualizaciones globales para listas en tiempo real
 *   /topic/auction/{id}/bids   → detalle de cada puja (solo BID_PLACED)
 *
 * Si la publicación falla, el error queda registrado en log pero NO
 * se propaga al llamador, preservando la integridad de la transacción
 * de puja (HU-19/HU-20).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuctionEventPublisher implements AuctionEventPublisherPort {

    static final String TOPIC_AUCTION     = "/topic/auction/";
    static final String TOPIC_AUCTIONS    = "/topic/auctions";
    static final String SUFFIX_BIDS       = "/bids";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(Long auctionId, AuctionEvent event) {
        try {
            AuctionPriceUpdateMessage priceUpdate = AuctionPriceUpdateMessage.from(event);

            // 1. Topic por subasta: participantes suscritos a esta subasta concreta
            messagingTemplate.convertAndSend(TOPIC_AUCTION + auctionId, priceUpdate);

            // 2. Topic global: permite actualizar listas de subastas activas
            messagingTemplate.convertAndSend(TOPIC_AUCTIONS, priceUpdate);

            // 3. Topic de pujas: solo cuando se registra una puja nueva
            if (event.getType() == AuctionEvent.EventType.BID_PLACED) {
                messagingTemplate.convertAndSend(
                        TOPIC_AUCTION + auctionId + SUFFIX_BIDS,
                        BidPlacedMessage.from(event));
            }

            log.info("[WS] Evento {} publicado → /topic/auction/{}, /topic/auctions{}",
                    event.getType(), auctionId,
                    event.getType() == AuctionEvent.EventType.BID_PLACED ? ", /topic/auction/" + auctionId + "/bids" : "");

        } catch (Exception ex) {
            // La publicación WS no debe romper la transacción de negocio.
            // Se registra como ERROR con stack trace completo para diagnóstico.
            log.error("[WS] Error publicando evento {} para subasta {}: {}",
                    event.getType(), auctionId, ex.getMessage(), ex);
        }
    }
}
