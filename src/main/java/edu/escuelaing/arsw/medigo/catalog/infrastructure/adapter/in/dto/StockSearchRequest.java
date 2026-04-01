package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar la búsqueda de disponibilidad por medicamento y sucursal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "StockSearchRequest",
    description = "Solicitud para buscar disponibilidad de un medicamento en una sucursal"
)
public class StockSearchRequest {

    @NotNull(message = "El ID del medicamento es requerido")
    @Positive(message = "El ID del medicamento debe ser positivo")
    @Schema(
        description = "ID del medicamento",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long medicationId;

    @NotNull(message = "El ID de la sucursal es requerido")
    @Positive(message = "El ID de la sucursal debe ser positivo")
    @Schema(
        description = "ID de la sucursal",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long branchId;
}
