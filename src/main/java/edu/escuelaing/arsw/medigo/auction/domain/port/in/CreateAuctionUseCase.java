package edu.escuelaing.arsw.medigo.auction.domain.port.in;
import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public interface CreateAuctionUseCase {
    Auction createAuction(Long medicationId, Long branchId, BigDecimal basePrice, LocalDateTime startTime, LocalDateTime endTime);
    Auction updateAuction(Long auctionId, BigDecimal basePrice, LocalDateTime endTime);
}