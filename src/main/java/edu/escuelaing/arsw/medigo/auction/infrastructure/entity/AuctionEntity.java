package edu.escuelaing.arsw.medigo.auction.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name = "auctions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuctionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "medication_id", nullable = false) private Long medicationId;
    @Column(name = "branch_id", nullable = false) private Long branchId;
    @Column(name = "base_price", nullable = false) private BigDecimal basePrice;
    @Column(name = "start_time", nullable = false) private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false) private LocalDateTime endTime;
    @Column(nullable = false) private String status;
    @Column(name = "winner_id") private Long winnerId;
}