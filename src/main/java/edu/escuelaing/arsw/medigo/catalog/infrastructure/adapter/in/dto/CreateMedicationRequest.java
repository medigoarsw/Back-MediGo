package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar la creación de un medicamento con stock inicial
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "CreateMedicationRequest",
    description = "Solicitud para crear un medicamento con stock inicial en una sucursal"
)
public class CreateMedicationRequest {

    @NotBlank(message = "El nombre del medicamento es requerido")
    @Schema(
        description = "Nombre del medicamento",
        example = "Paracetamol 500mg",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Descripción del medicamento",
        example = "Analgésico y antipirético para dolores leves a moderados"
    )
    private String description;

    @NotBlank(message = "La unidad es requerida")
    @Schema(
        description = "Unidad del medicamento (tableta, cápsula, ml, etc)",
        example = "tableta",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String unit;

    @NotNull(message = "El ID de la sucursal es requerido")
    @Positive(message = "El ID de la sucursal debe ser positivo")
    @Schema(
        description = "ID de la sucursal donde se cuenta el stock inicial",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long branchId;

    @NotNull(message = "El stock inicial es requerido")
    @Positive(message = "El stock inicial debe ser positivo")
    @Schema(
        description = "Cantidad inicial de stock",
        example = "100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer initialStock;
}
