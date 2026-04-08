package edu.escuelaing.arsw.medigo.catalog.infrastructure.repository;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchSpringDataRepository extends JpaRepository<BranchEntity, Long>, JpaSpecificationExecutor<BranchEntity> {

    /**
     * Verificar si una sucursal existe
     */
    boolean existsById(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByNameIgnoreCaseAndActiveTrueAndIdNot(String name, Long id);
}
