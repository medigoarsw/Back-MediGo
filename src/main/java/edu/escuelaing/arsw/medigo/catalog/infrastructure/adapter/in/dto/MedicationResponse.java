package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO de respuesta para un medicamento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "MedicationResponse",
    description = "Información de un medicamento en el catálogo"
)
public class MedicationResponse {

    @Schema(
        description = "ID único del medicamento",
        example = "1"
    )
    private Long id;

    @Schema(
        description = "Nombre del medicamento",
        example = "Paracetamol 500mg"
    )
    private String name;

    @Schema(
        description = "Descripción detallada del medicamento",
        example = "Analgésico y antipirético para dolores leves a moderados"
    )
    private String description;

    @Schema(
        description = "Unidad de medida del medicamento",
        example = "tableta"
    )
    private String unit;

    @Schema(
        description = "Precio del medicamento",
        example = "5000.00"
    )
    private BigDecimal price;  // HU-07: Agregar precio
}
