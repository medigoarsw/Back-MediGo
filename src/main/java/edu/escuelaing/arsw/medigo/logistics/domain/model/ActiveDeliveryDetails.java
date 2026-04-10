package edu.escuelaing.arsw.medigo.logistics.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Vista enriquecida de una entrega activa: combina datos de Delivery, Order y Branch
 * para que el repartidor pueda ver origen (farmacia) y destino (afiliado) en el mapa.
 */
@Getter
@Builder
public class ActiveDeliveryDetails {
    private Long id;
    private Long orderId;
    private Long deliveryPersonId;
    private Delivery.DeliveryStatus status;
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
