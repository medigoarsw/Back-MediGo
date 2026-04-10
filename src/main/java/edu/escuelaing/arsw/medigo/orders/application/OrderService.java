package edu.escuelaing.arsw.medigo.orders.application;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest;
import edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.out.util.OrderNumberGenerator;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateStockUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService implements CreateOrderUseCase, ConfirmOrderUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final SearchMedicationUseCase searchMedicationUseCase;
    private final UpdateStockUseCase updateStockUseCase;
    private final SimpMessagingTemplate messagingTemplate;
    private static final BigDecimal DEFAULT_MEDICATION_PRICE = BigDecimal.valueOf(25.00);
    
    @Transactional
    public Order addItemToCart(Long affiliateId, Long branchId, Long medicationId, int quantityToAdd) {
        log.info("Agregando medicamento {} al carrito del cliente {}", medicationId, affiliateId);
        validateCartInput(affiliateId, branchId, medicationId, quantityToAdd);
        
        Order cart = orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseGet(() -> orderRepository.save(createNewCart(affiliateId, branchId)));
        
        updateCartItems(cart, medicationId, quantityToAdd);
        BigDecimal newTotal = cart.calculateTotalPrice();
        
        Order updatedCart = cart.toBuilder()
                .totalPrice(newTotal)
                .status(Order.OrderStatus.PENDING)
                .build();
        
        return orderRepository.save(updatedCart);
    }
    
    @Transactional(readOnly = true)
    public Order getCart(Long affiliateId, Long branchId) {
        return orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));
    }
    
    @Override @Transactional
    public Order createOrder(Long affiliateId, Long branchId, Double lat, Double lng, List<OrderItemRequest> items) {
        Order newOrder = Order.builder()
                .affiliateId(affiliateId)
                .branchId(branchId)
                .status(Order.OrderStatus.PENDING)
                .addressLat(lat)
                .addressLng(lng)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
        return orderRepository.save(newOrder);
    }
    
    @Override @Transactional
    public Order confirmOrder(Long orderId) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override @Transactional
    public Order confirmPendingOrder(Long affiliateId, Long branchId, ConfirmOrderRequest request) {
        log.info("Confirmando pedido para cliente {} en sucursal {}", affiliateId, branchId);
        
        Order cart = orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito no encontrado"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito está vacío. Agregue medicamentos antes de confirmar");
        }

        if (request.getStreet() == null || request.getStreet().isBlank()) {
            throw new BusinessException("La calle es obligatoria");
        }

        // Un afiliado solo puede tener un pedido activo a la vez
        boolean hasActiveOrder = orderRepository.findByAffiliateId(affiliateId).stream()
                .anyMatch(o -> o.getStatus() == Order.OrderStatus.CONFIRMED
                        || o.getStatus() == Order.OrderStatus.ASSIGNED
                        || o.getStatus() == Order.OrderStatus.IN_ROUTE);
        if (hasActiveOrder) {
            throw new BusinessException("Ya tienes un pedido activo. Espera a que sea entregado antes de hacer uno nuevo.");
        }
        
        Order confirmedOrder = cart.toBuilder()
                .orderNumber(OrderNumberGenerator.generateOrderNumber())
                .status(Order.OrderStatus.CONFIRMED)
                .street(request.getStreet())
                .streetNumber(request.getStreetNumber())
                .city(request.getCity())
                .commune(request.getCommune())
                .addressLat(request.getLatitude())
                .addressLng(request.getLongitude())
                .build();
        
        Order savedOrder = orderRepository.save(confirmedOrder);
        
        // NOTIFICACIÓN: Publicar pedido disponible para todos los repartidores
        try {
            messagingTemplate.convertAndSend("/topic/available-orders", Map.of(
                "id", savedOrder.getId(),
                "orderNumber", savedOrder.getOrderNumber(),
                "status", "AVAILABLE",
                "lat", savedOrder.getAddressLat() != null ? savedOrder.getAddressLat() : 4.711,
                "lng", savedOrder.getAddressLng() != null ? savedOrder.getAddressLng() : -74.072,
                "total", savedOrder.getTotalPrice()
            ));
            log.info("HU-06: Pedido {} publicado para mercado de repartidores", savedOrder.getId());
        } catch (Exception e) {
            log.error("Fallo al notificar disponibilidad: {}", e.getMessage());
        }
        
        reduceStockForOrder(branchId, savedOrder);
        
        orderRepository.save(createNewCart(affiliateId, branchId));
        
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public List<Order> getAvailableOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED)
                .collect(Collectors.toList());
    }
    
    private void validateCartInput(Long affiliateId, Long branchId, Long medicationId, int quantity) {
        if (medicationId != null && medicationId <= 0) {
            throw new BusinessException("ID del medicamento inválido");
        }
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0");
        }
        if (quantity > 100) {
            throw new BusinessException("No hay suficiente stock disponible. Stock máximo permitido: 100");
        }
        if (affiliateId == null || affiliateId <= 0 || branchId == null || medicationId == null) {
            throw new BusinessException("Datos de entrada inválidos");
        }
    }
    
    private Order createNewCart(Long affiliateId, Long branchId) {
        return Order.builder()
                .affiliateId(affiliateId)
                .branchId(branchId)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }
    
    private void updateCartItems(Order cart, Long medicationId, int quantityToAdd) {
        if (cart.getItems() == null) cart.setItems(new ArrayList<>());
        Optional<OrderItem> existing = cart.getItems().stream()
                .filter(item -> item.getMedicationId().equals(medicationId)).findFirst();
        
        if (existing.isPresent()) {
            OrderItem item = existing.get();
            cart.getItems().set(cart.getItems().indexOf(item), 
                item.toBuilder().quantity(item.getQuantity() + quantityToAdd).build());
        } else {
            cart.getItems().add(OrderItem.builder().medicationId(medicationId)
                .quantity(quantityToAdd).unitPrice(DEFAULT_MEDICATION_PRICE).build());
        }
    }
    
    private void reduceStockForOrder(Long branchId, Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                BranchStock stock = searchMedicationUseCase.getAvailabilityByMedicationBranch(item.getMedicationId(), branchId);
                if (stock != null) {
                    updateStockUseCase.updateStock(branchId, item.getMedicationId(), Math.max(0, stock.getQuantity() - item.getQuantity()));
                }
            } catch (Exception ignored) {}
        }
    }
}
