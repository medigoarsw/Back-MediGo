package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class AuctionJpaRepository implements AuctionRepositoryPort {
    @Override public Auction save(Auction auction) { return auction; }
    @Override public Optional<Auction> findById(Long id) { return Optional.empty(); }
    @Override public List<Auction> findActiveAuctions() { return List.of(); }
    @Override public List<Auction> findExpiredActiveAuctions() { return List.of(); }
    @Override public Bid saveBid(Bid bid) { return bid; }
    @Override public Optional<Bid> findHighestBid(Long auctionId) { return Optional.empty(); }
    @Override public List<Bid> findBidsByAuction(Long auctionId) { return List.of(); }
    @Override public void updateStatus(Long auctionId, Auction.AuctionStatus status) {}
    @Override public void setWinner(Long auctionId, Long winnerId) {}
}