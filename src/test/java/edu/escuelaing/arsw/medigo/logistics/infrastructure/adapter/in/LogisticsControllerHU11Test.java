package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.AssignDeliveryUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.GetActiveDeliveriesUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in.dto.DeliveryResponse;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas para HU-11: Repartidor presiona botón de finalización al entregar
 *
 * Escenarios:
 * 1. Finalizar entrega exitosamente
 * 2. Cancelar finalización de entrega
 * 3. Botón visible solo en pedidos activos
 * 4. Confirmación antes de finalizar
 * + Validaciones de seguridad y propiedad
 */
@DisplayName("LogisticsController - HU-11: Botón de finalizar entrega")
class LogisticsControllerHU11Test {

    private LogisticsController logisticsController;

    @Mock
    private GetActiveDeliveriesUseCase getActiveDeliveriesUseCase;

    @Mock
    private AssignDeliveryUseCase assignDeliveryUseCase;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private DeliveryRepositoryPort deliveryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logisticsController = new LogisticsController(
                null,
                assignDeliveryUseCase,
                getActiveDeliveriesUseCase,
                orderRepository,
                deliveryRepository
        );
    }

    // ====================
    // ESCENARIO 1: Finalizar entrega exitosamente
    // ====================
    @Test
    @DisplayName("Escenario 1: Repartidor ve botón 'Finalizar entrega' en pedido activo, confirma y entrega se marca como DELIVERED")
    void testHU11_FinalizarEntregaExitosamente() {
        // Given: el repartidor está autenticado en su aplicación
        Long deliveryPersonId = 5L;
        Long deliveryId = 1L;
        Long orderId = 100L;

        // And: tiene un pedido activo en estado "en camino"
        Delivery activeDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(getActiveDeliveriesUseCase.getDeliveryIfOwner(deliveryId, deliveryPersonId))
                .thenReturn(activeDelivery);

        // When: el repartidor presiona el botón "Finalizar entrega"
        ResponseEntity<DeliveryResponse> detailResponse = logisticsController
                .getDeliveryDetail(deliveryId, deliveryPersonId);

        // Then: aparece un modal de confirmación con los detalles
        assertNotNull(detailResponse.getBody());
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());
        assertEquals(deliveryId, detailResponse.getBody().getId());
        assertEquals(orderId, detailResponse.getBody().getOrderId());
        assertEquals(Delivery.DeliveryStatus.IN_ROUTE, detailResponse.getBody().getStatus());

        // When: el repartidor confirma
        Delivery completedDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(activeDelivery.getAssignedAt())
                .build();

        when(assignDeliveryUseCase.completeDelivery(deliveryId))
                .thenReturn(completedDelivery);

        ResponseEntity<?> completeResponse = logisticsController
                .completeDelivery(deliveryId);

        // Then: el estado cambia a DELIVERED y se muestra mensaje de éxito
        assertEquals(HttpStatus.OK, completeResponse.getStatusCode());
        DeliveryResponse completedBody = (DeliveryResponse) completeResponse.getBody();
        assertNotNull(completedBody);
        assertEquals(Delivery.DeliveryStatus.DELIVERED, completedBody.getStatus());

        // And: el pedido desaparece de lista activa (al recargar, no aparecerá en getActiveDeliveries)
        verify(assignDeliveryUseCase, times(1)).completeDelivery(deliveryId);
    }

    // ====================
    // ESCENARIO 2: Cancelar finalización
    // ====================
    @Test
    @DisplayName("Escenario 2: Repartidor cancela la confirmación de entrega y el estado no cambia")
    void testHU11_CancelarFinalizacion() {
        // Given: el repartidor presiona el botón "Finalizar entrega"
        Long deliveryPersonId = 5L;
        Long deliveryId = 1L;
        Long orderId = 100L;

        Delivery activeDelivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now().minusHours(2))
                .build();

        when(getActiveDeliveriesUseCase.getDeliveryIfOwner(deliveryId, deliveryPersonId))
                .thenReturn(activeDelivery);

        // When: aparece el modal de confirmación y presiona "Cancelar"
        ResponseEntity<DeliveryResponse> detailResponse = logisticsController
                .getDeliveryDetail(deliveryId, deliveryPersonId);

        // Then: se obtiene la información pero completeDelivery NO se invoca
        assertNotNull(detailResponse.getBody());
        assertEquals(HttpStatus.OK, detailResponse.getStatusCode());

        // Verifica que completeDelivery nunca fue llamado
        verify(assignDeliveryUseCase, never()).completeDelivery(anyLong());

        // And: el pedido permanece en su lista de entregas activas
        // Aquí verificamos que la entrega sigue siendo IN_ROUTE
        assertEquals(Delivery.DeliveryStatus.IN_ROUTE, detailResponse.getBody().getStatus());
    }

    // ====================
    // ESCENARIO 3: Botón visible solo en pedidos activos
    // ====================
    @Test
    @DisplayName("Escenario 3: El botón 'Finalizar entrega' solo aparece en entregas con estado activo (IN_ROUTE)")
    void testHU11_BotónVisibleSoloEnPedidosActivos() {
        // Given: el repartidor tiene entregas en diferentes estados
        Long deliveryPersonId = 5L;

        // Entregas activas
        Delivery inRouteDelivery = Delivery.builder()
                .id(1L)
                .orderId(100L)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now().minusHours(1))
                .build();

        Delivery assignedDelivery = Delivery.builder()
                .id(2L)
                .orderId(101L)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        // Entrega ya entregada (no debe mostrar botón)
        Delivery deliveredDelivery = Delivery.builder()
                .id(3L)
                .orderId(102L)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(LocalDateTime.now().minusHours(3))
                .build();

        // When: visualiza su lista de entregas
        when(getActiveDeliveriesUseCase.getActiveDeliveries(deliveryPersonId))
                .thenReturn(List.of(inRouteDelivery, assignedDelivery));  // Solo devuelve activas

        ResponseEntity<List<DeliveryResponse>> response = logisticsController
                .getActiveDeliveries(deliveryPersonId);

        // Then: el repartidor ve solo las entregas activas
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        // And: los pedidos ya entregados no aparecen en la lista
        assertFalse(response.getBody().stream()
                .anyMatch(d -> d.getId().equals(deliveredDelivery.getId()))
        );

        // And: las entregas activas tienen el botón habilitado (status != DELIVERED)
        assertTrue(response.getBody().stream()
                .allMatch(d -> !d.getStatus().equals(Delivery.DeliveryStatus.DELIVERED))
        );
    }

    // ====================
    // ESCENARIO 4: Confirmación antes de finalizar
    // ====================
    @Test
    @DisplayName("Escenario 4: El modal de confirmación contiene número de pedido y detalles de la entrega")
    void testHU11_ConfirmacionAntesDeFinalizar() {
        // Given: el repartidor presiona "Finalizar entrega"
        Long deliveryPersonId = 5L;
        Long deliveryId = 1L;
        Long orderId = 100L;

        Delivery delivery = Delivery.builder()
                .id(deliveryId)
                .orderId(orderId)
                .deliveryPersonId(deliveryPersonId)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.of(2026, 4, 2, 14, 30))
                .build();

        when(getActiveDeliveriesUseCase.getDeliveryIfOwner(deliveryId, deliveryPersonId))
                .thenReturn(delivery);

        // When: el modal de confirmación se muestra
        ResponseEntity<DeliveryResponse> response = logisticsController
                .getDeliveryDetail(deliveryId, deliveryPersonId);

        // Then: el modal contiene el número de pedido y dirección de destino
        assertNotNull(response.getBody());
        assertEquals(orderId, response.getBody().getOrderId());
        assertEquals(deliveryId, response.getBody().getId());

        // And: tiene botones "Confirmar" y "Cancelar" (implícito en la interfaz)
        // Confirmar: llamar completeDelivery
        // Cancelar: simplemente no hacer nada

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ====================
    // VALIDACIONES DE SEGURIDAD
    // ====================
    @Test
    @DisplayName("Seguridad: Repartidor solo ve sus propias entregas activas")
    void testHU11_RepartidorSoloVePropias() {
        // Given: existen 2 repartidores con entregas diferentes
        Long repartidor1 = 5L;
        Long repartidor2 = 6L;

        Delivery entregaRepartidor1 = Delivery.builder()
                .id(1L)
                .orderId(100L)
                .deliveryPersonId(repartidor1)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery entregaRepartidor2 = Delivery.builder()
                .id(2L)
                .orderId(101L)
                .deliveryPersonId(repartidor2)
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(LocalDateTime.now())
                .build();

        // When: repartidor 1 solicita sus entregas activas
        when(getActiveDeliveriesUseCase.getActiveDeliveries(repartidor1))
                .thenReturn(List.of(entregaRepartidor1));

        ResponseEntity<List<DeliveryResponse>> response1 = logisticsController
                .getActiveDeliveries(repartidor1);

        // Then: solo ve sus propias entregas
        assertEquals(1, response1.getBody().size());
        assertEquals(repartidor1, response1.getBody().get(0).getDeliveryPersonId());

        // When: repartidor 2 solicita sus entregas activas
        when(getActiveDeliveriesUseCase.getActiveDeliveries(repartidor2))
                .thenReturn(List.of(entregaRepartidor2));

        ResponseEntity<List<DeliveryResponse>> response2 = logisticsController
                .getActiveDeliveries(repartidor2);

        // Then: solo ve sus propias entregas (no ve las de repartidor1)
        assertEquals(1, response2.getBody().size());
        assertEquals(repartidor2, response2.getBody().get(0).getDeliveryPersonId());
    }

    @Test
    @DisplayName("Seguridad: Repartidor no puede acceder a entregas de otro repartidor")
    void testHU11_NoAccesoEntregasOtro() {
        // Given: existe una entrega asignada a repartidor 6
        Long repartidor1 = 5L;
        Long repartidor2 = 6L;
        Long deliveryId = 1L;

        // When: repartidor 1 intenta acceder a una entrega de repartidor 2
        when(getActiveDeliveriesUseCase.getDeliveryIfOwner(deliveryId, repartidor1))
                .thenThrow(new ResourceNotFoundException("No tiene acceso a esta entrega"));

        // Then: obtiene un error 404/403
        assertThrows(ResourceNotFoundException.class, () ->
                logisticsController.getDeliveryDetail(deliveryId, repartidor1)
        );
    }
}
