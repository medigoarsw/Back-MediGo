package edu.escuelaing.arsw.medigo.catalog.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que agrupa medicamentos por sucursal en la capa de dominio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchWithMedications {
    private Long branchId;
    private String branchName;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<StockWithMedicationInfo> medications;
}
