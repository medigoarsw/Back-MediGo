package edu.escuelaing.arsw.medigo.catalog.application;
import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.*;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class CatalogService implements SearchMedicationUseCase, UpdateStockUseCase {
    private final MedicationRepositoryPort medicationRepository;
    @Override public List<Medication> searchByName(String name) { throw new UnsupportedOperationException("TODO Alejandra"); }
    @Override public List<BranchStock> getStockByBranch(Long branchId) { throw new UnsupportedOperationException("TODO Alejandra"); }
    @Override public Medication createMedication(Medication medication, Long branchId, int initialStock) { throw new UnsupportedOperationException("TODO Alejandra"); }
    @Override public void updateStock(Long branchId, Long medicationId, int quantity) { throw new UnsupportedOperationException("TODO Alejandra"); }
}