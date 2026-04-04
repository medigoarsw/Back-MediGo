package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CreateAuctionUseCase {

    Auction createAuction(CreateAuctionCommand command);

    record CreateAuctionCommand(
        Long          medicationId,
        Long          branchId,
        BigDecimal    basePrice,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Auction.ClosureType closureType,
        BigDecimal    maxPrice,          // solo para MAX_PRICE
        Integer       inactivityMinutes  // solo para INACTIVITY
    ) {}
}
