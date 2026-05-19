package edu.escuelaing.arsw.medigo.logistics.application;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LocationStatePort;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LogisticsEventPublisherPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogisticsService - Unit Tests")
class LogisticsServiceTest {

    @Mock
    private LocationStatePort locationState;
    @Mock
    private DeliveryRepositoryPort deliveryRepository;
    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private LogisticsEventPublisherPort eventPublisher;

    @InjectMocks
    private LogisticsService logisticsService;

    private Delivery mockDelivery;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockDelivery = Delivery.builder()
                .id(100L)
                .orderId(200L)
                .deliveryPersonId(300L)
                .status(Delivery.DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        mockOrder = Order.builder()
                .id(200L)
                .status(Order.OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("updateLocation throws UnsupportedOperationException")
    void updateLocation_throwsException() {
        LocationUpdate loc = new LocationUpdate(100L, 1.0, 1.0, 100L);
        assertThrows(UnsupportedOperationException.class, () -> logisticsService.updateLocation(loc));
    }

    @Test
    @DisplayName("assignDelivery - Success")
    void assignDelivery_success() {
        when(orderRepository.findById(200L)).thenReturn(Optional.of(mockOrder));
        when(deliveryRepository.findByOrderId(200L)).thenReturn(Optional.empty());
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        Delivery result = logisticsService.assignDelivery(200L, 300L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(Delivery.DeliveryStatus.ASSIGNED, result.getStatus());
        verify(orderRepository).updateStatus(200L, Order.OrderStatus.ASSIGNED);
        verify(eventPublisher).publishOrderStatusUpdate(eq(200L), anyMap());
    }

    @Test
    @DisplayName("assignDelivery - Order not found throws ResourceNotFoundException")
    void assignDelivery_orderNotFound_throwsException() {
        when(orderRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> logisticsService.assignDelivery(200L, 300L));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("assignDelivery - Delivery already exists throws BusinessException")
    void assignDelivery_deliveryExists_throwsException() {
        when(orderRepository.findById(200L)).thenReturn(Optional.of(mockOrder));
        when(deliveryRepository.findByOrderId(200L)).thenReturn(Optional.of(mockDelivery));

        assertThrows(BusinessException.class, () -> logisticsService.assignDelivery(200L, 300L));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("markInRoute - Success")
    void markInRoute_success() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        Delivery result = logisticsService.markInRoute(100L);

        assertNotNull(result);
        assertEquals(Delivery.DeliveryStatus.IN_ROUTE, result.getStatus());
        verify(deliveryRepository).updateStatus(100L, Delivery.DeliveryStatus.IN_ROUTE);
        verify(orderRepository).updateStatus(200L, Order.OrderStatus.IN_ROUTE);
        verify(eventPublisher).publishOrderStatusUpdate(eq(200L), anyMap());
    }

    @Test
    @DisplayName("markInRoute - Not ASSIGNED throws BusinessException")
    void markInRoute_notAssigned_throwsException() {
        mockDelivery = Delivery.builder().id(mockDelivery.getId()).orderId(mockDelivery.getOrderId()).deliveryPersonId(mockDelivery.getDeliveryPersonId()).assignedAt(mockDelivery.getAssignedAt()).status(Delivery.DeliveryStatus.IN_ROUTE).build();
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        assertThrows(BusinessException.class, () -> logisticsService.markInRoute(100L));
    }

    @Test
    @DisplayName("completeDelivery - Success")
    void completeDelivery_success() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        Delivery result = logisticsService.completeDelivery(100L);

        assertNotNull(result);
        assertEquals(Delivery.DeliveryStatus.DELIVERED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
        verify(deliveryRepository).updateStatusAndDeliveredAt(eq(100L), eq(Delivery.DeliveryStatus.DELIVERED), any(LocalDateTime.class));
        verify(orderRepository).updateStatusAndDeliveredAt(eq(200L), eq(Order.OrderStatus.DELIVERED), any(LocalDateTime.class));
        verify(eventPublisher).publishOrderStatusUpdate(eq(200L), anyMap());
    }

    @Test
    @DisplayName("completeDelivery - Already DELIVERED throws BusinessException")
    void completeDelivery_alreadyDelivered_throwsException() {
        mockDelivery = Delivery.builder().id(mockDelivery.getId()).orderId(mockDelivery.getOrderId()).deliveryPersonId(mockDelivery.getDeliveryPersonId()).assignedAt(mockDelivery.getAssignedAt()).status(Delivery.DeliveryStatus.DELIVERED).build();
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        assertThrows(BusinessException.class, () -> logisticsService.completeDelivery(100L));
    }

    @Test
    @DisplayName("getActiveDeliveries - Success")
    void getActiveDeliveries_success() {
        when(deliveryRepository.findActiveByDeliveryPersonId(300L)).thenReturn(List.of(mockDelivery));

        List<Delivery> results = logisticsService.getActiveDeliveries(300L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getId());
    }

    @Test
    @DisplayName("getDeliveryIfOwner - Success")
    void getDeliveryIfOwner_success() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        Delivery result = logisticsService.getDeliveryIfOwner(100L, 300L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("getDeliveryIfOwner - Wrong Owner throws BusinessException")
    void getDeliveryIfOwner_wrongOwner_throwsException() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(mockDelivery));

        assertThrows(BusinessException.class, () -> logisticsService.getDeliveryIfOwner(100L, 999L));
    }
}
