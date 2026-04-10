package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeliveryJpaRepository implements DeliveryRepositoryPort {

    private final SpringDeliveryJpaRepository springRepo;

    @Override
    public Delivery save(Delivery delivery) {
        DeliveryEntity entity = DeliveryEntity.fromDomain(delivery);
        return springRepo.save(entity).toDomain();
    }

    @Override
    public Optional<Delivery> findByOrderId(Long orderId) {
        return springRepo.findByOrderId(orderId).map(DeliveryEntity::toDomain);
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        return springRepo.findById(id).map(DeliveryEntity::toDomain);
    }

    @Override
    public List<Delivery> findActiveByDeliveryPersonId(Long deliveryPersonId) {
        return springRepo.findActiveByDriverId(deliveryPersonId).stream()
                .map(DeliveryEntity::toDomain)
                .toList();
    }

    @Override
    public void updateStatus(Long deliveryId, Delivery.DeliveryStatus status) {
        springRepo.updateStatus(deliveryId, status);
    }
}
