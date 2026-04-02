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
            
            CartResponse response = buildCartResponse(cart);
            log.info("Medicamento agregado exitosamente. Total: {}", response.totalPrice);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
                    
        } catch (Exception e) {
            log.error("Error al agregar medicamento al carrito", e);
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorMessage(e.getMessage()));
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
            CartResponse response = buildCartResponse(cart);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener carrito", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorMessage(e.getMessage()));
        }
    }
    
    @PostMapping
    @Operation(
        summary = "Crear nueva orden",
        description = "Crea una nueva orden (carrito) para el cliente. " +
                     "Próximamente: proceso de creación de órdenes."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Orden creada exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida"
        )
    })
    public ResponseEntity<?> create(@RequestBody Object req) {
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Confirmar orden",
        description = "Confirma una orden pendiente y la transiciona a estado CONFIRMED. " +
                     "Próximamente: confirmación de órdenes con validación de inventario."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Orden confirmada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Orden no encontrada"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Orden no puede ser confirmada (estado inválido)"
        )
    })
    public ResponseEntity<?> confirm(
            @Parameter(
                name = "id",
                description = "ID de la orden a confirmar",
                required = true
            )
            @PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my-orders")
    @Operation(
        summary = "Obtener mis órdenes",
        description = "Retorna todas las órdenes (en cualquier estado) del cliente autenticado. " +
                     "Próximamente: listado de órdenes confirmadas, pendientes y entregadas."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de órdenes obtenida exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado"
        )
    })
    public ResponseEntity<?> myOrders() {
        return ResponseEntity.ok().build();
    }
    
    // ────── Helper Methods ──────
    
    private CartResponse buildCartResponse(Order cart) {
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
                "Medicamento agregado al carrito exitosamente"
        );
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
}