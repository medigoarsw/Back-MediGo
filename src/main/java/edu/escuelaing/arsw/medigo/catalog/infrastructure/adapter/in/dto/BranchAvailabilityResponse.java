package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar la disponibilidad de un medicamento en una sucursal específica
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "BranchAvailabilityResponse",
    description = "Disponibilidad de un medicamento en una sucursal"
)
public class BranchAvailabilityResponse {

    @Schema(
        description = "ID de la sucursal",
        example = "1"
    )
    private Long branchId;

    @Schema(
        description = "Nombre de la sucursal",
        example = "Sucursal Centro"
    )
    private String branchName;

    @Schema(
        description = "Dirección de la sucursal",
        example = "Calle 10 # 5-20"
    )
    private String address;

    @Schema(
        description = "Latitud de la sucursal",
        example = "4.72160"
    )
    private Double latitude;

    @Schema(
        description = "Longitud de la sucursal",
        example = "-74.04499"
    )
    private Double longitude;

    @Schema(
        description = "Cantidad disponible en stock",
        example = "5"
    )
    private Integer quantity;

    @Schema(
        description = "Indica si el medicamento está disponible (cantidad > 0)",
        example = "true"
    )
    private Boolean isAvailable;

    @Schema(
        description = "Mensaje descriptivo del estado de disponibilidad",
        example = "Disponible"
    )
    private String availabilityStatus;
}
