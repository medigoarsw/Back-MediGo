package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.auction.domain.port.in.PlaceBidUseCase;
import edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto.PlaceBidRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

/**
 * Pruebas del canal STOMP de subastas.
 *
 * Verifica que AuctionWebSocketHandler delega correctamente a PlaceBidUseCase
 * con los parámetros del mensaje STOMP, reproduciendo el mismo flujo que
 * el endpoint REST POST /api/auctions/{id}/bids.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionWebSocketHandler - Canal STOMP /app/auction/{id}/bid")
class AuctionWebSocketHandlerTest {

    @Mock
    PlaceBidUseCase placeBidUseCase;

    @InjectMocks
    AuctionWebSocketHandler handler;

    @Test
    @DisplayName("handleBid delega a PlaceBidUseCase con los parámetros correctos")
    void handleBid_delegatesToPlaceBidUseCase() {
        Long auctionId = 10L;
        PlaceBidRequest request = new PlaceBidRequest(3L, "Carlos", BigDecimal.valueOf(5500));

        handler.handleBid(auctionId, request);

        verify(placeBidUseCase).placeBid(auctionId, 3L, "Carlos", BigDecimal.valueOf(5500));
    }

    @Test
    @DisplayName("handleBid es equivalente al flujo REST: mismo caso de uso")
    void handleBid_sameUseCaseAsRest() {
        Long auctionId = 7L;
        PlaceBidRequest request = new PlaceBidRequest(9L, "Sofia", BigDecimal.valueOf(12000));

        handler.handleBid(auctionId, request);

        // PlaceBidUseCase.placeBid es llamado exactamente una vez con todos los argumentos
        verify(placeBidUseCase).placeBid(7L, 9L, "Sofia", BigDecimal.valueOf(12000));
    }
}
