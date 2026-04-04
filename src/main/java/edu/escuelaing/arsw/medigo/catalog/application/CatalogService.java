package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.domain.model.*;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.BranchWithMedications;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.*;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación que implementa los casos de uso del módulo de Catálogo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService implements SearchMedicationUseCase, UpdateStockUseCase, CreateMedicationUseCase {

    private final MedicationRepositoryPort medicationRepository;

    /**
     * Busca medicamentos por nombre (búsqueda parcial, insensible a mayúsculas)
     * @param name término de búsqueda
     * @return lista de medicamentos encontrados
     */
    @Override
    @Transactional(readOnly = true)
    public List<Medication> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("El término de búsqueda no puede estar vacío");
        }

        log.info("Buscando medicamentos con nombre que contenga: {}", name);
        List<Medication> results = medicationRepository.findByNameContaining(name.trim());

        log.info("Se encontraron {} medicamentos", results.size());
        return results;
    }

    /**
     * Obtiene toda la disponibilidad de medicamentos en una sucursal específica
     * @param branchId ID de la sucursal
     * @return lista de stocks disponibles en la sucursal
     */
    @Override
    @Transactional(readOnly = true)
    public List<BranchStock> getStockByBranch(Long branchId) {
        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        log.info("Obteniendo stock para sucursal: {}", branchId);
        List<BranchStock> stocks = medicationRepository.findStockByBranch(branchId);

        log.info("Se encontraron {} medicamentos en sucursal {}", stocks.size(), branchId);
        return stocks;
    }

    /**
     * Crea un nuevo medicamento en el catálogo con stock inicial en una sucursal
     * @param medication datos del medicamento
     * @param branchId ID de la sucursal donde se crea el stock inicial
     * @param initialStock cantidad inicial de stock
     * @return medicamento creado
     */
    @Override
    @Transactional
    public Medication createMedication(Medication medication, Long branchId, int initialStock) {
        // Validaciones
        validateMedicationData(medication);

        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        if (initialStock < 0) {
            throw new BusinessException("El stock inicial no puede ser negativo");
        }

        log.info("Creando medicamento: {} en sucursal: {} con stock inicial: {}",
                medication.getName(), branchId, initialStock);

        // Guardar el medicamento
        Medication savedMedication = medicationRepository.save(medication);

        // Crear el stock inicial si es > 0
        if (initialStock > 0) {
            BranchStock branchStock = BranchStock.builder()
                    .branchId(branchId)
                    .medicationId(savedMedication.getId())
                    .quantity(initialStock)
                    .build();

            medicationRepository.updateStock(branchId, savedMedication.getId(), initialStock);
        }

        log.info("Medicamento creado exitosamente: {}", savedMedication.getId());
        return savedMedication;
    }

    /**
     * Actualiza la disponibilidad de un medicamento en una sucursal
     * @param branchId ID de la sucursal
     * @param medicationId ID del medicamento
     * @param quantity nueva cantidad (no puede ser negativa)
     */
    @Override
    @Transactional
    public void updateStock(Long branchId, Long medicationId, int quantity) {
        // Validaciones
        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        if (medicationId == null || medicationId <= 0) {
            throw new BusinessException("El ID del medicamento debe ser válido");
        }

        if (quantity < 0) {
            throw new BusinessException("La cantidad no puede ser negativa");
        }

        log.info("Actualizando stock - Sucursal: {}, Medicamento: {}, Nueva cantidad: {}",
                branchId, medicationId, quantity);

        // Verificar que el medicamento existe
        medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicamento no encontrado con ID: " + medicationId));

        // Actualizar el stock
        medicationRepository.updateStock(branchId, medicationId, quantity);

        log.info("Stock actualizado exitosamente");
    }

    /**
     * Obtiene todos los medicamentos disponibles en una sucursal específica
     * @param branchId ID de la sucursal
     * @return lista de medicamentos con sus stocks en esa sucursal
     */
    @Transactional(readOnly = true)
    public List<StockWithMedicationInfo> getMedicationsByBranch(Long branchId) {
        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        log.info("Obteniendo medicamentos para sucursal: {}", branchId);
        List<StockWithMedicationInfo> medications = medicationRepository.findMedicationsByBranch(branchId);

        log.info("Se encontraron {} medicamentos en sucursal {}", medications.size(), branchId);
        return medications;
    }

    /**
     * Obtiene todos los medicamentos agrupados por sucursal
     * Útil para dashboards y reportes
     * @return lista de sucursales con sus medicamentos disponibles
     */
    @Transactional(readOnly = true)
    public List<BranchWithMedications> getAllMedicationsByBranches() {
        log.info("Obteniendo medicamentos agrupados por todas las sucursales");
        List<BranchWithMedications> result = medicationRepository.findAllBranchesWithMedications();

        log.info("Se encontraron {} sucursales con medicamentos", result.size());
        return result;
    }

    /**
     * Obtiene un medicamento por ID
     * @param id ID del medicamento
     * @return el medicamento si existe
     */
    @Transactional(readOnly = true)
    public Optional<Medication> findById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("El ID del medicamento debe ser válido");
        }

        log.debug("Buscando medicamento con ID: {}", id);
        return medicationRepository.findById(id);
    }

    /**
     * Obtiene la disponibilidad de un medicamento en una sucursal específica (HU-04)
     * @param medicationId ID del medicamento
     * @param branchId ID de la sucursal
     * @return información de disponibilidad del medicamento en esa sucursal
     */
    @Transactional(readOnly = true)
    public BranchStock getAvailabilityByMedicationBranch(Long medicationId, Long branchId) {
        if (medicationId == null || medicationId <= 0) {
            throw new BusinessException("El ID del medicamento debe ser válido");
        }

        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        log.info("Obteniendo disponibilidad del medicamento {} en sucursal {}", medicationId, branchId);

        // Verificar que el medicamento existe
        medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicamento no encontrado con ID: " + medicationId));

        // Obtener el stock del medicamento en la sucursal
        BranchStock stock = medicationRepository.findStockByMedicationAndBranch(medicationId, branchId);

        if (stock == null) {
            // Si no hay registro, retornar con cantidad 0 (no disponible)
            stock = BranchStock.builder()
                    .medicationId(medicationId)
                    .branchId(branchId)
                    .quantity(0)
                    .build();
        }

        log.info("Disponibilidad obtenida: {} unidades", stock.getQuantity());
        return stock;
    }

    /**
     * Obtiene la disponibilidad de un medicamento en todas las sucursales (HU-04)
     * @param medicationId ID del medicamento
     * @return información de disponibilidad del medicamento en todas las sucursales
     */
    @Transactional(readOnly = true)
    public List<BranchStock> getAvailabilityByMedicationAllBranches(Long medicationId) {
        if (medicationId == null || medicationId <= 0) {
            throw new BusinessException("El ID del medicamento debe ser válido");
        }

        log.info("Obteniendo disponibilidad del medicamento {} en todas las sucursales", medicationId);

        // Verificar que el medicamento existe
        medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medicamento no encontrado con ID: " + medicationId));

        // Obtener el stock del medicamento en todas las sucursales
        List<BranchStock> stocks = medicationRepository.findStockByMedication(medicationId);

        log.info("Se encontraron {} sucursales con stock para medicamento {}", stocks.size(), medicationId);
        return stocks;
    }

    /**
     * Valida los datos básicos de un medicamento
     */
    private void validateMedicationData(Medication medication) {
        if (medication == null) {
            throw new BusinessException("Los datos del medicamento son requeridos");
        }

        if (medication.getName() == null || medication.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre del medicamento es requerido");
        }

        if (medication.getUnit() == null || medication.getUnit().trim().isEmpty()) {
            throw new BusinessException("La unidad del medicamento es requerida");
        }

        if (medication.getPrice() == null) {
            throw new BusinessException("El precio del medicamento es requerido");
        }

        if (medication.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor a 0");
        }

        if (medication.getName().length() > 255) {
            throw new BusinessException("El nombre del medicamento no puede exceder 255 caracteres");
        }
    }

    /**
     * HU-07: Implementa CreateMedicationUseCase con parámetros individuales
     * Crea un nuevo medicamento en el catálogo con stock inicial en una sucursal
     * @param name nombre del medicamento (obligatorio)
     * @param description descripción (opcional)
     * @param unit presentación/unidad (obligatorio)
     * @param price precio del medicamento (debe ser > 0)
     * @param branchId sucursal donde se crea el stock inicial
     * @param initialStock cantidad inicial de stock (debe ser > 0)
     * @return medicamento creado con ID asignado
     */
    @Override
    @Transactional
    public Medication createMedication(
            String name,
            String description,
            String unit,
            BigDecimal price,
            Long branchId,
            Integer initialStock) {

        // Validar parámetros individualmente
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("El nombre es obligatorio");
        }

        if (unit == null || unit.trim().isEmpty()) {
            throw new BusinessException("La presentación es obligatoria");
        }

        if (price == null) {
            throw new BusinessException("El precio es requerido");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor a 0");
        }

        if (branchId == null || branchId <= 0) {
            throw new BusinessException("El ID de la sucursal debe ser válido");
        }

        if (initialStock == null || initialStock <= 0) {
            throw new BusinessException("El stock inicial debe ser mayor a 0");
        }

        if (name.length() > 255) {
            throw new BusinessException("El nombre del medicamento no puede exceder 255 caracteres");
        }

        log.info("HU-07: Creando medicamento: {} en sucursal: {} con stock inicial: {}",
                name, branchId, initialStock);

        // Crear el objeto Medication
        Medication medication = Medication.builder()
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .unit(unit.trim())
                .price(price)
                .build();

        // Guardar el medicamento
        Medication savedMedication = medicationRepository.save(medication);

        // Crear el stock inicial en la sucursal
        medicationRepository.updateStock(branchId, savedMedication.getId(), initialStock);

        log.info("HU-07: Medicamento creado exitosamente con ID: {} y número de orden: ORD-{}",
                savedMedication.getId(), savedMedication.getId());

        return savedMedication;
    }
}