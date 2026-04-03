error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/orders/application/OrderServiceTest.java:java/math/BigDecimal#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/orders/application/OrderServiceTest.java
empty definition using pc, found symbol in pc: java/math/BigDecimal#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 978
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/orders/application/OrderServiceTest.java
text:
```scala
package edu.escuelaing.arsw.medigo.orders.application;

import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateStockUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.@@BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - Pruebas de Carrito de Compras")
class OrderServiceTest {

  @Mock
  private OrderRepositoryPort orderRepository;

  @Mock
  private SearchMedicationUseCase searchMedicationUseCase;

  @Mock
  private UpdateStockUseCase updateStockUseCase;

  @InjectMocks
  private OrderService orderService;

  private static final Long AFFILIATE_ID = 1L;
  private static final Long BRANCH_ID = 1L;
  private static final Long MEDICATION_ID = 5L;

  @BeforeEach
  void setUp() {
    // Setup común para todas las pruebas
  }

  @Test
  @DisplayName("Escenario 1: Agregar producto con stock disponible")
  void testAddItemToCartSuccessfully() {
    // Given: cliente viendo catálogo con medicamento que tiene stock
    Order emptyCart = Order.builder()
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.ZERO)
        .items(new ArrayList<>())
        .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(emptyCart));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> {
          Order order = invocation.getArgument(0);
          order.setId(1L);
          return order;
        });

    // When: agrega producto al carrito
    Order cart = orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 1);

    // Then: producto aparece en carrito con cantidad 1 y total se actualiza
    assertNotNull(cart);
    assertEquals(1, cart.getItems().size());
    assertEquals(MEDICATION_ID, cart.getItems().get(0).getMedicationId());
    assertEquals(1, cart.getItems().get(0).getQuantity());
    assertTrue(cart.getTotalPrice().compareTo(BigDecimal.ZERO) > 0);

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  @DisplayName("Escenario 2: Agregar el mismo producto dos veces - incrementa cantidad sin duplicar")
  void testAddSameMedicationTwiceIncreasesQuantity() {
    // Given: cliente tiene medicamento en carrito con cantidad 1
    OrderItem existingItem = OrderItem.builder()
        .medicationId(MEDICATION_ID)
        .quantity(1)
        .unitPrice(BigDecimal.valueOf(25.00))
        .build();

    Order cartWithItem = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.valueOf(25.00))
        .items(new ArrayList<>(java.util.List.of(existingItem)))
        .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(cartWithItem));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When: agrega nuevamente el mismo medicamento
    Order updatedCart = orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 1);

    // Then: cantidad se incrementa a 2 y no hay segunda entrada duplicada
    assertNotNull(updatedCart);
    assertEquals(1, updatedCart.getItems().size()); // No duplica
    assertEquals(2, updatedCart.getItems().get(0).getQuantity()); // Incrementa a 2
    assertEquals(BigDecimal.valueOf(50.00), updatedCart.getTotalPrice());

    verify(orderRepository).save(any(Order.class));
  }

  @Test
  @DisplayName("Escenario 3: Intentar agregar más unidades que el stock disponible")
  void testAddProductExceedsStockThrowsException() {
    // Given: cliente intenta agregar más de lo permitido (>100 para MVP)
    Order emptyCart = Order.builder()
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .items(new ArrayList<>())
        .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(emptyCart));

    // When & Then: lanza excepción por insuficiente stock
    BusinessException exception = assertThrows(BusinessException.class, () ->
        orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 101)
    );

    assertEquals("No hay suficiente stock disponible. Stock máximo permitido: 100", 
        exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Validación: Cantidad debe ser mayor a 0")
  void testAddWithInvalidQuantity() {
    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () ->
        orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, MEDICATION_ID, 0)
    );

    assertEquals("La cantidad debe ser mayor a 0", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Validación: Medicament ID inválido")
  void testAddWithInvalidMedicationId() {
    // When & Then
    BusinessException exception = assertThrows(BusinessException.class, () ->
        orderService.addItemToCart(AFFILIATE_ID, BRANCH_ID, -1L, 1)
    );

    assertEquals("ID del medicamento inválido", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Obtener carrito: Retorna el carrito del cliente")
  void testGetCartSuccessfully() {
    // Given: cliente tiene un carrito existente
    Order existingCart = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .items(new ArrayList<>())
        .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(existingCart));

    // When: obtiene el carrito
    Order cart = orderService.getCart(AFFILIATE_ID, BRANCH_ID);

    // Then: retorna el carrito
    assertNotNull(cart);
    assertEquals(AFFILIATE_ID, cart.getAffiliateId());
    assertEquals(BRANCH_ID, cart.getBranchId());
    assertEquals(Order.OrderStatus.PENDING, cart.getStatus());
  }

  @Test
  @DisplayName("Obtener carrito: Lanza excepción si no existe")
  void testGetCartNotFoundThrowsException() {
    // Given: cliente sin carrito
    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.empty());

    // When & Then: lanza excepción
    assertThrows(ResourceNotFoundException.class, () ->
        orderService.getCart(AFFILIATE_ID, BRANCH_ID)
    );
  }

  // ────── HU-06: Confirmar Pedido para Envío a Domicilio ──────

  @Test
  @DisplayName("HU-06 Escenario 1: Confirmar pedido con dirección completa")
  void testConfirmPendingOrderWithCompleteAddressSuccess() {
    // Given: cliente tiene carrito con medicamentos
    OrderItem medication = OrderItem.builder()
        .medicationId(MEDICATION_ID)
        .quantity(2)
        .unitPrice(BigDecimal.valueOf(25.00))
        .build();

    Order cartWithItems = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.valueOf(50.00))
        .items(new ArrayList<>(java.util.List.of(medication)))
        .build();

    edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest request = 
        edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest.builder()
            .street("Calle 10")
            .streetNumber("50-20")
            .city("Bogotá")
            .commune("Centro")
            .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(cartWithItems));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // Mock stock disponible: 50 unidades en sucursal
    BranchStock currentStock = BranchStock.builder()
        .medicationId(MEDICATION_ID)
        .branchId(BRANCH_ID)
        .quantity(50)
        .build();
    when(searchMedicationUseCase.getAvailabilityByMedicationBranch(MEDICATION_ID, BRANCH_ID))
        .thenReturn(currentStock);
    doNothing().when(updateStockUseCase).updateStock(anyLong(), anyLong(), anyInt());

    // When: confirma pedido con dirección completa
    Order confirmedOrder = orderService.confirmPendingOrder(AFFILIATE_ID, BRANCH_ID, request);

    // Then: se genera número de orden, estado cambia a CONFIRMED, dirección se guarda
    assertNotNull(confirmedOrder);
    assertNotNull(confirmedOrder.getOrderNumber());
    assertTrue(confirmedOrder.getOrderNumber().matches("ORD-\\d{4}-\\d{6}"));
    assertEquals(Order.OrderStatus.CONFIRMED, confirmedOrder.getStatus());
    assertEquals("Calle 10", confirmedOrder.getStreet());
    assertEquals("50-20", confirmedOrder.getStreetNumber());
    assertEquals("Bogotá", confirmedOrder.getCity());
    assertEquals("Centro", confirmedOrder.getCommune());
    verify(orderRepository, times(2)).save(any(Order.class)); // Una para confirmar, otra para nuevo carrito
    // Verificar que se redujo el stock: 50 - 2 = 48 unidades
    verify(updateStockUseCase).updateStock(BRANCH_ID, MEDICATION_ID, 48);
  }

  @Test
  @DisplayName("HU-06 Escenario 2: Intentar confirmar sin dirección completa")
  void testConfirmPendingOrderWithIncompleteAddressThrowsException() {
    // Given: cliente intenta confirmar sin calle
    Order orderWithItems = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.valueOf(50.00))
        .items(new ArrayList<>(java.util.List.of(
            OrderItem.builder()
                .medicationId(MEDICATION_ID)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(25.00))
                .build()
        )))
        .build();

    edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest incompleteRequest = 
        edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest.builder()
            .street("")  // Calle vacía
            .streetNumber("50-20")
            .city("Bogotá")
            .commune("Centro")
            .build();

    // When & Then: lanza excepción por dirección incompleta
    BusinessException exception = assertThrows(BusinessException.class, () ->
        orderService.confirmPendingOrder(AFFILIATE_ID, BRANCH_ID, incompleteRequest)
    );

    assertEquals("La calle es obligatoria", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("HU-06 Escenario 3: Ver resumen antes de confirmar (con medicamentos)")
  void testConfirmPendingOrderShowsSummaryWithItems() {
    // Given: cliente tiene carrito con resumen: productos, cantidad, subtotal, total
    OrderItem medication1 = OrderItem.builder()
        .medicationId(5L)
        .quantity(2)
        .unitPrice(BigDecimal.valueOf(25.00))
        .build();

    OrderItem medication2 = OrderItem.builder()
        .medicationId(7L)
        .quantity(1)
        .unitPrice(BigDecimal.valueOf(15.50))
        .build();

    Order cartWithMultipleItems = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.valueOf(65.50))  // (2*25.00) + (1*15.50)
        .items(new ArrayList<>(java.util.List.of(medication1, medication2)))
        .build();

    edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest request = 
        edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest.builder()
            .street("Calle 10")
            .streetNumber("50-20")
            .city("Bogotá")
            .commune("Centro")
            .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(cartWithMultipleItems));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // Mock stock disponible para ambos medicamentos
    BranchStock stock1 = BranchStock.builder()
        .medicationId(5L)
        .branchId(BRANCH_ID)
        .quantity(100)
        .build();
    BranchStock stock2 = BranchStock.builder()
        .medicationId(7L)
        .branchId(BRANCH_ID)
        .quantity(50)
        .build();
    when(searchMedicationUseCase.getAvailabilityByMedicationBranch(5L, BRANCH_ID))
        .thenReturn(stock1);
    when(searchMedicationUseCase.getAvailabilityByMedicationBranch(7L, BRANCH_ID))
        .thenReturn(stock2);
    doNothing().when(updateStockUseCase).updateStock(anyLong(), anyLong(), anyInt());

    // When: obtiene confir­mación que muestra resumen completo
    Order confirmedOrder = orderService.confirmPendingOrder(AFFILIATE_ID, BRANCH_ID, request);

    // Then: resumen muestra todos los detalles
    assertNotNull(confirmedOrder);
    assertEquals(2, confirmedOrder.getItems().size());
    assertEquals(BigDecimal.valueOf(65.50), confirmedOrder.getTotalPrice());
    assertEquals(2, confirmedOrder.getItems().get(0).getQuantity());
    assertEquals(1, confirmedOrder.getItems().get(1).getQuantity());
    // Verificar que se redujo stock para ambos medicamentos
    verify(updateStockUseCase).updateStock(BRANCH_ID, 5L, 98);  // 100 - 2 = 98
    verify(updateStockUseCase).updateStock(BRANCH_ID, 7L, 49);  // 50 - 1 = 49
  }

  @Test
  @DisplayName("HU-06 Escenario 4: Carrito se vacía después de confirmar")
  void testConfirmPendingOrderCreatesNewEmptyCart() {
    // Given: cliente confirma carrito con items
    OrderItem medication = OrderItem.builder()
        .medicationId(MEDICATION_ID)
        .quantity(2)
        .unitPrice(BigDecimal.valueOf(25.00))
        .build();

    Order cartWithItems = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.valueOf(50.00))
        .items(new ArrayList<>(java.util.List.of(medication)))
        .build();

    edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest request = 
        edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest.builder()
            .street("Calle 10")
            .streetNumber("50-20")
            .city("Bogotá")
            .commune("Centro")
            .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(cartWithItems));
    when(orderRepository.save(any(Order.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    
    // Mock stock disponible
    BranchStock currentStock = BranchStock.builder()
        .medicationId(MEDICATION_ID)
        .branchId(BRANCH_ID)
        .quantity(50)
        .build();
    when(searchMedicationUseCase.getAvailabilityByMedicationBranch(MEDICATION_ID, BRANCH_ID))
        .thenReturn(currentStock);
    doNothing().when(updateStockUseCase).updateStock(anyLong(), anyLong(), anyInt());

    // When: confirma pedido
    Order confirmedOrder = orderService.confirmPendingOrder(AFFILIATE_ID, BRANCH_ID, request);

    // Then: nuevo carrito vacío se crea para el cliente
    assertNotNull(confirmedOrder);
    assertEquals(Order.OrderStatus.CONFIRMED, confirmedOrder.getStatus());
    
    // Verificar que se hizo save dos veces: una para la orden confirmada, otra para nuevo carrito vacío
    verify(orderRepository, times(2)).save(any(Order.class));
  }

  @Test
  @DisplayName("HU-06 Validación: No se puede confirmar carrito vacío")
  void testConfirmPendingOrderEmptyCartThrowsException() {
    // Given: cliente intenta confirmar carrito vacío
    Order emptyCart = Order.builder()
        .id(1L)
        .affiliateId(AFFILIATE_ID)
        .branchId(BRANCH_ID)
        .status(Order.OrderStatus.PENDING)
        .createdAt(LocalDateTime.now())
        .totalPrice(BigDecimal.ZERO)
        .items(new ArrayList<>())  // Carrito vacío
        .build();

    edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest request = 
        edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest.builder()
            .street("Calle 10")
            .streetNumber("50-20")
            .city("Bogotá")
            .commune("Centro")
            .build();

    when(orderRepository.findPendingByAffiliateAndBranch(AFFILIATE_ID, BRANCH_ID))
        .thenReturn(Optional.of(emptyCart));

    // When & Then: lanza excepción por carrito vacío
    BusinessException exception = assertThrows(BusinessException.class, () ->
        orderService.confirmPendingOrder(AFFILIATE_ID, BRANCH_ID, request)
    );

    assertEquals("El carrito está vacío. Agregue medicamentos antes de confirmar", exception.getMessage());
    verify(orderRepository, never()).save(any(Order.class));
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/math/BigDecimal#