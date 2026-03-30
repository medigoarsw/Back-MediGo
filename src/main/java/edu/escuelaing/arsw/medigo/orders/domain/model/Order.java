package edu.escuelaing.arsw.medigo.orders.domain.model;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Builder @AllArgsConstructor
public class Order {
    private Long id;
    private Long affiliateId;
    private Long branchId;
    private OrderStatus status;
    private Double addressLat;
    private Double addressLng;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
    public enum OrderStatus { PENDING, CONFIRMED, ASSIGNED, IN_ROUTE, DELIVERED, CANCELLED }
}