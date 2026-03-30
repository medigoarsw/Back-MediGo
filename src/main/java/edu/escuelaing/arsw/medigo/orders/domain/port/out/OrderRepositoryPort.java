package edu.escuelaing.arsw.medigo.orders.domain.port.out;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import java.util.*;
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByAffiliateId(Long affiliateId);
    void updateStatus(Long orderId, Order.OrderStatus status);
}