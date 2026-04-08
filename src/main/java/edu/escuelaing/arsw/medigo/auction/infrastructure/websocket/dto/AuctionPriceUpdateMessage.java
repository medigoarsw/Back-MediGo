package edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;

import java.math.BigDecimal;

/**
 * DTO inmutable enviado a los topics:
 *   /topic/auction/{id}   → detalle de subasta para participantes
 *   /topic/auctions       → lista global de subastas activas
 *
 * IMPORTANTE: usa String en lugar de LocalDateTime para timestamp
 * porque el MappingJackson2MessageConverter del broker STOMP puede
 * no tener JavaTimeModule registrado; String es siempre serializable.
 */
public record AuctionPriceUpdateMessage(
        String     eventType,
        Long       auctionId,
        BigDecimal currentPrice,
        String     leaderName,
        Long       leaderId,
        String     timestamp,   // ISO-8601, ej: "2025-04-06T20:30:00"
        String     message
) {
    public static AuctionPriceUpdateMessage from(AuctionEvent event) {
        return new AuctionPriceUpdateMessage(
                event.getType().name(),
                event.getAuctionId(),
                event.getCurrentAmount(),
                event.getLeaderName(),
                event.getLeaderId(),
                event.getTimestamp() != null ? event.getTimestamp().toString() : null,
                event.getMessage()
        );
    }
}
