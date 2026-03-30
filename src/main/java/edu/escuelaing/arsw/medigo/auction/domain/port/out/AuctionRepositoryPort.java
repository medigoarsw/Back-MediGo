package edu.escuelaing.arsw.medigo.auction.domain.port.out;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import java.util.*;
public interface AuctionRepositoryPort {
    Auction save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction> findActiveAuctions();
    List<Auction> findExpiredActiveAuctions();
    Bid saveBid(Bid bid);
    Optional<Bid> findHighestBid(Long auctionId);
    List<Bid> findBidsByAuction(Long auctionId);
    void updateStatus(Long auctionId, Auction.AuctionStatus status);
    void setWinner(Long auctionId, Long winnerId);
}