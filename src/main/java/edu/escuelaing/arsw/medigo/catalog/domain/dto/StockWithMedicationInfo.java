package edu.escuelaing.arsw.medigo.catalog.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de dominio que representa el stock con información enriquecida del medicamento
 * Útil para consultas que necesitan datos del medicamento junto con su disponibilidad
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockWithMedicationInfo {
    private Long medicationId;
    private String medicationName;
    private String description;
    private String medicationUnit;
    private Long branchId;
    private Integer quantity;

    public boolean isAvailable() {
        return quantity != null && quantity > 0;
    }
}
