package edu.escuelaing.arsw.medigo.orders.domain.model;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @Builder @AllArgsConstructor
public class Order {
    private Long id;
    private String orderNumber;          // número único de pedido (ej: ORD-2024-001234)
    private Long affiliateId;
    private Long branchId;
    private Long auctionId;              // link a la subasta (null para pedidos normales)
    private BigDecimal finalPrice;       // precio final de adjudicación (null para pedidos normales)
    private BigDecimal totalPrice;       // total del carrito (suma de items)
    private OrderStatus status;
    private String street;               // calle de envío
    private String streetNumber;         // número de calle
    private String city;                 // ciudad
    private String commune;              // comuna/barrio
    private Double addressLat;
    private Double addressLng;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
    public enum OrderStatus { PENDING, CONFIRMED, PENDING_SHIPPING, ASSIGNED, IN_ROUTE, DELIVERED, CANCELLED, PENDING_PAYMENT }
    
    public BigDecimal calculateTotalPrice() {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}