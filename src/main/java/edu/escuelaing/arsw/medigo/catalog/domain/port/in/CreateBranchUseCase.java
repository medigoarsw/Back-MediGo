package edu.escuelaing.arsw.medigo.catalog.domain.port.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;

/**
 * Puerto de entrada para el caso de uso de creación de sucursales/centros médicos.
 */
public interface CreateBranchUseCase {
    Branch createBranch(String name, String address, Double latitude, Double longitude);
}
