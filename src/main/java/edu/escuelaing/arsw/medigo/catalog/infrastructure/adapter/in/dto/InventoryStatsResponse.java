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
@Schema(name = "InventoryStatsResponse", description = "Métricas de inventario para dashboard administrativo")
public class InventoryStatsResponse {

    @Schema(example = "1248390.42")
    private BigDecimal totalInventoryValue;

    @Schema(example = "0")
    private Double deltaPct;

    @Schema(example = "1482")
    private Integer activeLots;

    @Schema(example = "true")
    private Boolean allVerified;

    @Schema(example = "250")
    private Integer totalMedications;

    @Schema(example = "7640")
    private Integer totalUnits;

    @Schema(example = "12")
    private Integer lowStockCount;

    @Schema(example = "4")
    private Integer outOfStockCount;
}
