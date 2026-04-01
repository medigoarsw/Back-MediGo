package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa un medicamento con su información de stock en una sucursal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Medicamento con información de stock en sucursal")
public class MedicationBranchStockResponse {

    @Schema(description = "ID del medicamento", example = "1")
    private Long medicationId;

    @Schema(description = "Nombre del medicamento", example = "Ibuprofeno 400mg")
    private String medicationName;

    @Schema(description = "Descripción del medicamento", example = "Antiinflamatorio y analgésico")
    private String description;

    @Schema(description = "Unidad de venta", example = "Caja x30")
    private String unit;

    @Schema(description = "Cantidad disponible en la sucursal", example = "150")
    private Integer quantity;
}
