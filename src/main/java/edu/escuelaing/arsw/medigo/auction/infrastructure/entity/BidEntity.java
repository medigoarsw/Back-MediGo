package edu.escuelaing.arsw.medigo.auction.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;
}
