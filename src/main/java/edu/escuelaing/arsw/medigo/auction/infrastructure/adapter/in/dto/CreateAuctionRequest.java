package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionRequest(
    @NotNull Long          medicationId,
    @NotNull Long          branchId,
    @NotNull @Positive BigDecimal basePrice,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    String         closureType,    // FIXED_TIME | INACTIVITY | MAX_PRICE
    BigDecimal     maxPrice,
    Integer        inactivityMinutes
) {}
