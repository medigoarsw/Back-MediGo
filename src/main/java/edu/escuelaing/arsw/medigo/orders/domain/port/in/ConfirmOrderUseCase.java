package edu.escuelaing.arsw.medigo.orders.domain.port.in;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
public interface ConfirmOrderUseCase {
    Order confirmOrder(Long orderId);
}