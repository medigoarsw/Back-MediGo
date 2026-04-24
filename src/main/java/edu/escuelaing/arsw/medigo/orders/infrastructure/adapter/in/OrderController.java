package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.orders.application.OrderService;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "API de órdenes y carrito de compras")
public class OrderController {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final OrderService orderService;
    
    /**
     * POST /api/orders/cart/add
     * Agrega un medicamento al carrito de compras
     */
    @PostMapping("/cart/add")
    @Operation(
        summary = "Agregar medicamento al carrito",
        description = "Agrega un nuevo medicamento al carrito de compras del cliente. " +
                     "Si el medicamento ya existe, incrementa la cantidad. " +
                     "Valida disponibilidad de stock (máximo 100 unidades por medicamento)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Medicamento agregado exitosamente al carrito",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Éxito",
                    value = """
                        {
                          "cartId": 42,
                          "affiliateId": 1,
                          "branchId": 1,
                          "items": [
                            {
                              "medicationId": 5,
                              "quantity": 2,
                              "unitPrice": 25.00,
                              "subtotal": 50.00
                            }
                          ],
                          "totalPrice": 50.00,
                          "message": "Medicamento agregado al carrito exitosamente"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida - Los siguientes errores pueden ocurrir:\\n" +
                         "• La cantidad debe ser mayor a 0\\n" +
                         "• ID del cliente inválido\\n" +
                         "• ID de la sucursal inválido\\n" +
                         "• ID del medicamento inválido\\n" +
                         "• No hay suficiente stock disponible",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Cantidad inválida (≤ 0)",
                        value = """
                            {
                              "message": "La cantidad debe ser mayor a 0"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Stock insuficiente",
                        value = """
                            {
                              "message": "No hay suficiente stock disponible. Máximo: 100 unidades"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "ID de cliente inválido",
                        value = """
                            {
                              "message": "ID del cliente inválido"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "ID de medicamento inválido",
                        value = """
                            {
                              "message": "ID del medicamento inválido"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Error interno",
                    value = """
                        {
                          "message": "Error interno del servidor. Por favor intenta nuevamente"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Object> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        try {
            log.info("Agregando medicamento {} al carrito del cliente {}", 
                    request.medicationId, request.affiliateId);
            
            Order cart = orderService.addItemToCart(
                    request.affiliateId,
                    request.branchId,
                    request.medicationId,
                    request.quantity
            );
            
            CartResponse response = buildCartResponse(cart, "Medicamento agregado al carrito exitosamente");
            log.info("Medicamento agregado exitosamente. Total: {}", response.totalPrice);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Error al agregar medicamento al carrito", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error al agregar medicamento al carrito";
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage(errorMessage));
        }
    }
    
    /**
     * GET /api/orders/cart
     * Obtiene el carrito actual del cliente
     */
    @GetMapping("/cart")
    @Operation(
        summary = "Obtener carrito actual",
        description = "Recupera el carrito de compras (con estado PENDING) del cliente en una sucursal específica. " +
                     "Si no existe carrito, retorna 404."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Carrito obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Carrito con items",
                    value = """
                        {
                          "cartId": 42,
                          "affiliateId": 1,
                          "branchId": 1,
                          "items": [
                            {
                              "medicationId": 5,
                              "quantity": 2,
                              "unitPrice": 25.00,
                              "subtotal": 50.00
                            },
                            {
                              "medicationId": 7,
                              "quantity": 1,
                              "unitPrice": 15.50,
                              "subtotal": 15.50
                            }
                          ],
                          "totalPrice": 65.50,
                          "message": "Carrito obtenido exitosamente"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos - Los siguientes errores pueden ocurrir:\\n" +
                         "• affiliateId menor o igual a 0\\n" +
                         "• branchId menor o igual a 0\\n" +
                         "• Parámetros requeridos faltantes",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Parámetro affiliateId inválido",
                        value = """
                            {
                              "message": "affiliateId debe ser mayor a 0"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Parámetro branchId inválido",
                        value = """
                            {
                              "message": "branchId debe ser mayor a 0"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Parámetro faltante",
                        value = """
                            {
                              "message": "Parámetro requerido 'affiliateId' no presente"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado - El cliente no tiene un carrito pendiente en esta sucursal",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Carrito no encontrado",
                    value = """
                        {
                          "message": "Carrito no encontrado para el cliente 1 en la sucursal 1"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Error interno",
                    value = """
                        {
                          "message": "Error interno del servidor. Por favor intenta nuevamente"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Object> getCart(
            @Parameter(
                name = "affiliateId",
                description = "ID del cliente (afiliado)",
                required = true,
                schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @RequestParam Long affiliateId,
            @Parameter(
                name = "branchId",
                description = "ID de la sucursal",
                required = true,
                schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @RequestParam Long branchId) {
        try {
            log.debug("Obteniendo carrito del cliente {}", affiliateId);
            
            Order cart = orderService.getCart(affiliateId, branchId);
            CartResponse response = buildCartResponse(cart, "Carrito obtenido exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener carrito", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error al obtener carrito";
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(errorMessage));
        }
    }
    
    /**
     * POST /api/orders
     * Crea un nuevo carrito vacío (Order con status PENDING)
     * 
     * El carrito creado estará vacío hasta que se agreguen medicamentos mediante POST /api/orders/cart/add
     */
    @PostMapping
    @Operation(
        summary = "Crear nuevo carrito",
        description = "Crea un nuevo carrito (orden vacía) para el cliente en una sucursal específica. " +
                     "El carrito se crea con estado PENDING y sin items. " +
                     "Luego se pueden agregar medicamentos mediante POST /api/orders/cart/add"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Carrito creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateOrderResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Carrito creado",
                    value = """
                        {
                          "id": 7,
                          "affiliateId": 1,
                          "branchId": 1,
                          "status": "PENDING",
                          "totalAmount": 0.00,
                          "deliveryFee": 5.00,
                          "items": [],
                          "notes": "Dejar en portería",
                          "createdAt": "2026-04-03T14:00:00",
                          "message": "Carrito creado exitosamente"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida - Los siguientes errores pueden ocurrir:\\n" +
                         "• affiliateId es requerido y debe ser > 0\\n" +
                         "• branchId es requerido y debe ser > 0\\n" +
                         "• Cliente (affiliateId) no existe\\n" +
                         "• Sucursal (branchId) no existe",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "affiliateId inválido",
                        value = """
                            {
                              "message": "affiliateId debe ser mayor a 0"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "branchId inválido",
                        value = """
                            {
                              "message": "branchId debe ser mayor a 0"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Cliente no existe",
                        value = """
                            {
                              "message": "Cliente con ID 999 no existe"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Sucursal no existe",
                        value = """
                            {
                              "message": "Sucursal con ID 999 no existe"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorMessage.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Error interno",
                    value = """
                        {
                          "message": "Error interno del servidor. Por favor intenta nuevamente"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<?> create(
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            log.info("Creando nuevo carrito para cliente {} en sucursal {}", 
                    request.affiliateId, request.branchId);
            
            Order newCart = createOrderUseCase.createOrder(
                    request.affiliateId,
                    request.branchId,
                    request.addressLat,
                    request.addressLng,
                    List.of()
            );
            
            CreateOrderResponse response = new CreateOrderResponse(
                    newCart.getId(),
                    newCart.getAffiliateId(),
                    newCart.getBranchId(),
                    newCart.getStatus().name(),
                    newCart.getTotalPrice(),
                    BigDecimal.valueOf(5.00),
                    List.of(),
                    request.notes,
                    newCart.getCreatedAt(),
                    "Carrito creado exitosamente"
            );
            
            log.info("Carrito {} creado exitosamente", newCart.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Error al crear carrito", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error al crear carrito";
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage(errorMessage));
        }
    }
    
    
    /**
     * POST /api/orders/{branchId}/confirm
     * HU-06: Confirma el carrito pendiente del cliente con dirección de envío
     */
    @PostMapping("/{branchId}/confirm")
    @Operation(
        summary = "Confirmar carrito con dirección de envío",
        description = "Confirma el carrito pendiente (PENDING) del cliente y agrega dirección de envío. " +
                     "Genera número de orden único, valida dirección completa, cambia estado a CONFIRMED, " +
                     "y crea nuevo carrito vacío."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Carrito confirmado exitosamente con orden generada"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida - dirección incompleta o carrito vacío"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<Object> confirmPendingOrder(
            @Parameter(
                name = "affiliateId",
                description = "ID del cliente (afiliado)",
                required = true,
                schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @RequestParam Long affiliateId,
            @Parameter(
                name = "branchId",
                description = "ID de la sucursal",
                required = true,
                schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @PathVariable Long branchId,
            @Valid @RequestBody edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest request) {
        try {
            log.info("Confirmando carrito para cliente {} en sucursal {}", affiliateId, branchId);
            
            Order confirmedOrder = orderService.confirmPendingOrder(affiliateId, branchId, request);
            
            log.info("Carrito confirmado exitosamente. Número de orden: {}", confirmedOrder.getOrderNumber());
            
            return ResponseEntity.ok(confirmedOrder);
            
        } catch (Exception e) {
            log.error("Error al confirmar carrito: {}", e.getMessage(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error al confirmar carrito";
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage(errorMessage));
        }
    }
    
    /**
     * GET /api/orders?status=CONFIRMED
     * Lista pedidos filtrados por estado. Accesible a DELIVERY (repartidores) y ADMIN.
     */
    @GetMapping
    @Operation(
        summary = "Listar pedidos por estado",
        description = "Retorna pedidos filtrados por estado. Sin parámetro devuelve todos."
    )
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status) {
        try {
            List<Order> orders;
            if (status != null && !status.isBlank()) {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderService.findByStatus(orderStatus);
            } else {
                orders = List.of();
            }
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage("Estado inválido: " + status));
        } catch (Exception e) {
            log.error("Error al listar pedidos por estado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage("Error interno del servidor"));
        }
    }

    /**
     * GET /api/orders/affiliate/{affiliateId}
     * Retorna todos los pedidos del afiliado.
     */
    @GetMapping("/affiliate/{affiliateId}")
    @Operation(
        summary = "Obtener pedidos del afiliado",
        description = "Retorna todas las órdenes del cliente (afiliado)."
    )
    public ResponseEntity<?> getOrdersByAffiliate(@PathVariable Long affiliateId) {
        try {
            List<Order> orders = orderService.findByAffiliateId(affiliateId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al obtener pedidos del afiliado {}", affiliateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage("Error interno del servidor"));
        }
    }
    
    // ────── Helper Methods ──────
    
    private CartResponse buildCartResponse(Order cart, String message) {
        List<CartItemResponse> items = cart.getItems() != null
                ? cart.getItems().stream()
                    .map(item -> new CartItemResponse(
                            item.getMedicationId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    ))
                    .collect(Collectors.toList())
                : List.of();
        
        return new CartResponse(
                cart.getId(),
                cart.getAffiliateId(),
                cart.getBranchId(),
                items,
                cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO,
                message
        );
    }
    
    private CartResponse buildCartResponse(Order cart) {
        return buildCartResponse(cart, "Medicamento agregado al carrito exitosamente");
    }
    
    // ────── DTOs ──────
    
    @Schema(
        name = "AddToCartRequest",
        description = "Solicitud para agregar un medicamento al carrito"
    )
    record AddToCartRequest(
            @Schema(
                description = "ID del cliente (afiliado)",
                example = "1",
                minimum = "1"
            )
            Long affiliateId,
            
            @Schema(
                description = "ID de la sucursal",
                example = "1",
                minimum = "1"
            )
            Long branchId,
            
            @Schema(
                description = "ID del medicamento a agregar",
                example = "5",
                minimum = "1"
            )
            Long medicationId,
            
            @Schema(
                description = "Cantidad de unidades a agregar (1-100)",
                example = "2",
                minimum = "1",
                maximum = "100"
            )
            int quantity
    ) {}
    
    @Schema(
        name = "CartItemResponse",
        description = "Item dentro del carrito de compras"
    )
    record CartItemResponse(
            @Schema(
                description = "ID del medicamento",
                example = "5"
            )
            Long medicationId,
            
            @Schema(
                description = "Cantidad de unidades en el carrito",
                example = "2"
            )
            int quantity,
            
            @Schema(
                description = "Precio unitario del medicamento",
                example = "25.00"
            )
            BigDecimal unitPrice,
            
            @Schema(
                description = "Subtotal (cantidad × precio unitario)",
                example = "50.00"
            )
            BigDecimal subtotal
    ) {}
    
    @Schema(
        name = "CartResponse",
        description = "Respuesta con información del carrito de compras"
    )
    record CartResponse(
            @Schema(
                description = "ID único del carrito",
                example = "42"
            )
            Long cartId,
            
            @Schema(
                description = "ID del cliente propietario del carrito",
                example = "1"
            )
            Long affiliateId,
            
            @Schema(
                description = "ID de la sucursal asociada",
                example = "1"
            )
            Long branchId,
            
            @Schema(
                description = "Lista de medicamentos en el carrito"
            )
            List<CartItemResponse> items,
            
            @Schema(
                description = "Total del carrito (suma de subtotales)",
                example = "65.50"
            )
            BigDecimal totalPrice,
            
            @Schema(
                description = "Mensaje descriptivo de la operación",
                example = "Medicamento agregado al carrito exitosamente"
            )
            String message
    ) {}
    
    @Schema(
        name = "ErrorMessage",
        description = "Respuesta de error estándar"
    )
    record ErrorMessage(
            @Schema(
                description = "Descripción del error ocurrido",
                example = "La cantidad debe ser mayor a 0"
            )
            String message
    ) {}
    
    @Schema(
        name = "CreateOrderRequest",
        description = "Solicitud para crear un nuevo carrito (orden vacía)"
    )
    record CreateOrderRequest(
            @Schema(
                description = "ID del cliente (afiliado) que crea el carrito",
                example = "1",
                minimum = "1"
            )
            Long affiliateId,
            
            @Schema(
                description = "ID de la sucursal en la que se crea el carrito",
                example = "1",
                minimum = "1"
            )
            Long branchId,
            
            @Schema(
                description = "Latitud de la dirección de entrega (opcional)",
                example = "4.6452",
                nullable = true
            )
            Double addressLat,
            
            @Schema(
                description = "Longitud de la dirección de entrega (opcional)",
                example = "-74.0505",
                nullable = true
            )
            Double addressLng,
            
            @Schema(
                description = "Notas adicionales para el repartidor (opcional)",
                example = "Dejar en portería",
                nullable = true
            )
            String notes
    ) {}
    
    @Schema(
        name = "CreateOrderResponse",
        description = "Respuesta al crear un nuevo carrito"
    )
    record CreateOrderResponse(
            @Schema(
                description = "ID único del carrito creado",
                example = "7"
            )
            Long id,
            
            @Schema(
                description = "ID del cliente propietario del carrito",
                example = "1"
            )
            Long affiliateId,
            
            @Schema(
                description = "ID de la sucursal",
                example = "1"
            )
            Long branchId,
            
            @Schema(
                description = "Estado actual del carrito",
                example = "PENDING",
                allowableValues = {"PENDING", "CONFIRMED", "ASSIGNED", "IN_ROUTE", "DELIVERED", "CANCELLED"}
            )
            String status,
            
            @Schema(
                description = "Total actual del carrito (vacío = 0.00)",
                example = "0.00"
            )
            BigDecimal totalAmount,
            
            @Schema(
                description = "Tarifa de envío estándar",
                example = "5.00"
            )
            BigDecimal deliveryFee,
            
            @Schema(
                description = "Items en el carrito (vacío al crear)"
            )
            List<CartItemResponse> items,
            
            @Schema(
                description = "Notas adicionales para el repartidor",
                example = "Dejar en portería",
                nullable = true
            )
            String notes,
            
            @Schema(
                description = "Fecha y hora de creación del carrito",
                example = "2026-04-03T14:00:00"
            )
            LocalDateTime createdAt,
            
            @Schema(
                description = "Mensaje de confirmación",
                example = "Carrito creado exitosamente"
            )
            String message
    ) {}
}