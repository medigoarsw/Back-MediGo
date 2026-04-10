package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in.dto;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de entrega
 * HU-10: Confirmación de entrega
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    name = "DeliveryResponse",
    description = "Información de una entrega con su estado actual"
)
public class DeliveryResponse {

    @Schema(
        description = "ID único de la entrega",
        example = "1"
    )
    private Long id;

    @Schema(
        description = "ID del pedido asociado",
        example = "100"
    )
    private Long orderId;

    @Schema(
        description = "ID del repartidor asignado",
        example = "5"
    )
    private Long deliveryPersonId;

    @Schema(
        description = "Estado actual de la entrega",
        example = "DELIVERED",
        allowableValues = {"ASSIGNED", "IN_ROUTE", "DELIVERED"}
    )
    private Delivery.DeliveryStatus status;

    @Schema(
        description = "Fecha y hora de asignación de la entrega",
        example = "2026-04-02T14:30:00"
    )
    private LocalDateTime assignedAt;

    // Punto de recogida (farmacia/sede)
    private Double pickupLat;
    private Double pickupLng;
    private String branchName;

    // Punto de entrega (domicilio del afiliado)
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
}
