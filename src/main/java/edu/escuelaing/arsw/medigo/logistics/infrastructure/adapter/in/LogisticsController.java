package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.ActiveDeliveryDetails;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@Tag(name = "Logistics", description = "Gestión de entregas y logística")
@Slf4j
public class LogisticsController {
    private final UpdateLocationUseCase updateLocationUseCase;
    private final AssignDeliveryUseCase assignDeliveryUseCase;  // Contiene completeDelivery para HU-10
    private final GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;  // HU-11

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

    /**
     * HU-11: Obtiene todas las entregas activas del repartidor
     * Las entregas activas son aquellas no entregadas (ASSIGNED, IN_ROUTE, PENDING_SHIPPING)
     */
    @GetMapping("/deliveries/active")
    @Operation(
        summary = "Obtener entregas activas (Repartidor)",
        description = "Retorna la lista de entregas asignadas y activas del repartidor autenticado. El botón 'Finalizar entrega' solo se muestra en estas entregas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de entregas activas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    example = """
                        [
                          {
                            "id": 1,
                            "orderId": 100,
                            "deliveryPersonId": 5,
                            "status": "IN_ROUTE",
                            "assignedAt": "2026-04-02T14:30:00"
                          },
                          {
                            "id": 2,
                            "orderId": 101,
                            "deliveryPersonId": 5,
                            "status": "ASSIGNED",
                            "assignedAt": "2026-04-02T15:00:00"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de repartidor inválido"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Se requiere autenticación"
        )
    })
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries(
            @Parameter(
                name = "deliveryPersonId",
                description = "ID del repartidor",
                example = "5",
                required = true
            )
            @RequestParam Long deliveryPersonId) {

        log.info("HU-11: Solicitando entregas activas para repartidor: {}", deliveryPersonId);

        List<ActiveDeliveryDetails> activeDeliveries = getActiveDeliveriesUseCase.getActiveDeliveryDetails(deliveryPersonId);

        List<DeliveryResponse> responses = activeDeliveries.stream()
                .map(d -> DeliveryResponse.builder()
                        .id(d.getId())
                        .orderId(d.getOrderId())
                        .deliveryPersonId(d.getDeliveryPersonId())
                        .status(d.getStatus())
                        .assignedAt(d.getAssignedAt())
                        .pickupLat(d.getPickupLat())
                        .pickupLng(d.getPickupLng())
                        .branchName(d.getBranchName())
                        .deliveryLat(d.getDeliveryLat())
                        .deliveryLng(d.getDeliveryLng())
                        .deliveryAddress(d.getDeliveryAddress())
                        .build())
                .toList();

        log.info("HU-11: Se encontraron {} entregas activas", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * HU-11: Obtiene una entrega específica si pertenece al repartidor
     * Valida la propiedad y muestra el modal de confirmación con detalles de la entrega
     */
    @GetMapping("/deliveries/{id}")
    @Operation(
        summary = "Obtener detalle de una entrega (Repartidor)",
        description = "Retorna los detalles de una entrega. El repartidor solo puede ver sus propias entregas. Usado para mostrar la información en el modal de confirmación."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Detalle de la entrega",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    example = """
                        {
                          "id": 1,
                          "orderId": 100,
                          "deliveryPersonId": 5,
                          "status": "IN_ROUTE",
                          "assignedAt": "2026-04-02T14:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Esta entrega no pertenece al repartidor"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Entrega no encontrada"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autorizado - Se requiere autenticación"
        )
    })
    public ResponseEntity<DeliveryResponse> getDeliveryDetail(
            @Parameter(
                name = "id",
                description = "ID de la entrega",
                example = "1",
                required = true
            )
            @PathVariable Long id,
            @Parameter(
                name = "deliveryPersonId",
                description = "ID del repartidor (para validar propiedad)",
                example = "5",
                required = true
            )
            @RequestParam Long deliveryPersonId) {

        log.info("HU-11: Solicitando detalle de entrega {} para repartidor {}", id, deliveryPersonId);

        Delivery delivery = getActiveDeliveriesUseCase.getDeliveryIfOwner(id, deliveryPersonId);

        DeliveryResponse response = DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .build();

        log.info("HU-11: Retornando detalle de entrega {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Repartidor recoge el pedido en la farmacia: ASSIGNED → IN_ROUTE
     */
    @PutMapping("/deliveries/{id}/pickup")
    @Operation(summary = "Repartidor recoge el pedido (ASSIGNED → IN_ROUTE)")
    public ResponseEntity<DeliveryResponse> pickupDelivery(@PathVariable Long id) {
        log.info("Repartidor recogiendo pedido para entrega {}", id);

        Delivery delivery = assignDeliveryUseCase.pickupDelivery(id);

        DeliveryResponse response = DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .build();

        log.info("Entrega {} marcada como IN_ROUTE", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Repartidor acepta un pedido disponible del mercado global
     */
    @PostMapping("/deliveries/accept")
    @Operation(
        summary = "Aceptar un pedido disponible (Repartidor)",
        description = "El repartidor toma un pedido del mercado global y se le asigna."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido aceptado y entrega creada"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @ApiResponse(responseCode = "409", description = "El pedido ya fue tomado por otro repartidor")
    })
    public ResponseEntity<DeliveryResponse> acceptDelivery(
            @RequestParam Long orderId,
            @RequestParam Long driverId) {

        log.info("Repartidor {} aceptando pedido {}", driverId, orderId);

        Delivery delivery = assignDeliveryUseCase.assignDelivery(orderId, driverId);

        DeliveryResponse response = DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .build();

        log.info("Pedido {} aceptado, entrega {} creada", orderId, delivery.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Dashboard logístico del afiliado: pedido activo + estado + repartidor asignado.
     * Usado en la carga inicial de MapaPedidos para restaurar estado tras un refresh.
     */
    @GetMapping("/affiliate/dashboard")
    @Operation(summary = "Dashboard logístico del afiliado (AFFILIATE ONLY)")
    public ResponseEntity<?> getAffiliateDashboard(@RequestParam Long affiliateId) {
        log.info("Solicitando dashboard logístico para afiliado {}", affiliateId);
        Map<String, Object> dashboard = getActiveDeliveriesUseCase.getAffiliateDashboard(affiliateId);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/deliveries/assign")
    public ResponseEntity<?> assign(@RequestBody Object req) {
        return ResponseEntity.ok().build();
    }
}