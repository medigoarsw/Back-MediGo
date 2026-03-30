package edu.escuelaing.arsw.medigo.catalog.domain.port.in;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
public interface UpdateStockUseCase {
    Medication createMedication(Medication medication, Long branchId, int initialStock);
    void updateStock(Long branchId, Long medicationId, int quantity);
}