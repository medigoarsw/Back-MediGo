package edu.escuelaing.arsw.medigo.auction.infrastructure.persistence;

import edu.escuelaing.arsw.medigo.auction.infrastructure.entity.BidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SpringBidJpaRepository extends JpaRepository<BidEntity, Long> {

    List<BidEntity> findByAuctionIdOrderByPlacedAtDesc(Long auctionId);

    // Puja mas alta actual (para validar nueva puja)
    @Query("SELECT b FROM BidEntity b WHERE b.auctionId = :auctionId " +
           "ORDER BY b.amount DESC LIMIT 1")
    Optional<BidEntity> findHighestBid(@Param("auctionId") Long auctionId);

    // Segunda puja más alta (para re-adjudicación), excluyendo al ganador original
    @Query("SELECT b FROM BidEntity b WHERE b.auctionId = :auctionId " +
           "AND b.userId <> :excludeUserId ORDER BY b.amount DESC LIMIT 1")
    Optional<BidEntity> findSecondHighestBid(@Param("auctionId") Long auctionId,
                                              @Param("excludeUserId") Long excludeUserId);

    // Obtiene todas las pujas para múltiples subastas
    @Query("SELECT b FROM BidEntity b " +
           "WHERE b.auctionId IN :auctionIds " +
           "ORDER BY b.auctionId, b.amount DESC, b.placedAt DESC")
    List<BidEntity> findBidsByAuctionIds(@Param("auctionIds") List<Long> auctionIds);
}
