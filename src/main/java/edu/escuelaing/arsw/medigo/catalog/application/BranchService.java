package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.CreateBranchUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateBranchUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.BranchRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que implementa los casos de uso para la gestión de sucursales.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService implements CreateBranchUseCase, UpdateBranchUseCase {

    private final BranchRepositoryPort branchRepositoryPort;

    @Override
    @Transactional
    public Branch createBranch(String name, String address, Double latitude, Double longitude) {
        log.info("Creando nueva sucursal: {}", name);
        
        Branch branch = Branch.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
                
        return branchRepositoryPort.save(branch);
    }

    @Override
    @Transactional
    public Branch updateBranch(Long id, String name, String address, Double latitude, Double longitude) {
        log.info("Actualizando sucursal con ID: {}", id);
        
        Branch existing = branchRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + id));
        
        existing.setName(name);
        existing.setAddress(address);
        existing.setLatitude(latitude);
        existing.setLongitude(longitude);
        
        return branchRepositoryPort.save(existing);
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        log.info("Eliminando sucursal con ID: {}", id);
        
        if (!branchRepositoryPort.existsById(id)) {
            throw new ResourceNotFoundException("Sucursal no encontrada con ID: " + id);
        }
        
        branchRepositoryPort.deleteById(id);
    }
}
