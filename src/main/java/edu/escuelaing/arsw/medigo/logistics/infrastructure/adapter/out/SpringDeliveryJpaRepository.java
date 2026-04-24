package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for DeliveryEntity.
 * HU-10: Supports finding deliveries by ID and querying active deliveries.
 */
public interface SpringDeliveryJpaRepository extends JpaRepository<DeliveryEntity, Long> {

    Optional<DeliveryEntity> findByOrderId(Long orderId);

    /**
     * Finds all deliveries for a given delivery person that are not yet DELIVERED.
     * Statuses considered "active": ASSIGNED, IN_ROUTE.
     */
    @Query("SELECT d FROM DeliveryEntity d WHERE d.deliveryPersonId = :deliveryPersonId AND d.status <> 'DELIVERED'")
    List<DeliveryEntity> findActiveByDeliveryPersonId(@Param("deliveryPersonId") Long deliveryPersonId);
}
