package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.auction.domain.port.in.PlaceBidUseCase;
import edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto.PlaceBidRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * Maneja mensajes STOMP entrantes para subastas.
 *
 * Flujo de puja via WebSocket:
 *   Cliente envia a: /app/auction/{auctionId}/bid
 *   Este handler llama a PlaceBidUseCase (mismo caso de uso que el REST)
 *   El resultado se publica a:  /topic/auction/{auctionId}
 *   Todos los clientes suscritos reciben el AuctionEvent automaticamente
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuctionWebSocketHandler {

    private final PlaceBidUseCase placeBidUseCase;

    @MessageMapping("/auction/{auctionId}/bid")
    public void handleBid(@DestinationVariable Long auctionId,
                          PlaceBidRequest request) {
        log.debug("Puja WS recibida: subasta={} user={} amount={}",
                auctionId, request.userId(), request.amount());

        placeBidUseCase.placeBid(
            auctionId,
            request.userId(),
            request.userName(),
            request.amount()
        );
        // El evento se publica automaticamente desde AuctionService
        // hacia /topic/auction/{auctionId} via WebSocketAuctionEventPublisher
    }
}
