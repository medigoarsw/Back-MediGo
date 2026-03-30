package edu.escuelaing.arsw.medigo.auction.domain.model;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter @Builder @AllArgsConstructor
public class Auction {
    private Long id;
    private Long medicationId;
    private Long branchId;
    private BigDecimal basePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private Long winnerId;
    public enum AuctionStatus { SCHEDULED, ACTIVE, CLOSED, CANCELLED }
}