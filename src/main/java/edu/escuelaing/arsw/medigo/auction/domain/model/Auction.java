package edu.escuelaing.arsw.medigo.auction.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Auction {

    private Long          id;
    private Long          medicationId;
    private Long          branchId;
    private BigDecimal    basePrice;
    private BigDecimal    maxPrice;          // opcional: cierre por monto maximo
    private Integer       inactivityMinutes; // opcional: cierre por inactividad
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastBidAt;         // para calcular inactividad
    private AuctionStatus status;
    private Long          winnerId;
    private ClosureType   closureType;

    public enum AuctionStatus  { SCHEDULED, ACTIVE, CLOSED, CANCELLED }
    public enum ClosureType    { FIXED_TIME, INACTIVITY, MAX_PRICE }

    // ── Reglas de dominio puras ─────────────────────────────────

    public boolean isEditable() {
        return this.status == AuctionStatus.SCHEDULED;
    }

    public boolean isAcceptingBids() {
        return this.status == AuctionStatus.ACTIVE
            && LocalDateTime.now().isBefore(this.endTime);
    }

    public boolean shouldCloseByMaxPrice(BigDecimal bidAmount) {
        return this.closureType == ClosureType.MAX_PRICE
            && this.maxPrice != null
            && bidAmount.compareTo(this.maxPrice) >= 0;
    }

    public boolean shouldCloseByInactivity() {
        if (this.closureType != ClosureType.INACTIVITY
                || this.inactivityMinutes == null) return false;
        LocalDateTime ref = this.lastBidAt != null ? this.lastBidAt : this.startTime;
        return LocalDateTime.now().isAfter(ref.plusMinutes(this.inactivityMinutes));
    }
}
