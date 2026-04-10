package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDeliveryJpaRepository extends JpaRepository<DeliveryEntity, Long> {
    Optional<DeliveryEntity> findByOrderId(Long orderId);
    
    @Query("SELECT d FROM DeliveryEntity d WHERE d.deliveryPersonId = :driverId AND d.status != 'DELIVERED'")
    List<DeliveryEntity> findActiveByDriverId(Long driverId);

    @Modifying
    @Query("UPDATE DeliveryEntity d SET d.status = :status WHERE d.id = :id")
    void updateStatus(Long id, Delivery.DeliveryStatus status);
}
