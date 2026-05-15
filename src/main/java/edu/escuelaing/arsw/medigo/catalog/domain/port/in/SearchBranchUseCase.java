package edu.escuelaing.arsw.medigo.catalog.domain.port.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;
import java.util.Optional;

public interface SearchBranchUseCase {
    Optional<Branch> findBranchById(Long branchId);
}
