package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionResponse(
    Long          id,
    Long          medicationId,
    Long          branchId,
    BigDecimal    basePrice,
    BigDecimal    maxPrice,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String        status,
    String        closureType,
    Long          winnerId
) {
    public static AuctionResponse from(Auction a) {
        return new AuctionResponse(
            a.getId(), a.getMedicationId(), a.getBranchId(),
            a.getBasePrice(), a.getMaxPrice(),
            a.getStartTime(), a.getEndTime(),
            a.getStatus().name(), a.getClosureType().name(),
            a.getWinnerId()
        );
    }
}
