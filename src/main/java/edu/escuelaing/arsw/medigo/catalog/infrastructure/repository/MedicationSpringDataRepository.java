package edu.escuelaing.arsw.medigo.catalog.infrastructure.repository;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.MedicationEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationSpringDataRepository extends JpaRepository<MedicationEntity, Long> {

    /**
     * Búsqueda insensible a mayúsculas/minúsculas de medicamentos por nombre (LIKE)
     */
    @Query("SELECT m FROM MedicationEntity m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MedicationEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Buscar medicamento por nombre exacto
     */
    Optional<MedicationEntity> findByNameIgnoreCase(String name);

    /**
     * Verificar si existe un medicamento con ese nombre
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Obtener todas las sucursales para agregación de medicamentos
     */
    @Query("SELECT DISTINCT b FROM BranchEntity b")
    List<BranchEntity> findAllBranches();
}
