package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.auction.domain.port.in.QueryAuctionUseCase;

import java.time.ZoneOffset;

public record WonAuctionResponse(
        Long   auctionId,
        String medicationName,
        String lotLabel,
        Long   branchId,
        java.math.BigDecimal finalAmount,
        String wonAt,
        String status,
        String closureType
) {
    public static WonAuctionResponse from(QueryAuctionUseCase.WonAuctionView view) {
        String wonAtIso = view.wonAt() != null
                ? view.wonAt().atOffset(ZoneOffset.UTC).toInstant().toString()
                : null;

        return new WonAuctionResponse(
                view.auctionId(),
                view.medicationName(),
                view.lotLabel(),
                view.branchId(),
                view.finalAmount(),
                wonAtIso,
                view.status(),
                view.closureType()
        );
    }
}
