package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class MedicationJpaRepository implements MedicationRepositoryPort {
    @Override public List<Medication> findByNameContaining(String name) { return List.of(); }
    @Override public Optional<Medication> findById(Long id) { return Optional.empty(); }
    @Override public Medication save(Medication medication) { return medication; }
    @Override public List<BranchStock> findStockByBranch(Long branchId) { return List.of(); }
    @Override public void updateStock(Long branchId, Long medicationId, int quantity) {}
}