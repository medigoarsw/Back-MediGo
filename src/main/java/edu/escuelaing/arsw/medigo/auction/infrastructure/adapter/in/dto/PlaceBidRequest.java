package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PlaceBidRequest(
    @NotNull Long      userId,
    @NotNull String    userName,
    @NotNull @Positive BigDecimal amount
) {}
