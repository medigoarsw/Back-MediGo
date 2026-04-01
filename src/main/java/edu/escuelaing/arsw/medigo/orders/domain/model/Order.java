package edu.escuelaing.arsw.medigo.orders.domain.model;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class Order {
    private Long id;
    private Long affiliateId;
    private Long branchId;
    private Long auctionId;        // link a la subasta (null para pedidos normales)
    private BigDecimal finalPrice; // precio final de adjudicación (null para pedidos normales)
    private OrderStatus status;
    private Double addressLat;
    private Double addressLng;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
    public enum OrderStatus { PENDING, CONFIRMED, ASSIGNED, IN_ROUTE, DELIVERED, CANCELLED, PENDING_PAYMENT }
}