package edu.escuelaing.arsw.medigo.catalog.domain.port.out;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de sucursales/centros médicos.
 */
public interface BranchRepositoryPort {
    Branch save(Branch branch);
    Optional<Branch> findById(Long id);
    List<Branch> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
}
