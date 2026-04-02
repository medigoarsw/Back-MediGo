package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.QueryAuctionUseCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionResponse(
    Long          id,
    Long          medicationId,
    String        medicationName,
    String        medicationUnit,
    Long          branchId,
    BigDecimal    basePrice,
    BigDecimal    maxPrice,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String        status,
    String        closureType,
    Long          winnerId,
    String        winnerName,
    Long          remainingSeconds  // null si la subasta ya cerró
) {
    /** Mapa básico: usado en listados donde no se necesita detalle enriquecido. */
    public static AuctionResponse from(Auction a) {
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), null, null,
            a.getBranchId(), a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId(), null, null
        );
    }

    /** Mapa enriquecido: incluye datos de catálogo, tiempo restante y nombre del ganador. */
    public static AuctionResponse fromDetail(QueryAuctionUseCase.AuctionDetailView detail) {
        Auction a = detail.auction();
        Long remaining = detail.remainingTime() != null
                ? detail.remainingTime().getSeconds()
                : null;
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), detail.medicationName(), detail.medicationUnit(),
            a.getBranchId(), a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId(), detail.winnerName(), remaining
        );
    }
}
