package edu.escuelaing.arsw.medigo.catalog.domain.port.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import java.math.BigDecimal;

/**
 * UseCase para crear medicamentos en el catálogo (HU-07)
 * 
 * Solo administradores pueden crear medicamentos.
 * El medicamento se crea con precio y presentación obligatorios.
 * Se crea el stock inicial en la sucursal especificada.
 */
public interface CreateMedicationUseCase {
    
    /**
     * Crea un nuevo medicamento en el catálogo
     * @param name nombre del medicamento (obligatorio)
     * @param description descripción (opcional)
     * @param unit presentación/unidad (obligatorio)
     * @param price precio del medicamento (debe ser > 0)
     * @param branchId sucursal donde se crea el stock inicial
     * @param initialStock cantidad inicial de stock (debe ser > 0)
     * @return medicamento creado con ID asignado
     * @throws BusinessException si falta nombre, presentación está vacía, precio <= 0, stock <= 0
     */
    Medication createMedication(
            String name,
            String description,
            String unit,
            BigDecimal price,
            Long branchId,
            Integer initialStock
    );
}
