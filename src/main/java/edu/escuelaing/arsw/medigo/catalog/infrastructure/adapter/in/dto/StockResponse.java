package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para disponibilidad de stock en una sucursal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "StockResponse",
    description = "Información de disponibilidad de medicamento en una sucursal"
)
public class StockResponse {

    @Schema(
        description = "ID del medicamento",
        example = "1"
    )
    private Long medicationId;

    @Schema(
        description = "Nombre del medicamento",
        example = "Paracetamol 500mg"
    )
    private String medicationName;

    @Schema(
        description = "ID de la sucursal",
        example = "5"
    )
    private Long branchId;

    @Schema(
        description = "Cantidad disponible en stock",
        example = "35"
    )
    private Integer quantity;

    @Schema(
        description = "Indica si el medicamento está disponible (cantidad > 0)",
        example = "true"
    )
    private Boolean isAvailable;

    @Schema(
        description = "Unidad del medicamento",
        example = "tableta"
    )
    private String unit;
}
