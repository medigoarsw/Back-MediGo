package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in.dto.DeliveryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@Tag(name = "Logistics", description = "Gestión de entregas y logística")
@Slf4j
public class LogisticsController {
    private final UpdateLocationUseCase updateLocationUseCase;
    private final AssignDeliveryUseCase assignDeliveryUseCase;  // Contiene completeDelivery para HU-10

    @PutMapping("/deliveries/{id}/location")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Object req) {
        return ResponseEntity.ok().build();
    }

    /**
     * HU-10: Confirma la entrega de un pedido
     * Actualiza el estado de la entrega a DELIVERED y del pedido a ENTREGADO
     */
    @PutMapping("/deliveries/{id}/complete")
    @Operation(
        summary = "Confirmar entrega (Repartidor)",
        description = "El repartidor confirma que ha entregado el pedido. Actualiza estado del pedido a ENTREGADO y registra fecha/hora de entrega."
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Entrega confirmada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    example = """
                        {
                          "id": 1,
                          "orderId": 100,
                          "deliveryPersonId": 5,
                          "status": "DELIVERED",
                          "assignedAt": "2026-04-02T14:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Entrega no está en estado IN_ROUTE"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Entrega no encontrada"
        )
    })
    public ResponseEntity<DeliveryResponse> completeDelivery(
            @Parameter(
                name = "id",
                description = "ID de la entrega a confirmar",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        log.info("HU-10: Recibida solicitud para confirmar entrega con ID: {}", id);

        Delivery delivery = assignDeliveryUseCase.completeDelivery(id);

        DeliveryResponse response = DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .build();

        log.info("HU-10: Entrega {} confirmada exitosamente", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deliveries/assign")
    public ResponseEntity<?> assign(@RequestBody Object req) {
        return ResponseEntity.ok().build();
    }
}