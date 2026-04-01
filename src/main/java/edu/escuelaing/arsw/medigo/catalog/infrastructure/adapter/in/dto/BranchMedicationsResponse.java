package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que agrupa medicamentos por sucursal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Medicamentos agrupados por sucursal")
public class BranchMedicationsResponse {

    @Schema(description = "ID de la sucursal", example = "1")
    private Long branchId;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro")
    private String branchName;

    @Schema(description = "Dirección de la sucursal", example = "Calle 10 # 5-20")
    private String address;

    @Schema(description = "Latitud de la sucursal", example = "4.72160")
    private Double latitude;

    @Schema(description = "Longitud de la sucursal", example = "-74.04499")
    private Double longitude;

    @Schema(description = "Lista de medicamentos disponibles en la sucursal")
    private List<MedicationBranchStockResponse> medications;
}
