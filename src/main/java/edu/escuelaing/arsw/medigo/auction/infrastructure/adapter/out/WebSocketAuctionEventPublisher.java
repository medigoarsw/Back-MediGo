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
        AuctionPriceUpdateMessage priceUpdate = AuctionPriceUpdateMessage.from(event);

        // Cada topic se publica de forma aislada para evitar que un fallo puntual
        // impida que los demás clientes reciban actualizaciones en tiempo real.
        safeSend(TOPIC_AUCTION + auctionId, priceUpdate, event, auctionId);
        safeSend(TOPIC_AUCTIONS, priceUpdate, event, auctionId);

        if (event.getType() == AuctionEvent.EventType.BID_PLACED) {
            safeSend(
                    TOPIC_AUCTION + auctionId + SUFFIX_BIDS,
                    BidPlacedMessage.from(event),
                    event,
                    auctionId
            );
        }

        log.info("[WS] Evento {} publicado → /topic/auction/{}, /topic/auctions{}",
                event.getType(), auctionId,
                event.getType() == AuctionEvent.EventType.BID_PLACED ? ", /topic/auction/" + auctionId + "/bids" : "");
    }

    private void safeSend(String destination, Object payload, AuctionEvent event, Long auctionId) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception ex) {
            // La publicación WS no debe romper la transacción de negocio.
            log.error("[WS] Error publicando evento {} para subasta {} hacia {}: {}",
                    event.getType(), auctionId, destination, ex.getMessage(), ex);
        }
    }
}
