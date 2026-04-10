package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.orders.application.OrderService;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest;
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
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "API de órdenes y carrito de compras")
public class OrderController {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final OrderService orderService;
    
    @PostMapping("/cart/add")
    @Operation(summary = "Agregar medicamento al carrito")
    public ResponseEntity<Object> addToCart(@Valid @RequestBody AddToCartRequest request) {
        try {
            Order cart = orderService.addItemToCart(request.affiliateId(), request.branchId(), request.medicationId(), request.quantity());
            return ResponseEntity.status(HttpStatus.CREATED).body(buildCartResponse(cart, "Medicamento agregado al carrito exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }
    
    @GetMapping("/cart")
    @Operation(summary = "Obtener carrito actual")
    public ResponseEntity<Object> getCart(@RequestParam Long affiliateId, @RequestParam Long branchId) {
        try {
            Order cart = orderService.getCart(affiliateId, branchId);
            return ResponseEntity.ok(buildCartResponse(cart, "Carrito obtenido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.ok(new CartResponse(null, affiliateId, branchId, List.of(), BigDecimal.ZERO, "Carrito vacío"));
        }
    }
    
    @PostMapping
    @Operation(summary = "Crear nuevo carrito")
    public ResponseEntity<?> create(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order newCart = createOrderUseCase.createOrder(request.affiliateId(), request.branchId(), request.addressLat(), request.addressLng(), List.of());
            CreateOrderResponse response = new CreateOrderResponse(newCart.getId(), newCart.getAffiliateId(), newCart.getBranchId(), newCart.getStatus().name(), newCart.getTotalPrice(), BigDecimal.valueOf(5.00), List.of(), request.notes(), newCart.getCreatedAt(), "Carrito creado exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Obtener órdenes disponibles para repartidores")
    public ResponseEntity<List<OrderResponse>> getAvailableOrders() {
        List<OrderResponse> response = orderService.getAvailableOrders().stream()
                .map(o -> new OrderResponse(
                        o.getId(),
                        o.getOrderNumber(),
                        o.getAffiliateId(),
                        o.getBranchId(),
                        o.getTotalPrice(),
                        o.getStatus().name(),
                        o.getAddressLat(),
                        o.getAddressLng(),
                        (o.getStreet() != null ? o.getStreet() : "Sin calle") + " " + (o.getStreetNumber() != null ? o.getStreetNumber() : ""),
                        o.getCreatedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    record OrderResponse(
            Long id,
            String orderNumber,
            Long affiliateId,
            Long branchId,
            BigDecimal totalPrice,
            String status,
            Double lat,
            Double lng,
            String deliveryAddress,
            LocalDateTime createdAt
    ) {}
    
    @PostMapping("/{branchId}/confirm")
    @Operation(summary = "Confirmar carrito con dirección de envío")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrito confirmado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<Object> confirmPendingOrder(
            @RequestParam Long affiliateId,
            @PathVariable Long branchId,
            @Valid @RequestBody ConfirmOrderRequest request) {
        try {
            Order confirmedOrder = orderService.confirmPendingOrder(affiliateId, branchId, request);
            return ResponseEntity.ok(confirmedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Obtener mis órdenes")
    public ResponseEntity<?> myOrders() {
        return ResponseEntity.ok().build();
    }
    
    private CartResponse buildCartResponse(Order cart, String message) {
        List<CartItemResponse> items = cart.getItems() != null
                ? cart.getItems().stream().map(item -> new CartItemResponse(item.getMedicationId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal())).collect(Collectors.toList())
                : List.of();
        return new CartResponse(cart.getId(), cart.getAffiliateId(), cart.getBranchId(), items, cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO, message);
    }
    
    record AddToCartRequest(Long affiliateId, Long branchId, Long medicationId, int quantity) {}
    record CartItemResponse(Long medicationId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
    record CartResponse(Long cartId, Long affiliateId, Long branchId, List<CartItemResponse> items, BigDecimal totalPrice, String message) {}
    record ErrorMessage(String message) {}
    record CreateOrderRequest(Long affiliateId, Long branchId, Double addressLat, Double addressLng, String notes) {}
    record CreateOrderResponse(Long id, Long affiliateId, Long branchId, String status, BigDecimal totalAmount, BigDecimal deliveryFee, List<CartItemResponse> items, String notes, LocalDateTime createdAt, String message) {}
}
