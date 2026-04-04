package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar la actualización de stock
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "UpdateStockRequest",
    description = "Solicitud para actualizar la disponibilidad de un medicamento en una sucursal"
)
public class UpdateStockRequest {

    @NotNull(message = "El ID de la medicación es requerido")
    @Schema(
        description = "ID del medicamento a actualizar",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long medicationId;

    @NotNull(message = "La cantidad es requerida")
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    @Schema(
        description = "Nueva cantidad en stock (no puede ser negativa)",
        example = "50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer quantity;
}
