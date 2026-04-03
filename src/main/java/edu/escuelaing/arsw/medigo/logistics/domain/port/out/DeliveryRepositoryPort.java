package edu.escuelaing.arsw.medigo.logistics.domain.port.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import java.util.List;
import java.util.Optional;
public interface DeliveryRepositoryPort {
    Delivery save(Delivery delivery);
    Optional<Delivery> findByOrderId(Long orderId);
    Optional<Delivery> findById(Long id);
    List<Delivery> findActiveByDeliveryPersonId(Long deliveryPersonId);
    void updateStatus(Long deliveryId, Delivery.DeliveryStatus status);
}