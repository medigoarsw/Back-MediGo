package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.orders.application.OrderService;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.CreateOrderUseCase;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.ConfirmOrderUseCase;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OrderController - Unit Tests con Mockito")
class OrderControllerTest {

    private OrderController controller;

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private ConfirmOrderUseCase confirmOrderUseCase;

    @Mock
    private OrderService orderService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Long AFFILIATE_ID = 1L;
    private static final Long BRANCH_ID = 1L;
    private static final Long MEDICATION_ID = 5L;
    private static final int QUANTITY = 2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new OrderController(createOrderUseCase, confirmOrderUseCase, orderService);
    }

    // ────── Test: POST /api/orders/cart/add - Casos Exitosos ──────

    @Test
    @DisplayName("✅ Agregar medicamento exitosamente al carrito")
    void testAddToCartSuccessfully() {
        // Given: medicamento nuevo en carrito vacío
        Order cartWithItem = Order.builder()
                .id(1L)
                .affiliateId(AFFILIATE_ID)
                .branchId(BRANCH_ID)
                .status(Order.OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(50.00))
                .items(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .medicationId(MEDICATION_ID)
                                .quantity(QUANTITY)
                                .unitPrice(BigDecimal.valueOf(25.00))
                                .build()
                )))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, QUANTITY))
                .thenReturn(cartWithItem);

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, QUANTITY)
        );

        // Then: verificar respuesta exitosa
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("❌ Error al agregar: Cantidad debe ser mayor a 0")
    void testAddToCartWithZeroQuantity() {
        // Given: cantidad = 0
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0))
                .thenThrow(new BusinessException("La cantidad debe ser mayor a 0"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0);
    }

    @Test
    @DisplayName("❌ Error al agregar: ID del cliente inválido (≤ 0)")
    void testAddToCartWithInvalidAffiliateId() {
        // Given: affiliateId = -1
        when(orderService.addItemToCart(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY))
                .thenThrow(new BusinessException("ID del cliente inválido"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("❌ Error al agregar: ID de la sucursal inválido (≤ 0)")
    void testAddToCartWithInvalidBranchId() {
        // Given: branchId = 0
        when(orderService.addItemToCart(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY))
                .thenThrow(new BusinessException("ID de la sucursal inválido"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("❌ Error al agregar: ID del medicamento inválido (≤ 0)")
    void testAddToCartWithInvalidMedicationId() {
        // Given: medicationId negativo
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY))
                .thenThrow(new BusinessException("ID del medicamento inválido"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY);
    }

    @Test
    @DisplayName("❌ Error al agregar: Stock insuficiente (> 100)")
    void testAddToCartExceedsStockLimit() {
        // Given: cantidad > 100
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101))
                .thenThrow(new BusinessException("No hay suficiente stock disponible. Stock máximo permitido: 100"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101);
    }

    // ────── Test: GET /api/orders/cart ──────

    @Test
    @DisplayName("✅ Obtener carrito exitosamente")
    void testGetCartSuccessfully() {
        // Given: cliente con carrito existente
        Order existingCart = Order.builder()
                .id(1L)
                .affiliateId(AFFILIATE_ID)
                .branchId(BRANCH_ID)
                .status(Order.OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(50.00))
                .items(new ArrayList<>(List.of(
                        OrderItem.builder()
                                .medicationId(MEDICATION_ID)
                                .quantity(QUANTITY)
                                .unitPrice(BigDecimal.valueOf(25.00))
                                .build()
                )))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getCart(AFFILIATE_ID, BRANCH_ID))
                .thenReturn(existingCart);

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.getCart(AFFILIATE_ID, BRANCH_ID);

        // Then: verificar respuesta exitosa
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService).getCart(AFFILIATE_ID, BRANCH_ID);
    }

    @Test
    @DisplayName("❌ Error al obtener carrito: No existe para el cliente")
    void testGetCartNotFound() {
        // Given: cliente sin carrito
        when(orderService.getCart(AFFILIATE_ID, BRANCH_ID))
                .thenThrow(new ResourceNotFoundException("Carrito no encontrado"));

        // When: llamamos al controlador
        ResponseEntity<Object> response = controller.getCart(AFFILIATE_ID, BRANCH_ID);

        // Then: verificar error 404
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderService).getCart(AFFILIATE_ID, BRANCH_ID);
    }

    // ────── Test: POST /api/orders (Crear carrito) ──────

    @Test
    @DisplayName("✅ Crear carrito nuevo exitosamente")
    void testCreateCartSuccessfully() {
        // Given: solicitud para crear carrito vacío
        Order newCart = Order.builder()
                .id(1L)
                .affiliateId(AFFILIATE_ID)
                .branchId(BRANCH_ID)
                .status(Order.OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(createOrderUseCase.createOrder(eq(AFFILIATE_ID), eq(BRANCH_ID), any(), any(), any()))
                .thenReturn(newCart);

        // When: llamamos al controlador
        ResponseEntity<?> response = controller.create(
                new OrderController.CreateOrderRequest(AFFILIATE_ID, BRANCH_ID, null, null, null)
        );

        // Then: verificar respuesta exitosa
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(createOrderUseCase).createOrder(eq(AFFILIATE_ID), eq(BRANCH_ID), any(), any(), any());
    }

    @Test
    @DisplayName("❌ Error al crear carrito: affiliateId inválido")
    void testCreateCartInvalidAffiliateId() {
        // Given: affiliateId = 0
        when(createOrderUseCase.createOrder(eq(0L), eq(BRANCH_ID), any(), any(), any()))
                .thenThrow(new BusinessException("affiliateId debe ser mayor a 0"));

        // When: llamamos al controlador
        ResponseEntity<?> response = controller.create(
                new OrderController.CreateOrderRequest(0L, BRANCH_ID, null, null, null)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(createOrderUseCase).createOrder(eq(0L), eq(BRANCH_ID), any(), any(), any());
    }

    @Test
    @DisplayName("❌ Error al crear carrito: branchId inválido")
    void testCreateCartInvalidBranchId() {
        // Given: branchId = -1
        when(createOrderUseCase.createOrder(eq(AFFILIATE_ID), eq(-1L), any(), any(), any()))
                .thenThrow(new BusinessException("branchId debe ser mayor a 0"));

        // When: llamamos al controlador
        ResponseEntity<?> response = controller.create(
                new OrderController.CreateOrderRequest(AFFILIATE_ID, -1L, null, null, null)
        );

        // Then: verificar error 400
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(createOrderUseCase).createOrder(eq(AFFILIATE_ID), eq(-1L), any(), any(), any());
    }

}
