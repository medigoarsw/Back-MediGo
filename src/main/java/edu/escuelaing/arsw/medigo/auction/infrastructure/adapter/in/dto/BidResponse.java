package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
    Long          id,
    Long          auctionId,
    Long          userId,
    String        userName,
    BigDecimal    amount,
    LocalDateTime placedAt
) {
    public static BidResponse from(Bid b) {
        return new BidResponse(
            b.getId(), b.getAuctionId(), b.getUserId(),
            b.getUserName(), b.getAmount(), b.getPlacedAt()
        );
    }
}
