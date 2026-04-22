package edu.escuelaing.arsw.medigo.logistics.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "deliveries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false, unique = true) private Long orderId;
    @Column(name = "delivery_person_id") private Long deliveryPersonId;
    @Column(nullable = false) private String status;
    @Column(name = "assigned_at") private LocalDateTime assignedAt;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
}