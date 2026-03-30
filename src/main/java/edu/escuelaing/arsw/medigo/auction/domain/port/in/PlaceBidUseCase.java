package edu.escuelaing.arsw.medigo.auction.domain.port.in;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.math.BigDecimal;
public interface PlaceBidUseCase {
    Bid placeBid(Long auctionId, Long userId, BigDecimal amount);
}