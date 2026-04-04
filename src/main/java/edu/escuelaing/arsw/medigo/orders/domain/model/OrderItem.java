package edu.escuelaing.arsw.medigo.orders.domain.model;
import lombok.*;
import java.math.BigDecimal;
@Getter @Builder @AllArgsConstructor
public class OrderItem {
    private Long orderId;
    private Long medicationId;
    private int quantity;
    private BigDecimal unitPrice;  // Precio unitario al momento de agregar
    
    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}