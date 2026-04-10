package edu.escuelaing.arsw.medigo.logistics.application;

import edu.escuelaing.arsw.medigo.catalog.domain.port.out.BranchRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.domain.model.ActiveDeliveryDetails;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.AssignDeliveryUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.GetActiveDeliveriesUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.UpdateLocationUseCase;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LocationStatePort;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsService implements UpdateLocationUseCase, AssignDeliveryUseCase, GetActiveDeliveriesUseCase {
    
    private final LocationStatePort locationState;
    private final DeliveryRepositoryPort deliveryRepository;
    private final OrderRepositoryPort orderRepository;
    private final BranchRepositoryPort branchRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void updateLocation(LocationUpdate location) {
        log.info("Actualizando ubicación para entrega {}: [{}, {}]", 
                location.getDeliveryId(), location.getLat(), location.getLng());
        locationState.saveLocation(location);
        locationState.publishLocationUpdate(location);
    }
    
    @Override
    @Transactional
    public Delivery assignDelivery(Long orderId, Long deliveryPersonId) {
        log.info("Asignando pedido {} al repartidor {}", orderId, deliveryPersonId);
        
        return deliveryRepository.findByOrderId(orderId)
                .orElseGet(() -> {
                    orderRepository.updateStatus(orderId, Order.OrderStatus.ASSIGNED);

                    orderRepository.findById(orderId).ifPresent(order -> {
                        // Notificar al afiliado que su pedido fue tomado
                        messagingTemplate.convertAndSend("/topic/orders/" + order.getAffiliateId(),
                            Map.of(
                                "id", orderId,
                                "status", "ASSIGNED",
                                "driverId", deliveryPersonId,
                                "message", "¡Un repartidor ha tomado tu pedido!")
                        );
                    });

                    // Notificar a todos los repartidores que este pedido ya no está disponible
                    messagingTemplate.convertAndSend("/topic/available-orders",
                        Map.of("id", orderId, "action", "remove")
                    );

                    Delivery delivery = Delivery.builder()
                            .orderId(orderId)
                            .deliveryPersonId(deliveryPersonId)
                            .status(Delivery.DeliveryStatus.ASSIGNED)
                            .assignedAt(java.time.LocalDateTime.now())
                            .build();

                    return deliveryRepository.save(delivery);
                });
    }
    
    @Override
    @Transactional
    public Delivery pickupDelivery(Long deliveryId) {
        log.info("Marcando entrega {} como en ruta", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        
        deliveryRepository.updateStatus(deliveryId, Delivery.DeliveryStatus.IN_ROUTE);
        orderRepository.updateStatus(delivery.getOrderId(), Order.OrderStatus.IN_ROUTE);

        final Long driverIdForNotif = delivery.getDeliveryPersonId();
        orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
            messagingTemplate.convertAndSend("/topic/orders/" + order.getAffiliateId(),
                Map.of("id", order.getId(), "status", "IN_ROUTE",
                       "driverId", driverIdForNotif,
                       "message", "El repartidor ya recogió tu pedido y va en camino a entregarlo")
            );
        });
        
        return Delivery.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(Delivery.DeliveryStatus.IN_ROUTE)
                .assignedAt(delivery.getAssignedAt())
                .build();
    }

    @Override
    @Transactional
    public Delivery completeDelivery(Long deliveryId) {
        log.info("Finalizando entrega {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        
        deliveryRepository.updateStatus(deliveryId, Delivery.DeliveryStatus.DELIVERED);
        
        final Long driverIdComplete = delivery.getDeliveryPersonId();
        orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
            messagingTemplate.convertAndSend("/topic/orders/" + order.getAffiliateId(),
                Map.of("id", order.getId(), "status", "DELIVERED",
                       "driverId", driverIdComplete,
                       "message", "¡Tu pedido ha llegado!")
            );
        });
        
        return Delivery.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .deliveryPersonId(delivery.getDeliveryPersonId())
                .status(Delivery.DeliveryStatus.DELIVERED)
                .assignedAt(delivery.getAssignedAt())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Delivery> getActiveDeliveries(Long deliveryPersonId) {
        return deliveryRepository.findActiveByDeliveryPersonId(deliveryPersonId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveDeliveryDetails> getActiveDeliveryDetails(Long deliveryPersonId) {
        return deliveryRepository.findActiveByDeliveryPersonId(deliveryPersonId).stream()
                .map(delivery -> {
                    ActiveDeliveryDetails.ActiveDeliveryDetailsBuilder builder = ActiveDeliveryDetails.builder()
                            .id(delivery.getId())
                            .orderId(delivery.getOrderId())
                            .deliveryPersonId(delivery.getDeliveryPersonId())
                            .status(delivery.getStatus())
                            .assignedAt(delivery.getAssignedAt());

                    orderRepository.findById(delivery.getOrderId()).ifPresent(order -> {
                        builder.deliveryLat(order.getAddressLat())
                               .deliveryLng(order.getAddressLng())
                               .deliveryAddress(buildAddress(order));

                        branchRepository.findById(order.getBranchId()).ifPresent(branch -> {
                            builder.pickupLat(branch.getLatitude())
                                   .pickupLng(branch.getLongitude())
                                   .branchName(branch.getName());
                        });
                    });

                    return builder.build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAffiliateDashboard(Long affiliateId) {
        log.info("HU-06: Buscando estado logístico para afiliado {}", affiliateId);
        
        List<Order> activeOrders = orderRepository.findByAffiliateId(affiliateId).stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED
                        || o.getStatus() == Order.OrderStatus.ASSIGNED
                        || o.getStatus() == Order.OrderStatus.IN_ROUTE)
                .toList();

        if (activeOrders.isEmpty()) {
            log.info("HU-06: No se encontraron pedidos activos para el afiliado {}", affiliateId);
            return null;
        }

        if (activeOrders.size() > 1) {
            log.warn("HU-06: El afiliado {} tiene {} pedidos activos. Mostrando el más reciente.", affiliateId, activeOrders.size());
        }

        // Tomar el más reciente por ID (o fecha si estuviera disponible de forma confiable)
        Order active = activeOrders.get(activeOrders.size() - 1);

        Map<String, Object> result = new HashMap<>();
        result.put("id", active.getId());
        result.put("orderNumber", active.getOrderNumber());
        result.put("status", active.getStatus().name());
        
        String message = switch (active.getStatus()) {
            case CONFIRMED -> "Tu pedido está esperando a ser aceptado por un repartidor";
            case ASSIGNED  -> "¡Un repartidor aceptó tu pedido y va en camino a recogerlo!";
            case IN_ROUTE  -> "El repartidor ya recogió tu pedido y va en camino a entregarlo";
            default        -> "Pedido en proceso";
        };
        result.put("message", message);

        deliveryRepository.findByOrderId(active.getId())
                .ifPresent(d -> {
                    result.put("driverId", d.getDeliveryPersonId());
                    log.info("HU-06: Pedido {} tiene repartidor {} asignado", active.getId(), d.getDeliveryPersonId());
                });

        return result;
    }

    private String buildAddress(Order order) {
        StringBuilder sb = new StringBuilder();
        if (order.getStreet() != null) sb.append(order.getStreet()).append(" ");
        if (order.getStreetNumber() != null) sb.append(order.getStreetNumber()).append(", ");
        if (order.getCity() != null) sb.append(order.getCity());
        return sb.toString().trim().replaceAll(", $", "");
    }

    @Override
    @Transactional(readOnly = true)
    public Delivery getDeliveryIfOwner(Long deliveryId, Long deliveryPersonId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        if (!delivery.getDeliveryPersonId().equals(deliveryPersonId)) {
            throw new BusinessException("La entrega no pertenece al repartidor");
        }
        return delivery;
    }
}
