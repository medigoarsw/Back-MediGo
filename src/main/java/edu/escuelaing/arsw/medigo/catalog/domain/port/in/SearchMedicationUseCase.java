package edu.escuelaing.arsw.medigo.catalog.domain.port.in;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import java.util.List;
public interface SearchMedicationUseCase {
    List<Medication> searchByName(String name);
    List<BranchStock> getStockByBranch(Long branchId);
}