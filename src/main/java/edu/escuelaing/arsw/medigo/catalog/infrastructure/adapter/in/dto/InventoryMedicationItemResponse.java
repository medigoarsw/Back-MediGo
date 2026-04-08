package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "InventoryMedicationItemResponse", description = "Fila de inventario para administración")
public class InventoryMedicationItemResponse {

    @Schema(example = "1")
    private Long medicationId;

    @Schema(example = "Paracetamol 500mg")
    private String medicationName;

    @Schema(example = "Analgésico y antipirético")
    private String description;

    @Schema(example = "tableta")
    private String unit;

    @Schema(example = "5000.00")
    private BigDecimal unitPrice;

    @Schema(example = "120")
    private Integer quantity;

    @Schema(example = "true")
    private Boolean isAvailable;
}
