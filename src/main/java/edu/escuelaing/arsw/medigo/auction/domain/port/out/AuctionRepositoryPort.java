package edu.escuelaing.arsw.medigo.auction.domain.port.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.util.List;
import java.util.Optional;

public interface AuctionRepositoryPort {
    Auction           save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction>     findActiveAuctions();
    List<Auction>     findExpiredActiveAuctions();
    List<Auction>     findScheduledReadyToStart();
    boolean           existsActiveOrScheduledForMedication(Long medicationId);
    Bid               saveBid(Bid bid);
    Optional<Bid>     findHighestBid(Long auctionId);
    List<Bid>         findBidsByAuction(Long auctionId);
    void              updateStatus(Long auctionId, Auction.AuctionStatus status);
    void              setWinner(Long auctionId, Long winnerId);
    void              updateLastBidAt(Long auctionId, java.time.LocalDateTime ts);
    // Segundo lugar: la puja más alta excluyendo al ganador original
    Optional<Bid>     findSecondHighestBid(Long auctionId, Long excludeUserId);
    WonAuctionsPage   findWonAuctionsByWinnerId(Long winnerId, int page, int size);

    record WonAuctionRecord(
        Auction auction,
        Bid     winningBid
    ) {}

    record WonAuctionsPage(
        List<WonAuctionRecord> content,
        int                    page,
        int                    size,
        long                   totalElements,
        int                    totalPages
    ) {}
}
