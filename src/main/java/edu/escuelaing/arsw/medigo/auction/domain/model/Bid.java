package edu.escuelaing.arsw.medigo.auction.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Bid {
    private Long          id;
    private Long          auctionId;
    private Long          userId;
    private String        userName;
    private BigDecimal    amount;
    private LocalDateTime placedAt;
}
