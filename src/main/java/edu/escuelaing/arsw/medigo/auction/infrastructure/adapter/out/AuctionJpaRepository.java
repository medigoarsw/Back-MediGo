package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionRepositoryPort;
import edu.escuelaing.arsw.medigo.auction.infrastructure.entity.AuctionEntity;
import edu.escuelaing.arsw.medigo.auction.infrastructure.entity.BidEntity;
import edu.escuelaing.arsw.medigo.auction.infrastructure.persistence.SpringAuctionJpaRepository;
import edu.escuelaing.arsw.medigo.auction.infrastructure.persistence.SpringBidJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuctionJpaRepository implements AuctionRepositoryPort {

    private final SpringAuctionJpaRepository auctionRepo;
    private final SpringBidJpaRepository     bidRepo;

    // ── Auction CRUD ──────────────────────────────────────────────

    @Override
    public Auction save(Auction auction) {
        AuctionEntity entity = toEntity(auction);
        return toDomain(auctionRepo.save(entity));
    }

    @Override
    public Optional<Auction> findById(Long id) {
        return auctionRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Auction> findActiveAuctions() {
        return auctionRepo.findByStatus("ACTIVE").stream().map(this::toDomain).toList();
    }

    @Override
    public List<Auction> findExpiredActiveAuctions() {
        return auctionRepo.findExpiredActive(LocalDateTime.now())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Auction> findScheduledReadyToStart() {
        return auctionRepo.findScheduledReadyToStart(LocalDateTime.now())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveOrScheduledForMedication(Long medicationId) {
        return auctionRepo.existsActiveOrScheduledForMedication(medicationId);
    }

    @Override
    public void updateStatus(Long auctionId, Auction.AuctionStatus status) {
        auctionRepo.findById(auctionId).ifPresent(e -> {
            e.setStatus(status.name());
            auctionRepo.save(e);
        });
    }

    @Override
    public void setWinner(Long auctionId, Long winnerId) {
        auctionRepo.findById(auctionId).ifPresent(e -> {
            e.setWinnerId(winnerId);
            auctionRepo.save(e);
        });
    }

    @Override
    public void updateLastBidAt(Long auctionId, LocalDateTime ts) {
        auctionRepo.findById(auctionId).ifPresent(e -> {
            e.setLastBidAt(ts);
            auctionRepo.save(e);
        });
    }

    // ── Bid CRUD ──────────────────────────────────────────────────

    @Override
    public Bid saveBid(Bid bid) {
        BidEntity entity = BidEntity.builder()
                .auctionId(bid.getAuctionId())
                .userId(bid.getUserId())
                .userName(bid.getUserName())
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt())
                .build();
        return toBidDomain(bidRepo.save(entity));
    }

    @Override
    public Optional<Bid> findHighestBid(Long auctionId) {
        return bidRepo.findHighestBid(auctionId).map(this::toBidDomain);
    }

    @Override
    public List<Bid> findBidsByAuction(Long auctionId) {
        return bidRepo.findByAuctionIdOrderByPlacedAtDesc(auctionId)
                .stream().map(this::toBidDomain).toList();
    }

    // ── Mappers ───────────────────────────────────────────────────

    private AuctionEntity toEntity(Auction a) {
        return AuctionEntity.builder()
                .id(a.getId())
                .medicationId(a.getMedicationId())
                .branchId(a.getBranchId())
                .basePrice(a.getBasePrice())
                .maxPrice(a.getMaxPrice())
                .inactivityMinutes(a.getInactivityMinutes())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .lastBidAt(a.getLastBidAt())
                .status(a.getStatus().name())
                .closureType(a.getClosureType().name())
                .winnerId(a.getWinnerId())
                .build();
    }

    private Auction toDomain(AuctionEntity e) {
        return Auction.builder()
                .id(e.getId())
                .medicationId(e.getMedicationId())
                .branchId(e.getBranchId())
                .basePrice(e.getBasePrice())
                .maxPrice(e.getMaxPrice())
                .inactivityMinutes(e.getInactivityMinutes())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .lastBidAt(e.getLastBidAt())
                .status(Auction.AuctionStatus.valueOf(e.getStatus()))
                .closureType(Auction.ClosureType.valueOf(e.getClosureType()))
                .winnerId(e.getWinnerId())
                .build();
    }

    private Bid toBidDomain(BidEntity e) {
        return Bid.builder()
                .id(e.getId())
                .auctionId(e.getAuctionId())
                .userId(e.getUserId())
                .userName(e.getUserName())
                .amount(e.getAmount())
                .placedAt(e.getPlacedAt())
                .build();
    }
}
