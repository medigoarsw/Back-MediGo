package edu.escuelaing.arsw.medigo.logistics.infrastructure.entity;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "delivery_person_id")
    private Long deliveryPersonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Delivery.DeliveryStatus status;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    public Delivery toDomain() {
        return Delivery.builder()
                .id(this.id)
                .orderId(this.orderId)
                .deliveryPersonId(this.deliveryPersonId)
                .status(this.status)
                .assignedAt(this.assignedAt)
                .build();
    }

    public static DeliveryEntity fromDomain(Delivery delivery) {
        return DeliveryEntity.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt() != null ? delivery.getAssignedAt() : LocalDateTime.now())
                .build();
    }
}
