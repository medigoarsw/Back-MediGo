package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para representar la disponibilidad de un medicamento en múltiples sucursales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "MedicationAvailabilityResponse",
    description = "Disponibilidad de un medicamento en todas las sucursales"
)
public class MedicationAvailabilityResponse {

    @Schema(
        description = "ID del medicamento",
        example = "5"
    )
    private Long medicationId;

    @Schema(
        description = "Nombre del medicamento",
        example = "Paracetamol 500mg"
    )
    private String medicationName;

    @Schema(
        description = "Descripción del medicamento",
        example = "Analgésico y antipirético"
    )
    private String description;

    @Schema(
        description = "Unidad de medida",
        example = "tableta"
    )
    private String unit;

    @Schema(
        description = "Disponibilidad en todas las sucursales"
    )
    private List<BranchAvailabilityResponse> availabilityByBranch;

    @Schema(
        description = "Cantidad total de unidades disponibles en todas las sucursales",
        example = "25"
    )
    private Integer totalAvailable;

    @Schema(
        description = "Cantidad de sucursales donde el medicamento está disponible",
        example = "3"
    )
    private Integer branchesWithStock;
}
