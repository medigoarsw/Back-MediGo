package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.time.Duration;
import java.util.List;

public interface QueryAuctionUseCase {
    List<Auction>     getActiveAuctions();
    Auction           getAuctionById(Long id);
    List<Bid>         getBidHistory(Long auctionId);
    AuctionDetailView getAuctionDetail(Long id);

    record AuctionDetailView(
        Auction  auction,
        String   medicationName,
        String   medicationUnit,
        Duration remainingTime,  // null if CLOSED / CANCELLED
        String   winnerName      // null if no winner yet
    ) {}
}
