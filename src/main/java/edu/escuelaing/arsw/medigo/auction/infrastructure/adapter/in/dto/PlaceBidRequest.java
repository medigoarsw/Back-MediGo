package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PlaceBidRequest(
    @NotNull Long      userId,
    @NotNull @Pattern(regexp = "^[a-zA-Z0-9\\s]*$", message = "Nombre de usuario inválido") String userName,
    @NotNull @Positive BigDecimal amount
) {}
