package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter that persists Delivery using Spring Data Repository.
 * HU-10: Implements full CRUD + status update with deliveredAt timestamp.
 */
@Component
@RequiredArgsConstructor
public class DeliveryJpaRepository implements DeliveryRepositoryPort {

    private final SpringDeliveryJpaRepository springRepo;

    @Override
    public Delivery save(Delivery delivery) {
        DeliveryEntity entity = toEntity(delivery);
        return toDomain(springRepo.save(entity));
    }

    @Override
    public Optional<Delivery> findByOrderId(Long orderId) {
        return springRepo.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public Optional<Delivery> findById(Long id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Delivery> findActiveByDeliveryPersonId(Long deliveryPersonId) {
        return springRepo.findActiveByDeliveryPersonId(deliveryPersonId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateStatus(Long deliveryId, Delivery.DeliveryStatus status) {
        springRepo.findById(deliveryId).ifPresent(e -> {
            e.setStatus(status.name());
            springRepo.save(e);
        });
    }

    @Override
    public void updateStatusAndDeliveredAt(Long deliveryId, Delivery.DeliveryStatus status, LocalDateTime deliveredAt) {
        springRepo.findById(deliveryId).ifPresent(e -> {
            e.setStatus(status.name());
            e.setDeliveredAt(deliveredAt);
            springRepo.save(e);
        });
    }

    // ── Mappers ─────────────────────────────────────────────────────────

    private DeliveryEntity toEntity(Delivery d) {
        return DeliveryEntity.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .deliveryPersonId(d.getDeliveryPersonId())
                .status(d.getStatus() != null ? d.getStatus().name() : Delivery.DeliveryStatus.ASSIGNED.name())
                .assignedAt(d.getAssignedAt())
                .deliveredAt(d.getDeliveredAt())
                .build();
    }

    private Delivery toDomain(DeliveryEntity e) {
        return Delivery.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .deliveryPersonId(e.getDeliveryPersonId())
                .status(Delivery.DeliveryStatus.valueOf(e.getStatus()))
                .assignedAt(e.getAssignedAt())
                .deliveredAt(e.getDeliveredAt())
                .build();
    }
}