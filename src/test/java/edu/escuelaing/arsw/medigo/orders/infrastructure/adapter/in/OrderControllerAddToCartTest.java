package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.orders.application.OrderService;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
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
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS DEL ENDPOINT POST /api/orders/cart/add CON MOCKITO
 * 
 * Estas pruebas demuestran:
 * ✅ Cuándo funciona correctamente (casos exitosos)
 * ❌ Cuándo falla y por qué (casos de error)
 */
@DisplayName("OrderController.addToCart() - Pruebas con Mockito")
class OrderControllerAddToCartTest {

    private OrderController controller;

    @Mock
    private OrderService orderService;

    private static final Long AFFILIATE_ID = 1L;
    private static final Long BRANCH_ID = 1L;
    private static final Long MEDICATION_ID = 5L;
    private static final int QUANTITY = 2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Crear controller con mocks inyectados
        controller = new OrderController(null, null, orderService);
    }

    // ════════════════════════════════════════════════════════════════════
    // ✅ CASOS EXITOSOS - EL ENDPOINT DEBE FUNCIONAR
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("✅ ÉXITO: Agregar 1 medicamento nuevo al carrito")
    void testAddToCartSuccess_NewMedication() {
        // 📋 ARRANGE - Preparar datos de prueba
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

        // 🎬 ACT - Ejecutar
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, QUANTITY)
        );

        // ✔️ ASSERT - Validar
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), 
                "Debe retornar HTTP 201 Created");
        assertNotNull(response.getBody(), 
                "La respuesta debe tener cuerpo");
        
        verify(orderService, times(1)).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("✅ ÉXITO: Agregar medicamento a carrito que ya tiene items")
    void testAddToCartSuccess_MultipleItems() {
        // Carrito con 2 items
        Order cartWithMultipleItems = Order.builder()
                .id(1L)
                .affiliateId(AFFILIATE_ID)
                .branchId(BRANCH_ID)
                .status(Order.OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(65.50))
                .items(new ArrayList<>(List.of(
                        OrderItem.builder().medicationId(5L).quantity(2).unitPrice(BigDecimal.valueOf(25.00)).build(),
                        OrderItem.builder().medicationId(7L).quantity(1).unitPrice(BigDecimal.valueOf(15.50)).build()
                )))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, 7L, 1))
                .thenReturn(cartWithMultipleItems);

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, 7L, 1)
        );

        // ASSERT
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, 7L, 1);
    }

    // ════════════════════════════════════════════════════════════════════
    // ❌ CASOS DE ERROR - VALIDACIÓN DE PARÁMETROS
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("❌ ERROR 400: Cantidad = 0 (debe ser > 0)")
    void testAddToCartError_ZeroQuantity() {
        // ARRANGE
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0))
                .thenThrow(new BusinessException("La cantidad debe ser mayor a 0"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0)
        );

        // ASSERT  
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Debe retornar 400 Bad Request para cantidad 0");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("cantidad debe ser mayor"),
                "Debe contener mensaje descriptivo");
        
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0);
    }

    @Test
    @DisplayName("❌ ERROR 400: Cantidad negativa")
    void testAddToCartError_NegativeQuantity() {
        // ARRANGE
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, -5))
                .thenThrow(new BusinessException("La cantidad debe ser mayor a 0"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, -5)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, -5);
    }

    @Test
    @DisplayName("❌ ERROR 400: affiliateId inválido (≤ 0)")
    void testAddToCartError_InvalidAffiliateId() {
        // ARRANGE
        when(orderService.addItemToCart(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY))
                .thenThrow(new BusinessException("ID del cliente inválido"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Debe retornar 400 para ID de cliente inválido");
        assertTrue(response.getBody().toString().contains("cliente inválido"),
                "Debe indicar que el ID del cliente es inválido");

        verify(orderService).addItemToCart(-1L, BRANCH_ID, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("❌ ERROR 400: branchId inválido (≤ 0)")
    void testAddToCartError_InvalidBranchId() {
        // ARRANGE
        when(orderService.addItemToCart(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY))
                .thenThrow(new BusinessException("ID de la sucursal inválido"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("sucursal inválido"));
        verify(orderService).addItemToCart(AFFILIATE_ID, 0L, MEDICATION_ID, QUANTITY);
    }

    @Test
    @DisplayName("❌ ERROR 400: medicationId inválido (≤ 0)")
    void testAddToCartError_InvalidMedicationId() {
        // ARRANGE
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY))
                .thenThrow(new BusinessException("ID del medicamento inválido"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("medicamento inválido"));
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, -1L, QUANTITY);
    }

    // ════════════════════════════════════════════════════════════════════
    // ❌ CASOS DE ERROR - STOCK INSUFICIENTE
    // ════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("❌ ERROR 400: Stock insuficiente (cantidad > 100)")
    void testAddToCartError_ExceedsMaxStock() {
        // ARRANGE - 101 unidades es más del máximo el permitido (100)
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101))
                .thenThrow(new BusinessException("No hay suficiente stock disponible. Stock máximo permitido: 100"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Debe retornar 400 cuando se excede el stock");
        assertTrue(response.getBody().toString().contains("stock disponible"),
                "Debe indicar problema de stock");

        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101);
    }

    @Test
    @DisplayName("❌ ERROR 400: Total de cantidad excede límite (50 + 51 = 101)")
    void testAddToCartError_TotalExceedsMaxStock() {
        // ARRANGE - Intenta agregar 51 a las 50 que ya tiene (total 101)
        when(orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 51))
                .thenThrow(new BusinessException("No hay suficiente stock disponible. Stock máximo permitido: 100"));

        // ACT
        ResponseEntity<Object> response = controller.addToCart(
                new OrderController.AddToCartRequest(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 51)
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService).addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 51);
    }

    // ════════════════════════════════════════════════════════════════════
    //  💡 INTERPRETACIÓN DE RESULTADOS
    // ════════════════════════════════════════════════════════════════════

    /**
     * RESUMEN DE PRUEBAS:
     * 
     * Si ves "✅ ÉXITO" en los tests:
     *   → El endpoint está funcionando correctamente
     *   → El problema es otro (quizás en la DB, en los datos, o en la red)
     * 
     * Si ves "❌ ERROR 400" en los tests:
     *   → El endpoint rechazó tu solicitud por uno de estos motivos:
     *     • Cantidad ≤ 0
     *     • affiliateId/branchId/medicationId ≤ 0 o null
     *     • Cantidad total > 100 unidades
     *     • Los parámetros no coinciden con los esperados
     * 
     * Si el test falla (AssertionError):
     *   → Hay un problema en la lógica del controlador o servicio
     *   → Revisa los logs del servidor para más detalles
     */
}
