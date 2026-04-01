package edu.escuelaing.arsw.medigo.catalog.domain.port.in;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.*;
import java.util.List;
public interface SearchMedicationUseCase {
    List<Medication> searchByName(String name);
    List<BranchStock> getStockByBranch(Long branchId);
    List<StockWithMedicationInfo> getMedicationsByBranch(Long branchId);
    List<BranchWithMedications> getAllMedicationsByBranches();
}