package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateAuctionRequest(
    @NotNull @Positive BigDecimal basePrice,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "Fecha/hora local de inicio (sin zona, no usar sufijo Z)",
        type = "string",
        example = "2026-04-09T15:23:00"
    )
    @NotNull LocalDateTime startTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
        description = "Fecha/hora local de fin (sin zona, no usar sufijo Z)",
        type = "string",
        example = "2026-04-09T15:33:00"
    )
    @NotNull LocalDateTime endTime
) {}
