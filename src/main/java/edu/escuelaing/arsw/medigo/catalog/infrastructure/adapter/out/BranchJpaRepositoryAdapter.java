package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.BranchRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.BranchSpringDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto BranchRepositoryPort
 * usando Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BranchJpaRepositoryAdapter implements BranchRepositoryPort {

    private final BranchSpringDataRepository branchSpringDataRepository;

    @Override
    public Branch save(Branch branch) {
        log.debug("Guardando sucursal: {}", branch.getName());
        BranchEntity entity = toEntity(branch);
        BranchEntity saved = branchSpringDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Branch> findById(Long id) {
        return branchSpringDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Branch> findAll() {
        return branchSpringDataRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        branchSpringDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return branchSpringDataRepository.existsById(id);
    }

    private Branch toDomain(BranchEntity entity) {
        return Branch.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    private BranchEntity toEntity(Branch branch) {
        return BranchEntity.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .build();
    }
}
