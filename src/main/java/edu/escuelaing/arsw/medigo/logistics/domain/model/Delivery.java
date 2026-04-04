package edu.escuelaing.arsw.medigo.logistics.domain.model;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Builder @AllArgsConstructor
public class Delivery {
    private Long id;
    private Long orderId;
    private Long deliveryPersonId;
    private DeliveryStatus status;
    private LocalDateTime assignedAt;
    public enum DeliveryStatus { ASSIGNED, IN_ROUTE, DELIVERED }
}