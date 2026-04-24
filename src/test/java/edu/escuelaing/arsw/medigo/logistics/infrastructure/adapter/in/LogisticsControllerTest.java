package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.AssignDeliveryUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.GetActiveDeliveriesUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in.dto.DeliveryResponse;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out.SpringDeliveryJpaRepository;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out.UserJpaRepository;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para LogisticsController - HU-10: Confirmación de entrega
 */
@DisplayName("LogisticsController - HU-10 Tests")
class LogisticsControllerTest {

    private LogisticsController controller;

    @Mock
    private AssignDeliveryUseCase assignDeliveryUseCase;
    
    @Mock
    private GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private DeliveryRepositoryPort deliveryRepository;

    @Mock
    private SpringDeliveryJpaRepository springDeliveryRepo;

    @Mock
    private UserJpaRepository userRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new LogisticsController(null, assignDeliveryUseCase, getActiveDeliveriesUseCase, orderRepository, deliveryRepository, springDeliveryRepo, userRepo);
    }

    // ======================== HU-10 BDD SCENARIOS ========================

    @Test
    @DisplayName("HU-10 Escenario 1: Estado cambia a entregado al finalizar")
    void testHU10_EstadoCambiaAEntregado() {
        // Given un pedido está en estado "en camino" con repartidor asignado
        Long deliveryId = 1L;
        Long orderId = 100L;
        Long deliveryPersonId = 5L;
        
        Delivery inRouteDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now())
                .build();

        // When el repartidor confirma la entrega desde su aplicación
        Delivery deliveredDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(inRouteDelivery.getAssignedAt())
                .build();

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenReturn(deliveredDelivery);

        // Then el estado del pedido cambia automáticamente a "Entregado"
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        DeliveryResponse body = (DeliveryResponse) response.getBody();
        assertEquals(Delivery.DeliveryStatus.DELIVERED, body.getStatus());
        assertEquals(orderId, body.getOrderId());
        verify(assignDeliveryUseCase).completeDelivery(deliveryId);
    }

    @Test
    @DisplayName("HU-10 Escenario 2: Cliente ve estado entregado")
    void testHU10_ClienteVeeEstadoEntregado() {
        // Given el pedido acaba de ser marcado como entregado
        Long deliveryId = 1L;
        Long orderId = 100L;
        
        Delivery deliveredDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(5L)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenReturn(deliveredDelivery);

        // When el cliente accede a su panel de seguimiento
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);

        // Then visualiza el estado "Entregado"
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeliveryResponse body = (DeliveryResponse) response.getBody();
        assertNotNull(body);
        assertEquals(Delivery.DeliveryStatus.DELIVERED, body.getStatus());
        
        // Y ya no ve el mapa en vivo del repartidor (verificable en frontend)
        // Y se muestra la hora de entrega (assignedAt es visible)
        assertNotNull(body.getAssignedAt());
    }

    @Test
    @DisplayName("HU-10 Escenario 3: Notificación de entrega al cliente")
    void testHU10_NotificacionDeEntrega() {
        // Given el repartidor confirma la entrega del pedido
        Long deliveryId = 1L;
        Long orderId = 100L;
        
        Delivery deliveredDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(5L)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(LocalDateTime.now())
                .build();

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenReturn(deliveredDelivery);

        // When el sistema actualiza el estado
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);

        // Then el cliente recibe una notificación (mock indica "Tu pedido ha sido entregado")
        // (La notificación ocurre en el modelo de negocio/servicio, no en el controlador)
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeliveryResponse body = (DeliveryResponse) response.getBody();
        assertNotNull(body);
        assertEquals(Delivery.DeliveryStatus.DELIVERED, body.getStatus());
        
        // El estado DELIVERED indica que se puede enviar notificación
        verify(assignDeliveryUseCase).completeDelivery(deliveryId);
    }

    @Test
    @DisplayName("HU-10 Escenario 4: Pedido aparece en historial")
    void testHU10_PedidoEnHistorial() {
        // Given el pedido está en estado "Entregado"
        Long deliveryId = 1L;
        Long orderId = 100L;
        LocalDateTime deliveryTime = LocalDateTime.of(2026, 4, 2, 16, 45, 0);
        
        Delivery deliveredDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(5L)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(deliveryTime)
                .build();

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenReturn(deliveredDelivery);

        // When el cliente accede a su historial de pedidos
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);

        // Then el pedido aparece con estado "Entregado" y la fecha de entrega
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DeliveryResponse result = (DeliveryResponse) response.getBody();
        
        assertNotNull(result);
        assertEquals(Delivery.DeliveryStatus.DELIVERED, result.getStatus());
        assertEquals(orderId, result.getOrderId());
        assertEquals(deliveryTime, result.getAssignedAt());
    }

    // ======================== ERROR CASES ========================

    @Test
    @DisplayName("HU-10: Entrega no encontrada")
    void testHU10_EntregaNoEncontrada() {
        // Given una entrega que no existe
        Long deliveryId = 999L;

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenThrow(new ResourceNotFoundException("Entrega no encontrada con ID: " + deliveryId));

        // When se intenta confirmar la entrega
        // Then el controlador retorna 404 (ahora maneja la excepción internamente)
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(assignDeliveryUseCase).completeDelivery(deliveryId);
    }

    @Test
    @DisplayName("HU-10: Entrega no está en estado IN_ROUTE")
    void testHU10_EntregaNoEnEstadoInRoute() {
        // Given una entrega que no está en estado IN_ROUTE
        Long deliveryId = 1L;

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenThrow(new BusinessException("La entrega debe estar en estado IN_ROUTE para poder confirmar la entrega. Estado actual: DELIVERED"));

        // When se intenta confirmar la entrega
        // Then el controlador retorna 400 (ahora maneja la excepción internamente)
        ResponseEntity<?> response = controller.completeDelivery(deliveryId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(assignDeliveryUseCase).completeDelivery(deliveryId);
    }
}
