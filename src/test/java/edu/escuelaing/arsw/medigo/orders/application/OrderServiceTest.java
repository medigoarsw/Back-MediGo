package edu.escuelaing.arsw.medigo.orders.application;

import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
}
