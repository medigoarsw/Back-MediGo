package edu.escuelaing.arsw.medigo.logistics.application;
import edu.escuelaing.arsw.medigo.logistics.domain.model.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.*;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsService implements UpdateLocationUseCase, AssignDeliveryUseCase, GetActiveDeliveriesUseCase {
    private final LocationStatePort locationState;
    private final DeliveryRepositoryPort deliveryRepository;
    private final OrderRepositoryPort orderRepository;
    
    @Override
    public void updateLocation(LocationUpdate location) {
        throw new UnsupportedOperationException("TODO Anderson");
    }
    
    @Override
    public Delivery assignDelivery(Long orderId, Long deliveryPersonId) {
        throw new UnsupportedOperationException("TODO Miguel");
    }
    
    /**
     * HU-10: Confirma la entrega de un pedido.
     *
     * Escenario 1: Cambia estado del pedido a DELIVERED y registra fecha/hora de entrega.
     * Escenario 2: Valida que el delivery exista y esté en estado IN_ROUTE antes de confirmar.
     *
     * @param deliveryId ID de la entrega a confirmar
     * @return Delivery con estado DELIVERED y deliveredAt registrado
     * @throws ResourceNotFoundException si el delivery no existe
     * @throws BusinessException si el delivery no está en estado IN_ROUTE
     */
    @Override
    public Delivery completeDelivery(Long deliveryId) {
        log.info("HU-10: Confirmando entrega con ID: {}", deliveryId);

        // 1. Obtener la entrega (lanza 404 si no existe)
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Entrega con ID %d no encontrada", deliveryId)));

        // 2. Validar estado IN_ROUTE
        if (delivery.getStatus() != Delivery.DeliveryStatus.IN_ROUTE) {
            log.warn("HU-10: El delivery {} tiene estado {} — se requiere IN_ROUTE", deliveryId, delivery.getStatus());
            throw new BusinessException(
                    String.format("Solo se puede confirmar una entrega en estado IN_ROUTE. Estado actual: %s",
                            delivery.getStatus()));
        }

        // 3. Registrar fecha/hora de entrega
        LocalDateTime deliveredAt = LocalDateTime.now();

        // 4. Actualizar estado del Delivery → DELIVERED + deliveredAt
        deliveryRepository.updateStatusAndDeliveredAt(deliveryId, Delivery.DeliveryStatus.DELIVERED, deliveredAt);
        log.info("HU-10: Delivery {} marcado como DELIVERED a las {}", deliveryId, deliveredAt);

        // 5. Actualizar estado del Order asociado → DELIVERED + deliveredAt
        if (delivery.getOrderId() != null) {
            orderRepository.updateStatusAndDeliveredAt(
                    delivery.getOrderId(), Order.OrderStatus.DELIVERED, deliveredAt);
            log.info("HU-10: Order {} actualizado a DELIVERED a las {}", delivery.getOrderId(), deliveredAt);
        } else {
            log.warn("HU-10: El delivery {} no tiene orderId asociado — no se actualizó el pedido", deliveryId);
        }

        // 6. Retornar el objeto actualizado
        return Delivery.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(delivery.getAssignedAt())
                .deliveredAt(deliveredAt)
                .build();
    }

    
    /**
     * HU-11: Obtiene todas las entregas activas del repartidor
     * Las entregas activas son las que no han sido entregadas aún
     */
    @Override
    public List<Delivery> getActiveDeliveries(Long deliveryPersonId) {
        log.info("HU-11: Obteniendo entregas activas para repartidor ID: {}", deliveryPersonId);
        
        List<Delivery> activeDeliveries = deliveryRepository.findActiveByDeliveryPersonId(deliveryPersonId);
        
        log.info("HU-11: Se encontraron {} entregas activas para el repartidor {}", 
                activeDeliveries.size(), deliveryPersonId);
        return activeDeliveries;
    }
    
    /**
     * HU-11: Obtiene una entrega si pertenece al repartidor propietario
     * Valida la propiedad para evitar acceso no autorizado
     */
    @Override
    public Delivery getDeliveryIfOwner(Long deliveryId, Long deliveryPersonId) {
        log.info("HU-11: Validando propiedad de entrega {} para repartidor {}", deliveryId, deliveryPersonId);
        
        // Buscar la entrega por ID
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Entrega con ID %d no encontrada", deliveryId)
                ));
        
        // Validar que pertenece al repartidor
        if (!delivery.getDeliveryPersonId().equals(deliveryPersonId)) {
            log.warn("HU-11: Acceso denegado - Entrega {} no pertenece al repartidor {}", 
                    deliveryId, deliveryPersonId);
            throw new BusinessException(
                    String.format("La entrega %d no pertenece al repartidor %d", deliveryId, deliveryPersonId)
            );
        }
        
        log.info("HU-11: Validación exitosa - Aceso autorizado a entrega {}", deliveryId);
        return delivery;
    }
}