package edu.escuelaing.arsw.medigo.catalog.infrastructure.repository;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchStockEntity;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.InventoryMedicationAggregate;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchStockSpringDataRepository extends JpaRepository<BranchStockEntity, Long> {

    /**
     * Obtener stocks de una sucursal
     */
    List<BranchStockEntity> findByBranchId(Long branchId);

    /**
     * Obtener stock de un medicamento en una sucursal
     */
    Optional<BranchStockEntity> findByBranchIdAndMedicationId(Long branchId, Long medicationId);

    /**
     * Obtener todos los stocks de un medicamento
     */
    List<BranchStockEntity> findByMedicationId(Long medicationId);

    /**
     * Verificar disponibilidad: stock > 0
     */
    @Query("SELECT bs FROM BranchStockEntity bs WHERE bs.branchId = :branchId AND bs.medicationId = :medicationId AND bs.quantity > 0")
    Optional<BranchStockEntity> findAvailableStock(@Param("branchId") Long branchId, @Param("medicationId") Long medicationId);

    /**
     * Obtener stocks con información enriquecida del medicamento
     */
    @Query("""
            SELECT new edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo(
                bs.medicationId, 
                m.name, 
                m.description,
                m.unit, 
                bs.branchId, 
                bs.quantity
            )
            FROM BranchStockEntity bs
            JOIN MedicationEntity m ON bs.medicationId = m.id
            WHERE bs.branchId = :branchId
            ORDER BY m.name ASC
            """)
    List<StockWithMedicationInfo> findStockByBranchWithMedicationInfo(@Param("branchId") Long branchId);

    /**
     * Obtener stocks de un medicamento en todas las sucursales con información enriquecida (HU-04)
     */
    @Query("""
            SELECT new edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo(
                bs.medicationId, 
                m.name, 
                m.description,
                m.unit, 
                bs.branchId, 
                bs.quantity
            )
            FROM BranchStockEntity bs
            JOIN MedicationEntity m ON bs.medicationId = m.id
            WHERE bs.medicationId = :medicationId
            ORDER BY bs.branchId ASC
            """)
    List<StockWithMedicationInfo> findStockByMedicationWithBranchInfo(@Param("medicationId") Long medicationId);

    /**
     * Eliminar stock específico
     */
    void deleteByBranchIdAndMedicationId(Long branchId, Long medicationId);

    /**
     * Inventario agregado para todas las sucursales (MVP admin inventory page).
     */
    @Query("""
            SELECT new edu.escuelaing.arsw.medigo.catalog.domain.dto.InventoryMedicationAggregate(
                m.id,
                m.name,
                m.description,
                m.unit,
                m.price,
                CAST(COALESCE(SUM(bs.quantity), 0) AS integer)
            )
            FROM MedicationEntity m
            LEFT JOIN BranchStockEntity bs ON bs.medicationId = m.id
            WHERE (:q IS NULL OR :q = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')))
            GROUP BY m.id, m.name, m.description, m.unit, m.price
            ORDER BY m.name ASC
            """)
    List<InventoryMedicationAggregate> findInventoryAggregateAllBranches(@Param("q") String q);

    /**
     * Inventario agregado para una sucursal específica.
     */
    @Query("""
            SELECT new edu.escuelaing.arsw.medigo.catalog.domain.dto.InventoryMedicationAggregate(
                m.id,
                m.name,
                m.description,
                m.unit,
                m.price,
                CAST(COALESCE(bs.quantity, 0) AS integer)
            )
            FROM MedicationEntity m
            LEFT JOIN BranchStockEntity bs ON bs.medicationId = m.id AND bs.branchId = :branchId
            WHERE (:q IS NULL OR :q = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY m.name ASC
            """)
    List<InventoryMedicationAggregate> findInventoryAggregateByBranch(@Param("branchId") Long branchId,
                                                                      @Param("q") String q);
}

