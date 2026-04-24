package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in.dto.DeliveryResponse;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out.SpringDeliveryJpaRepository;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
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
    private final AssignDeliveryUseCase assignDeliveryUseCase;
    private final GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;
    private final OrderRepositoryPort orderRepository;
    private final DeliveryRepositoryPort deliveryRepository;
    private final SpringDeliveryJpaRepository springDeliveryRepo;
    private final UserJpaRepository userRepo;

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
    public ResponseEntity<?> completeDelivery(
            @Parameter(
                name = "id",
                description = "ID de la entrega a confirmar",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        log.info("HU-10: Recibida solicitud para confirmar entrega con ID: {}", id);

        try {
            Delivery delivery = assignDeliveryUseCase.completeDelivery(id);

            DeliveryResponse response = DeliveryResponse.builder()
                    .id(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .deliveryPersonId(delivery.getDeliveryPersonId())
                    .status(delivery.getStatus())
                    .assignedAt(delivery.getAssignedAt())
                    .deliveredAt(delivery.getDeliveredAt())
                    .build();

            log.info("HU-10: Entrega {} confirmada exitosamente a las {}", id, delivery.getDeliveredAt());
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.warn("HU-10: Entrega no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        } catch (BusinessException e) {
            log.warn("HU-10: Error de negocio al confirmar entrega: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
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

    /**
     * HU-10 (Escenario 2): El afiliado consulta el estado de su pedido.
     * Retorna el estado actual del pedido y la fecha de entrega (si está DELIVERED).
     */
    @GetMapping("/orders/{orderId}/status")
    @Operation(
        summary = "Estado del pedido (Afiliado)",
        description = "HU-10: El cliente consulta el estado de su pedido para saber si fue entregado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado del pedido retornado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<?> getOrderStatus(
            @Parameter(name = "orderId", description = "ID del pedido", required = true)
            @PathVariable Long orderId) {
        log.info("HU-10: Consultando estado del pedido {}", orderId);
        return orderRepository.findById(orderId)
                .map(order -> {
                    // Incluir deliveryId si existe para que el afiliado pueda suscribirse al WebSocket
                    Long deliveryId = deliveryRepository.findByOrderId(orderId)
                            .map(edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery::getId)
                            .orElse(null);
                    java.util.Map<String, Object> body = new java.util.HashMap<>();
                    body.put("orderId", order.getId());
                    body.put("status", order.getStatus().name());
                    body.put("deliveredAt", order.getDeliveredAt() != null ? order.getDeliveredAt().toString() : "");
                    body.put("deliveryId", deliveryId);
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
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
    @Operation(summary = "Auto-asignar repartidor a un pedido")
    public ResponseEntity<?> assign(@RequestBody AssignRequest req) {
        log.info("Asignando repartidor {} al pedido {}", req.deliveryPersonId(), req.orderId());
        try {
            Delivery delivery = assignDeliveryUseCase.assignDelivery(req.orderId(), req.deliveryPersonId());
            DeliveryResponse response = DeliveryResponse.builder()
                    .id(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .deliveryPersonId(delivery.getDeliveryPersonId())
                    .status(delivery.getStatus())
                    .assignedAt(delivery.getAssignedAt())
                    .build();
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al asignar repartidor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error interno al asignar pedido"));
        }
    }

    record AssignRequest(Long orderId, Long deliveryPersonId) {}

    @PutMapping("/deliveries/{id}/pickup")
    @Operation(summary = "Marcar recogida en sucursal (IN_ROUTE)")
    public ResponseEntity<?> markPickup(@PathVariable Long id) {
        log.info("Marcando entrega {} como IN_ROUTE", id);
        try {
            Delivery delivery = assignDeliveryUseCase.markInRoute(id);
            DeliveryResponse response = DeliveryResponse.builder()
                    .id(delivery.getId())
                    .orderId(delivery.getOrderId())
                    .deliveryPersonId(delivery.getDeliveryPersonId())
                    .status(delivery.getStatus())
                    .assignedAt(delivery.getAssignedAt())
                    .build();
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestParam(required = false) Long orderId) {
        log.info("Dashboard requested, orderId={}", orderId);

        // Entregas activas (ASSIGNED o IN_ROUTE)
        List<edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity> activeEntities =
                springDeliveryRepo.findAll().stream()
                        .filter(d -> "ASSIGNED".equals(d.getStatus()) || "IN_ROUTE".equals(d.getStatus()))
                        .toList();

        List<java.util.Map<String, Object>> drivers = activeEntities.stream()
                .map(d -> {
                    String driverName = userRepo.findById(d.getDeliveryPersonId())
                            .map(edu.escuelaing.arsw.medigo.users.infrastructure.entity.UserEntity::getName)
                            .orElse("Repartidor " + d.getDeliveryPersonId());

                    // Si la entrega corresponde al pedido del afiliado → ASSIGNED_TO_ME
                    boolean isAssignedToMe = orderId != null && orderId.equals(d.getOrderId());
                    String status = isAssignedToMe ? "ASSIGNED_TO_ME"
                            : "IN_ROUTE".equals(d.getStatus()) ? "BUSY" : "AVAILABLE";

                    log.info("Dashboard driver: deliveryPersonId={} deliveryId={} deliveryOrderId={} requestedOrderId={} isAssignedToMe={}",
                            d.getDeliveryPersonId(), d.getId(), d.getOrderId(), orderId, isAssignedToMe);

                    java.util.Map<String, Object> driver = new java.util.HashMap<>();
                    driver.put("id", d.getDeliveryPersonId());
                    driver.put("deliveryId", d.getId());
                    driver.put("orderId", d.getOrderId());
                    driver.put("name", driverName);
                    driver.put("status", status);
                    // Posición inicial en Bogotá — se actualiza en tiempo real vía WebSocket
                    driver.put("lat", 4.711);
                    driver.put("lng", -74.0721);
                    return driver;
                })
                .toList();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("drivers", drivers);
        response.put("activeDeliveries", activeEntities.size());
        response.put("completedToday", 0);
        response.put("pendingAssignments", 0);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/delivery/{id}")
    public ResponseEntity<?> debugDelivery(@PathVariable Long id) {
        return springDeliveryRepo.findById(id)
                .map(d -> {
                    java.util.Map<String, Object> info = new java.util.HashMap<>();
                    info.put("id", d.getId());
                    info.put("orderId", d.getOrderId());
                    info.put("deliveryPersonId", d.getDeliveryPersonId());
                    info.put("status", d.getStatus());
                    info.put("assignedAt", d.getAssignedAt());
                    return ResponseEntity.ok(info);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/debug/deliveries/active")
    public ResponseEntity<?> debugActiveDeliveries() {
        List<edu.escuelaing.arsw.medigo.logistics.infrastructure.entity.DeliveryEntity> all =
                springDeliveryRepo.findAll().stream()
                        .filter(d -> "ASSIGNED".equals(d.getStatus()) || "IN_ROUTE".equals(d.getStatus()))
                        .toList();
        List<java.util.Map<String, Object>> result = all.stream().map(d -> {
            java.util.Map<String, Object> info = new java.util.HashMap<>();
            info.put("id", d.getId());
            info.put("orderId", d.getOrderId());
            info.put("deliveryPersonId", d.getDeliveryPersonId());
            info.put("status", d.getStatus());
            return info;
        }).toList();
        return ResponseEntity.ok(result);
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