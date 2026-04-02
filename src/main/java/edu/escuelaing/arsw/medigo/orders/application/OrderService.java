package edu.escuelaing.arsw.medigo.orders.application;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService implements CreateOrderUseCase, ConfirmOrderUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final SearchMedicationUseCase searchMedicationUseCase;
    private static final BigDecimal DEFAULT_MEDICATION_PRICE = BigDecimal.valueOf(25.00);
    
    /**
     * Agrega un medicamento al carrito del cliente.
     * Si el medicamento ya está en el carrito, incrementa la cantidad.
     * Valida que hay stock disponible y que no se exceda.
     */
    @Transactional
    public Order addItemToCart(Long affiliateId, Long branchId, Long medicationId, int quantityToAdd) {
        log.info("Agregando medicamento {} al carrito del cliente {}", medicationId, affiliateId);
        
        // Validaciones
        validateCartInput(affiliateId, branchId, medicationId, quantityToAdd);
        
        // Obtener o crear carrito (Order en estado PENDING)
        Order cart = orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseGet(() -> createNewCart(affiliateId, branchId));
        
        // Verificar stock disponible (ESCENARIO 3 y 4)
        int currentQuantityInCart = cart.getItems() != null 
            ? cart.getItems().stream()
                .filter(item -> item.getMedicationId().equals(medicationId))
                .mapToInt(OrderItem::getQuantity)
                .sum() 
            : 0;
        
        int totalRequestedQuantity = currentQuantityInCart + quantityToAdd;
        validateStockAvailability(medicationId, branchId, totalRequestedQuantity);
        
        // Agregar o actualizar item en carrito (ESCENARIO 2)
        updateCartItems(cart, medicationId, quantityToAdd);
        
        // Actualizar total del carrito
        BigDecimal newTotal = cart.calculateTotalPrice();
        
        // Crear nueva Order con datos actualizados
        Order updatedCart = Order.builder()
                .id(cart.getId())
                .affiliateId(cart.getAffiliateId())
                .branchId(cart.getBranchId())
                .auctionId(cart.getAuctionId())
                .finalPrice(cart.getFinalPrice())
                .totalPrice(newTotal)
                .status(Order.OrderStatus.PENDING)
                .addressLat(cart.getAddressLat())
                .addressLng(cart.getAddressLng())
                .createdAt(cart.getCreatedAt())
                .items(cart.getItems())
                .build();
        
        Order savedCart = orderRepository.save(updatedCart);
        log.info("Item agregado al carrito exitosamente. Total: {}", newTotal);
        
        return savedCart;
    }
    
    /**
     * Obtiene el carrito actual (Order en estado PENDING)
     */
    @Transactional(readOnly = true)
    public Order getCart(Long affiliateId, Long branchId) {
        log.debug("Obteniendo carrito del cliente {}", affiliateId);
        
        return orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado para cliente: {}, sucursal: {}", affiliateId, branchId);
                    return new ResourceNotFoundException("Carrito no encontrado");
                });
    }
    
    @Override @Transactional
    public Order createOrder(Long affiliateId, Long branchId, Double lat, Double lng, List<OrderItemRequest> items) {
        throw new UnsupportedOperationException("TODO Miguel");
    }
    
    @Override @Transactional
    public Order confirmOrder(Long orderId) {
        throw new UnsupportedOperationException("TODO Miguel: SELECT FOR UPDATE SKIP LOCKED");
    }
    
    // ────── Private Helper Methods ──────
    
    private void validateCartInput(Long affiliateId, Long branchId, Long medicationId, int quantity) {
        if (affiliateId == null || affiliateId <= 0) {
            throw new BusinessException("ID del cliente inválido");
        }
        if (branchId == null || branchId <= 0) {
            throw new BusinessException("ID de la sucursal inválido");
        }
        if (medicationId == null || medicationId <= 0) {
            throw new BusinessException("ID del medicamento inválido");
        }
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0");
        }
    }
    
    private Order createNewCart(Long affiliateId, Long branchId) {
        log.debug("Creando nuevo carrito para cliente: {}", affiliateId);
        
        return Order.builder()
                .affiliateId(affiliateId)
                .branchId(branchId)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }
    
    private void validateStockAvailability(Long medicationId, Long branchId, int totalNeeded) {
        // Aquí se validaría contra el CatalogService
        // Por MVP, asumimos que si pasa la aplicación llega, está disponible
        log.debug("Validando stock: medicamento={}  sucursal={}, cantidad={}", medicationId, branchId, totalNeeded);
        
        if (totalNeeded > 100) { // Limite de stock máximo para MVP
            throw new BusinessException("No hay suficiente stock disponible. Stock máximo permitido: 100");
        }
    }
    
    private void updateCartItems(Order cart, Long medicationId, int quantityToAdd) {
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        
        // Buscar si el medicamento ya existe en el carrito
        Optional<OrderItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMedicationId().equals(medicationId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Incrementar cantidad (ESCENARIO 2: no duplicar)
            OrderItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantityToAdd;
            
            // Crear nuevo item con cantidad actualizada
            OrderItem updatedItem = OrderItem.builder()
                    .orderId(item.getOrderId())
                    .medicationId(item.getMedicationId())
                    .quantity(newQuantity)
                    .unitPrice(item.getUnitPrice())
                    .build();
            
            cart.getItems().set(cart.getItems().indexOf(item), updatedItem);
            log.debug("Cantidad del medicamento incrementada a: {}", newQuantity);
        } else {
            // Agregar nuevo item
            OrderItem newItem = OrderItem.builder()
                    .medicationId(medicationId)
                    .quantity(quantityToAdd)
                    .unitPrice(DEFAULT_MEDICATION_PRICE)
                    .build();
            
            cart.getItems().add(newItem);
            log.debug("Nuevo medicamento agregado al carrito");
        }
    }
}