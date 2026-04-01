package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface UpdateAuctionUseCase {

    Auction updateAuction(Long auctionId, UpdateAuctionCommand command);

    record UpdateAuctionCommand(
        BigDecimal    basePrice,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {}
}
