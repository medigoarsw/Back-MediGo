package edu.escuelaing.arsw.medigo.catalog.domain.port.in;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.*;
import java.util.List;
import java.util.Optional;
public interface SearchMedicationUseCase {
    List<Medication> searchByName(String name);
    List<BranchStock> getStockByBranch(Long branchId);
    List<StockWithMedicationInfo> getMedicationsByBranch(Long branchId);
    List<BranchWithMedications> getAllMedicationsByBranches();
    
    // HU-04: Disponibilidad en tiempo real
    BranchStock getAvailabilityByMedicationBranch(Long medicationId, Long branchId);
    List<BranchStock> getAvailabilityByMedicationAllBranches(Long medicationId);
    Optional<Medication> findById(Long medicationId);
}