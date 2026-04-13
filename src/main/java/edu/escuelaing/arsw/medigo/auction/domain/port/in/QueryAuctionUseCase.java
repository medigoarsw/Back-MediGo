package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface QueryAuctionUseCase {
    List<Auction>              getActiveAuctions();
    List<AuctionWithPrice>     getActiveAuctionsWithCurrentPrice();
    Auction                    getAuctionById(Long id);
    List<Bid>                  getBidHistory(Long auctionId);
    AuctionDetailView          getAuctionDetail(Long id);
    WinnerView                 getAuctionWinner(Long auctionId);
    WonAuctionsPageView        getWonAuctionsByAffiliate(Long affiliateId, int page, int size);

    record AuctionDetailView(
        Auction    auction,
        String     medicationName,
        String     medicationUnit,
        Long       remainingSeconds,  // null if CLOSED / CANCELLED (en segundos)
        String     winnerName,        // null if no winner yet
        BigDecimal currentPrice       // puja más alta hasta el momento (null si no hay pujas)
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

    record WonAuctionView(
        Long          auctionId,
        String        medicationName,
        String        lotLabel,
        Long          branchId,
        BigDecimal    finalAmount,
        LocalDateTime wonAt,
        String        status,
        String        closureType
    ) {}

    record WonAuctionsPageView(
        List<WonAuctionView> content,
        int                  page,
        int                  size,
        long                 totalElements,
        int                  totalPages
    ) {}
}
