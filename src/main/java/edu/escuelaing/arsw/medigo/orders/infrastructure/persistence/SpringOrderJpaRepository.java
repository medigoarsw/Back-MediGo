package edu.escuelaing.arsw.medigo.orders.infrastructure.persistence;

import edu.escuelaing.arsw.medigo.orders.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringOrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByAffiliateId(Long affiliateId);

    Optional<OrderEntity> findByAuctionId(Long auctionId);

    @Query("SELECT o FROM OrderEntity o WHERE o.status = 'PENDING_PAYMENT' AND o.createdAt < :cutoff")
    List<OrderEntity> findPendingPaymentCreatedBefore(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT o FROM OrderEntity o WHERE o.affiliateId = :affiliateId AND o.branchId = :branchId AND o.status = 'PENDING' ORDER BY o.createdAt DESC")
    Optional<OrderEntity> findPendingByAffiliateAndBranch(@Param("affiliateId") Long affiliateId, @Param("branchId") Long branchId);
}
