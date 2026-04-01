package edu.escuelaing.arsw.medigo.catalog.infrastructure.repository;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchSpringDataRepository extends JpaRepository<BranchEntity, Long> {

    /**
     * Verificar si una sucursal existe
     */
    boolean existsById(Long id);
}
