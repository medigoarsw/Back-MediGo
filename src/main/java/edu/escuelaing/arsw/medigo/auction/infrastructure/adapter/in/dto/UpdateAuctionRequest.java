package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateAuctionRequest(
    @NotNull @Positive BigDecimal basePrice,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime
) {}
