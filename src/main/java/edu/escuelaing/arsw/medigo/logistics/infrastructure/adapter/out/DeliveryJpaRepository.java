package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;
@Component
public class DeliveryJpaRepository implements DeliveryRepositoryPort {
    // TODO Miguel: Spring Data JPA
    @Override public Delivery save(Delivery delivery) { return delivery; }
    @Override public Optional<Delivery> findByOrderId(Long orderId) { return Optional.empty(); }
    @Override public void updateStatus(Long deliveryId, Delivery.DeliveryStatus status) {}
}