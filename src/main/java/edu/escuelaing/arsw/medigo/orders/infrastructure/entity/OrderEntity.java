package edu.escuelaing.arsw.medigo.orders.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "affiliate_id", nullable = false) private Long affiliateId;
    @Column(name = "branch_id", nullable = false) private Long branchId;
    @Column(nullable = false) private String status;
    @Column(name = "address_lat") private Double addressLat;
    @Column(name = "address_lng") private Double addressLng;
    @Column(name = "created_at") private LocalDateTime createdAt;
}