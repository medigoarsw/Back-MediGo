package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class OrderJpaRepository implements OrderRepositoryPort {
    @Override public Order save(Order order) { return order; }
    @Override public Optional<Order> findById(Long id) { return Optional.empty(); }
    @Override public List<Order> findByAffiliateId(Long affiliateId) { return List.of(); }
    @Override public void updateStatus(Long orderId, Order.OrderStatus status) {}
}