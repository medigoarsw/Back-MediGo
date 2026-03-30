package edu.escuelaing.arsw.medigo.orders.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class OrderItem {
    private Long orderId;
    private Long medicationId;
    private int quantity;
}