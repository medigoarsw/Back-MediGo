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
import java.util.List;

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

        List<Delivery> activeDeliveries = getActiveDeliveriesUseCase.getActiveDeliveries(deliveryPersonId);

        List<DeliveryResponse> responses = activeDeliveries.stream()
                .map(delivery -> DeliveryResponse.builder()
                        .id(delivery.getId())
                        .orderId(delivery.getOrderId())
                        .deliveryPersonId(delivery.getDeliveryPersonId())
                        .status(delivery.getStatus())
                        .assignedAt(delivery.getAssignedAt())
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

    @GetMapping("/deliveries/history")
    public ResponseEntity<List<DeliveryResponse>> getDeliveryHistory(
            @RequestParam Long deliveryPersonId,
            @RequestParam(required = false) String range) {
        log.info("Solicitando historial de entregas para repartidor: {}, rango: {}", deliveryPersonId, range);
        // TODO: implementar consulta de entregas con status=DELIVERED por deliveryPersonId
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/deliveries/history/summary")
    public ResponseEntity<?> getDeliveryHistorySummary(
            @RequestParam Long deliveryPersonId,
            @RequestParam(required = false) String range) {
        log.info("Solicitando resumen de historial para repartidor: {}, rango: {}", deliveryPersonId, range);
        return ResponseEntity.ok(java.util.Map.of(
                "totalTrips", 0,
                "tripsGrowthPct", 0,
                "averageRating", 5.0,
                "monthlyEarnings", 0,
                "currency", "COP"
        ));
    }

    @PostMapping("/driver/emergency")
    public ResponseEntity<?> reportEmergency(@RequestBody(required = false) Object body) {
        log.info("Soporte de emergencia reportado: {}", body);
        return ResponseEntity.ok(java.util.Map.of("message", "Reporte de emergencia recibido"));
    }

    @PostMapping("/deliveries/assign")
    public ResponseEntity<?> assign(@RequestBody Object req) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        log.info("Mock dashboard requested");
        return ResponseEntity.ok(java.util.Map.of(
            "activeDeliveries", 0,
            "completedToday", 0,
            "pendingAssignments", 0
        ));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Object body) {
        log.info("Mock logistics order created");
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("id", 123, "status", "CREATED"));
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> assignCourier(@RequestBody Object body) {
        log.info("Mock courier assigned");
        return ResponseEntity.ok(java.util.Map.of("message", "Courier assigned successfully"));
    }
}