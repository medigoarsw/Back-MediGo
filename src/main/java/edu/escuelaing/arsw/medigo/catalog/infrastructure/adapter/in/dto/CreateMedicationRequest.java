package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para solicitar la creación de un medicamento con stock inicial (HU-07)
 * 
 * Validaciones:
 * - nombre: obligatorio, no puede estar vacío
 * - presentación (unit): obligatoria, no puede estar vacía
 * - precio: obligatorio, debe ser > 0
 * - branchId: obligatorio, debe ser positivo
 * - initialStock: obligatorio, debe ser > 0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "CreateMedicationRequest",
    description = "Solicitud para crear un medicamento con stock inicial en una sucursal (HU-07)"
)
public class CreateMedicationRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(
        description = "Nombre del medicamento",
        example = "Paracetamol 500mg",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Descripción del medicamento",
        example = "Analgésico y antipirético para dolores leves a moderados"
    )
    private String description;

    @NotBlank(message = "La presentación es obligatoria")
    @Schema(
        description = "Presentación del medicamento (tableta, cápsula, ml, etc)",
        example = "tableta",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String unit;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Schema(
        description = "Precio del medicamento",
        example = "5000.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal price;

    @NotNull(message = "El ID de la sucursal es requerido")
    @Positive(message = "El ID de la sucursal debe ser positivo")
    @Schema(
        description = "ID de la sucursal donde se crea el stock inicial",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long branchId;

    @NotNull(message = "El stock inicial es requerido")
    @Positive(message = "El stock inicial debe ser positivo")
    @Schema(
        description = "Cantidad inicial de stock",
        example = "100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer initialStock;
}
