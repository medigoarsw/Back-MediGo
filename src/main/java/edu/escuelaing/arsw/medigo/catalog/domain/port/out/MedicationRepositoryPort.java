package edu.escuelaing.arsw.medigo.catalog.domain.port.out;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import java.util.*;
public interface MedicationRepositoryPort {
    List<Medication> findByNameContaining(String name);
    Optional<Medication> findById(Long id);
    Medication save(Medication medication);
    List<BranchStock> findStockByBranch(Long branchId);
    void updateStock(Long branchId, Long medicationId, int quantity);
}