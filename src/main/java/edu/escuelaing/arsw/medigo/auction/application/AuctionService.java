package edu.escuelaing.arsw.medigo.auction.application;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service @RequiredArgsConstructor
public class AuctionService implements CreateAuctionUseCase, PlaceBidUseCase, CloseAuctionUseCase {
    private final AuctionRepositoryPort auctionRepository;
    private final BidLockPort bidLock;
    @Override public Auction createAuction(Long medicationId, Long branchId, BigDecimal basePrice, LocalDateTime startTime, LocalDateTime endTime) { throw new UnsupportedOperationException("TODO Juana"); }
    @Override public Auction updateAuction(Long auctionId, BigDecimal basePrice, LocalDateTime endTime) { throw new UnsupportedOperationException("TODO Juana"); }
    @Override @Transactional
    public Bid placeBid(Long auctionId, Long userId, BigDecimal amount) {
        throw new UnsupportedOperationException("TODO Juana: SETNX lock + persist + broadcast");
    }
    @Override @Transactional public Auction closeAuction(Long auctionId) { throw new UnsupportedOperationException("TODO Juana"); }
    @Override @Transactional public void adjudicateWinner(Long auctionId) { throw new UnsupportedOperationException("TODO Juana"); }
    @Scheduled(fixedRate = 60000)
    public void autoCloseExpiredAuctions() {
        // TODO Juana: buscar subastas expiradas y cerrarlas
    }
}