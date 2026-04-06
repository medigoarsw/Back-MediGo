package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public interface QueryAuctionUseCase {
    List<Auction>              getActiveAuctions();
    List<AuctionWithPrice>     getActiveAuctionsWithCurrentPrice();
    Auction                    getAuctionById(Long id);
    List<Bid>                  getBidHistory(Long auctionId);
    AuctionDetailView          getAuctionDetail(Long id);
    WinnerView                 getAuctionWinner(Long auctionId);

    record AuctionDetailView(
        Auction  auction,
        String   medicationName,
        String   medicationUnit,
        Duration remainingTime,  // null if CLOSED / CANCELLED
        String   winnerName,     // null if no winner yet
        BigDecimal currentPrice  // puja más alta hasta el momento (null si no hay pujas)
    ) {}

    record WinnerView(
        Long       auctionId,
        Long       winnerId,
        String     winnerName,
        BigDecimal winningAmount // monto de la puja ganadora
    ) {}

    record AuctionWithPrice(
        Auction    auction,
        BigDecimal currentPrice  // puja más alta (null si no hay pujas)
    ) {}
}
