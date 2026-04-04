package edu.escuelaing.arsw.medigo.orders.domain.port.in;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import java.util.List;
public interface CreateOrderUseCase {
    Order createOrder(Long affiliateId, Long branchId, Double lat, Double lng, List<OrderItemRequest> items);
    record OrderItemRequest(Long medicationId, int quantity) {}
}