package edu.escuelaing.arsw.medigo.catalog.domain.port.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;

/**
 * Puerto de entrada para el caso de uso de actualización de sucursales/centros médicos.
 */
public interface UpdateBranchUseCase {
    Branch updateBranch(Long id, String name, String address, Double latitude, Double longitude);
    void deleteBranch(Long id);
}
