error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/persistence/SpringOrderJpaRepository.java:java/util/Optional#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/persistence/SpringOrderJpaRepository.java
empty definition using pc, found symbol in pc: java/util/Optional#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 401
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/persistence/SpringOrderJpaRepository.java
text:
```scala
package edu.escuelaing.arsw.medigo.orders.infrastructure.persistence;

import edu.escuelaing.arsw.medigo.orders.infrastructure.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.@@Optional;

public interface SpringOrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByAffiliateId(Long affiliateId);

    Optional<OrderEntity> findByAuctionId(Long auctionId);

    @Query("SELECT o FROM OrderEntity o WHERE o.status = 'PENDING_PAYMENT' AND o.createdAt < :cutoff")
    List<OrderEntity> findPendingPaymentCreatedBefore(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT o FROM OrderEntity o WHERE o.affiliateId = :affiliateId AND o.branchId = :branchId AND o.status = 'PENDING' ORDER BY o.createdAt DESC")
    Optional<OrderEntity> findPendingByAffiliateAndBranch(@Param("affiliateId") Long affiliateId, @Param("branchId") Long branchId);
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/Optional#