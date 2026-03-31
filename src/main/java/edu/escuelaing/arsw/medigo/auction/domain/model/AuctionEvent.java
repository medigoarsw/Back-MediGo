package edu.escuelaing.arsw.medigo.auction.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento inmutable que se publica via WebSocket a todos los participantes
 * cuando ocurre un cambio relevante en la subasta.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuctionEvent {

    public enum EventType {
        BID_PLACED,       // nueva puja registrada
        AUCTION_CLOSED,   // subasta cerrada
        AUCTION_STARTED,  // subasta activada
        WINNER_ADJUDICATED
    }

    private EventType     type;
    private Long          auctionId;
    private BigDecimal    currentAmount;
    private String        leaderName;
    private Long          leaderId;
    private LocalDateTime timestamp;
    private String        message;
}
