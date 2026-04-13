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
    Long          remainingSeconds,  // null si la subasta ya cerró
    BigDecimal    currentPrice       // puja más alta hasta el momento
) {
    /** Mapa básico: usado en listados. */
    public static AuctionResponse from(Auction a) {
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), null, null,
            a.getBranchId(), a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId(), null, null, null
        );
    }

    /** Mapa para subastas activas con currentPrice. */
    public static AuctionResponse fromActive(QueryAuctionUseCase.AuctionWithPrice ap) {
        Auction a = ap.auction();
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), null, null,
            a.getBranchId(), a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId(), null, null, ap.currentPrice()
        );
    }

    /** Mapa enriquecido: incluye datos de catálogo, tiempo restante, nombre del ganador y puja actual. */
    public static AuctionResponse fromDetail(QueryAuctionUseCase.AuctionDetailView detail) {
        Auction a = detail.auction();
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), detail.medicationName(), detail.medicationUnit(),
            a.getBranchId(), a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId(), detail.winnerName(), detail.remainingSeconds(), detail.currentPrice()
        );
    }
}
