package edu.escuelaing.arsw.medigo.catalog.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fila agregada de inventario para vistas administrativas.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMedicationAggregate {

    private Long medicationId;
    private String medicationName;
    private String description;
    private String unit;
    private BigDecimal unitPrice;
    private Integer quantity;
}
