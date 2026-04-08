package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.BranchWithMedications;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.InventoryMedicationAggregate;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.MedicationEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchStockEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.MedicationSpringDataRepository;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.BranchStockSpringDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto MedicationRepositoryPort
 * usando Spring Data JPA
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationJpaRepository implements MedicationRepositoryPort {

    private final MedicationSpringDataRepository medicationSpringDataRepository;
    private final BranchStockSpringDataRepository branchStockSpringDataRepository;

    /**
     * Busca medicamentos cuyo nombre contenga el texto especificado (insensible a mayúsculas)
     */
    @Override
    public List<Medication> findByNameContaining(String name) {
        log.debug("Buscando medicamentos con nombre que contenga: {}", name);

        return medicationSpringDataRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toMedicationDomain)
                .toList();
    }

    /**
     * Busca un medicamento por ID
     */
    @Override
    public Optional<Medication> findById(Long id) {
        log.debug("Buscando medicamento con ID: {}", id);

        return medicationSpringDataRepository
                .findById(id)
                .map(this::toMedicationDomain);
    }

    /**
     * Guarda un nuevo medicamento en la base de datos
     */
    @Override
    public Medication save(Medication medication) {
        log.debug("Guardando medicamento: {}", medication.getName());

        MedicationEntity entity = toMedicationEntity(medication);
        MedicationEntity saved = medicationSpringDataRepository.save(entity);

        log.debug("Medicamento guardado con ID: {}", saved.getId());
        return toMedicationDomain(saved);
    }

    /**
     * Obtiene todos los stocks disponibles en una sucursal específica
     */
    @Override
    public List<BranchStock> findStockByBranch(Long branchId) {
        log.debug("Obteniendo stocks para sucursal: {}", branchId);

        return branchStockSpringDataRepository
                .findByBranchId(branchId)
                .stream()
                .map(this::toBranchStockDomain)
                .toList();
    }

    /**
     * Obtiene stocks con información enriquecida del medicamento (para presentación en REST)
     * Este método es utilizado por el controlador para devolver respuestas completas
     */
    public List<StockWithMedicationInfo> findStockByBranchWithMedicationInfo(Long branchId) {
        log.debug("Obteniendo stocks enriquecidos para sucursal: {}", branchId);
        return branchStockSpringDataRepository.findStockByBranchWithMedicationInfo(branchId);
    }

    /**
     * Actualiza la disponibilidad de un medicamento en una sucursal
     * Crea el registro si no existe, o actualiza si ya existe
     */
    @Override
    public void updateStock(Long branchId, Long medicationId, int quantity) {
        log.debug("Actualizando stock - Sucursal: {}, Medicamento: {}, Cantidad: {}",
                branchId, medicationId, quantity);

        Optional<BranchStockEntity> existing = branchStockSpringDataRepository
                .findByBranchIdAndMedicationId(branchId, medicationId);

        BranchStockEntity stockEntity;

        if (existing.isPresent()) {
            // Actualizar existente
            stockEntity = existing.get();
            stockEntity.setQuantity(quantity);
        } else {
            // Crear nuevo
            stockEntity = BranchStockEntity.builder()
                    .branchId(branchId)
                    .medicationId(medicationId)
                    .quantity(quantity)
                    .build();
        }

        branchStockSpringDataRepository.save(stockEntity);
        log.debug("Stock actualizado exitosamente");
    }

    /**
     * Mapea una entidad JPA de Medicamento al modelo de dominio
     */
    private Medication toMedicationDomain(MedicationEntity entity) {
        return Medication.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .unit(entity.getUnit())
                .build();
    }

    /**
     * Mapea un modelo de dominio de Medicamento a entidad JPA
     */
    private MedicationEntity toMedicationEntity(Medication medication) {
        return MedicationEntity.builder()
                .id(medication.getId())
                .name(medication.getName())
                .description(medication.getDescription())
                .unit(medication.getUnit())
                .build();
    }

    /**
     * Mapea una entidad JPA de BranchStock al modelo de dominio
     */
    private BranchStock toBranchStockDomain(BranchStockEntity entity) {
        return BranchStock.builder()
                .branchId(entity.getBranchId())
                .medicationId(entity.getMedicationId())
                .quantity(entity.getQuantity())
                .build();
    }

    /**
     * Obtiene medicamentos disponibles en una sucursal específica con su información enriquecida
     */
    @Override
    public List<StockWithMedicationInfo> findMedicationsByBranch(Long branchId) {
        log.debug("Obteniendo medicamentos para sucursal: {}", branchId);
        return branchStockSpringDataRepository.findStockByBranchWithMedicationInfo(branchId);
    }

    /**
     * Obtiene todas las sucursales con sus medicamentos disponibles
     * Útil para dashboards que muestren disponibilidad por sucursal
     */
    @Override
    public List<BranchWithMedications> findAllBranchesWithMedications() {
        log.debug("Obteniendo todas las sucursales con medicamentos");
        
        return medicationSpringDataRepository.findAllBranches()
                .stream()
                .map(branchEntity -> {
                    List<StockWithMedicationInfo> medications = 
                        branchStockSpringDataRepository.findStockByBranchWithMedicationInfo(branchEntity.getId());
                    
                    return BranchWithMedications.builder()
                            .branchId(branchEntity.getId())
                            .branchName(branchEntity.getName())
                            .address(branchEntity.getAddress())
                            .latitude(branchEntity.getLatitude())
                            .longitude(branchEntity.getLongitude())
                            .medications(medications)
                            .build();
                })
                .toList();
    }

    /**
     * Obtiene el stock de un medicamento en una sucursal específica (HU-04)
     * @param medicationId ID del medicamento
     * @param branchId ID de la sucursal
     * @return BranchStock con quantity 0 si no existe
     */
    @Override
    public BranchStock findStockByMedicationAndBranch(Long medicationId, Long branchId) {
        log.debug("Buscando stock del medicamento {} en sucursal {}", medicationId, branchId);
        
        Optional<BranchStockEntity> entity = branchStockSpringDataRepository
                .findByBranchIdAndMedicationId(branchId, medicationId);
        
        if (entity.isPresent()) {
            return toBranchStockDomain(entity.get());
        } else {
            // Retornar con cantidad 0 si no existe el registro
            return BranchStock.builder()
                    .branchId(branchId)
                    .medicationId(medicationId)
                    .quantity(0)
                    .build();
        }
    }

    /**
     * Obtiene todos los stocks de un medicamento en todas las sucursales (HU-04)
     * @param medicationId ID del medicamento
     * @return Lista de BranchStock para ese medicamento en todas las sucursales
     */
    @Override
    public List<BranchStock> findStockByMedication(Long medicationId) {
        log.debug("Buscando stock del medicamento {} en todas las sucursales", medicationId);
        
        return branchStockSpringDataRepository
                .findByMedicationId(medicationId)
                .stream()
                .map(this::toBranchStockDomain)
                .toList();
    }

    /**
     * Obtiene stocks enriquecidos con información de sucursal para un medicamento (HU-04)
     * @param medicationId ID del medicamento
     * @return Lista de StockWithMedicationInfo con información de sucursales
     */
    @Override
    public List<StockWithMedicationInfo> findStockByMedicationWithBranchInfo(Long medicationId) {
        log.debug("Buscando stock enriquecido del medicamento {} con información de sucursal", medicationId);
        
        return branchStockSpringDataRepository.findStockByMedicationWithBranchInfo(medicationId);
    }

    /**
     * Obtiene inventario agregado para administración.
     */
    public List<InventoryMedicationAggregate> findInventoryAggregate(Long branchId, String q) {
        String safeQuery = q == null ? "" : q.trim();

        if (branchId != null) {
            return branchStockSpringDataRepository.findInventoryAggregateByBranch(branchId, safeQuery);
        }

        return branchStockSpringDataRepository.findInventoryAggregateAllBranches(safeQuery);
    }
}