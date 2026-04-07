package edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;

import java.math.BigDecimal;

/**
 * DTO inmutable enviado al topic:
 *   /topic/auction/{id}/bids  → historial de pujas en tiempo real
 *
 * IMPORTANTE: usa String en lugar de LocalDateTime para placedAt
 * por la misma razón que AuctionPriceUpdateMessage.
 */
public record BidPlacedMessage(
        Long       auctionId,
        BigDecimal amount,
        String     bidderName,
        Long       bidderId,
        String     placedAt    // ISO-8601, ej: "2025-04-06T20:30:00"
) {
    public static BidPlacedMessage from(AuctionEvent event) {
        return new BidPlacedMessage(
                event.getAuctionId(),
                event.getCurrentAmount(),
                event.getLeaderName(),
                event.getLeaderId(),
                event.getTimestamp() != null ? event.getTimestamp().toString() : null
        );
    }
}
