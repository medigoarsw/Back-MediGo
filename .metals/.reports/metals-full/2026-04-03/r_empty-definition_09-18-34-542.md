error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/application/OrderService.java:_empty_/Order#getItems#stream#filter#findFirst#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/application/OrderService.java
empty definition using pc, found symbol in pc: _empty_/Order#getItems#stream#filter#findFirst#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 13254
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/application/OrderService.java
text:
```scala
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService implements CreateOrderUseCase, ConfirmOrderUseCase {
    
    private final OrderRepositoryPort orderRepository;
    private final SearchMedicationUseCase searchMedicationUseCase;
    private final UpdateStockUseCase updateStockUseCase;
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
        // Se busca de forma thread-safe para evitar crear múltiples carritos
        Order cart = orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseGet(() -> {
                    Order newCart = createNewCart(affiliateId, branchId);
                    return orderRepository.save(newCart);  // Guardar inmediatamente
                });
        
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
        log.info("Creando nueva orden para cliente {} en sucursal {}", affiliateId, branchId);
        
        // Validaciones
        if (affiliateId == null || affiliateId <= 0) {
            throw new BusinessException("affiliateId debe ser mayor a 0");
        }
        if (branchId == null || branchId <= 0) {
            throw new BusinessException("branchId debe ser mayor a 0");
        }
        
        // Crear nueva orden vacía
        LocalDateTime now = LocalDateTime.now();
        Order newOrder = Order.builder()
                .affiliateId(affiliateId)
                .branchId(branchId)
                .status(Order.OrderStatus.PENDING)
                .addressLat(lat)
                .addressLng(lng)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(now)
                .items(new ArrayList<>())
                .build();
        
        try {
            Order savedOrder = orderRepository.save(newOrder);
            log.info("Orden {} creada exitosamente para cliente {}", savedOrder.getId(), affiliateId);
            return savedOrder;
        } catch (Exception e) {
            log.error("Error al guardar la orden en la BD: {}", e.getMessage(), e);
            throw new BusinessException("Error al crear la orden: " + e.getMessage());
        }
    }
    
    @Override @Transactional
    public Order confirmOrder(Long orderId) {
        throw new UnsupportedOperationException("TODO Miguel: SELECT FOR UPDATE SKIP LOCKED");
    }
    
    /**
     * HU-06: Confirma el carrito pendiente con dirección de envío.
     * Genera número de orden, actualiza dirección, cambia estado a CONFIRMED.
     * 
     * Escenario 1: Confirmar pedido con dirección completa - genera número, actualiza dirección, estado CONFIRMED
     * Escenario 2: Intentar confirmar sin dirección completa - valida que NO deje confirmar
     * Escenario 3: Ver resumen antes de confirmar - muestra resumen con productos y precios
     * Escenario 4: Carrito se vacía después de confirmar - crea nueva orden PENDING vacía
     */
    @Override @Transactional
    public Order confirmPendingOrder(Long affiliateId, Long branchId, ConfirmOrderRequest request) {
        log.info("Confirmando pedido para cliente {} en sucursal {}", affiliateId, branchId);
        
        // Validar request (dirección completa)
        if (request == null || request.getStreet() == null || request.getStreet().isBlank()) {
            throw new BusinessException("La calle es obligatoria");
        }
        if (request.getStreetNumber() == null || request.getStreetNumber().isBlank()) {
            throw new BusinessException("El número es obligatorio");
        }
        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new BusinessException("La ciudad es obligatoria");
        }
        if (request.getCommune() == null || request.getCommune().isBlank()) {
            throw new BusinessException("La comuna es obligatoria");
        }
        
        // Obtener carrito pendiente
        Order cart = orderRepository.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .orElseThrow(() -> {
                    log.warn("Carrito no encontrado para cliente: {}, sucursal: {}", affiliateId, branchId);
                    return new ResourceNotFoundException("Carrito no encontrado");
                });
        
        // Validar que carrito NO esté vacío (Escenario 3)
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito está vacío. Agregue medicamentos antes de confirmar");
        }
        
        // Generar número de orden único (Escenario 1)
        String orderNumber = OrderNumberGenerator.generateOrderNumber();
        log.debug("Número de orden generado: {}", orderNumber);
        
        // Actualizar carrito con dirección y información de confirmación (Escenario 1)
        Order confirmedOrder = Order.builder()
                .id(cart.getId())
                .orderNumber(orderNumber)
                .affiliateId(cart.getAffiliateId())
                .branchId(cart.getBranchId())
                .auctionId(cart.getAuctionId())
                .finalPrice(cart.getFinalPrice())
                .totalPrice(cart.getTotalPrice())
                .status(Order.OrderStatus.CONFIRMED)  // Estado CONFIRMED
                .street(request.getStreet())
                .streetNumber(request.getStreetNumber())
                .city(request.getCity())
                .commune(request.getCommune())
                .addressLat(request.getLatitude())
                .addressLng(request.getLongitude())
                .createdAt(cart.getCreatedAt())
                .items(cart.getItems())
                .build();
        
        Order savedOrder = orderRepository.save(confirmedOrder);
        log.info("Pedido confirmado exitosamente. Número: {}", orderNumber);
        
        // Reducir stock en catálogo para cada medicamento del pedido
        try {
            reduceStockForOrder(branchId, savedOrder);
            log.info("Stock reducido exitosamente para pedido: {}", orderNumber);
        } catch (Exception e) {
            log.error("Error al reducir stock para pedido {}: {}", orderNumber, e.getMessage());
            // No lanzar excepción - el pedido ya está confirmado, solo logear el error
        }
        
        // Escenario 4: Crear nuevo carrito vacío para el cliente
        Order newPendingCart = Order.builder()
                .affiliateId(affiliateId)
                .branchId(branchId)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
        
        orderRepository.save(newPendingCart);
        log.debug("Carrito vacío creado para cliente: {}", affiliateId);
        
        return savedOrder;
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
                .@@findFirst();
        
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
    
    /**
     * Reduce el stock en el catálogo para todos los medicamentos del pedido confirmado.
     * Para cada medicamento: obtiene stock actual y resta la cantidad del pedido.
     * 
     * @param branchId ID de la sucursal
     * @param order Orden confirmada con items
     */
    private void reduceStockForOrder(Long branchId, Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            log.debug("No hay items para reducir stock");
            return;
        }
        
        for (OrderItem item : order.getItems()) {
            try {
                Long medicationId = item.getMedicationId();
                int quantityOrdered = item.getQuantity();
                
                log.debug("Reduciendo stock - Medicamento: {}, Cantidad ordenada: {}", medicationId, quantityOrdered);
                
                // Obtener stock actual en la sucursal
                BranchStock currentStock = searchMedicationUseCase.getAvailabilityByMedicationBranch(medicationId, branchId);
                
                if (currentStock == null) {
                    log.warn("Stock no encontrado para medicamento: {} en sucursal: {}", medicationId, branchId);
                    continue;
                }
                
                int currentQuantity = currentStock.getQuantity();
                int newQuantity = currentQuantity - quantityOrdered;
                
                if (newQuantity < 0) {
                    log.warn("Stock insuficiente para medicamento: {}. Actual: {}, Solicitado: {}", 
                        medicationId, currentQuantity, quantityOrdered);
                    newQuantity = 0; // No permitir stock negativo
                }
                
                // Actualizar stock en catálogo
                updateStockUseCase.updateStock(branchId, medicationId, newQuantity);
                log.info("Stock actualizado - Medicamento: {}, Sucursal: {}, Nuevo stock: {}", 
                    medicationId, branchId, newQuantity);
                
            } catch (Exception e) {
                log.error("Error al reducir stock para medicamento: {}: {}", item.getMedicationId(), e.getMessage());
                // No relanzar excepción - continuar con otros items
            }
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Order#getItems#stream#filter#findFirst#