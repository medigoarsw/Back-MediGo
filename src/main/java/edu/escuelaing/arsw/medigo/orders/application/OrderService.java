package edu.escuelaing.arsw.medigo.orders.application;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
public class OrderService implements CreateOrderUseCase, ConfirmOrderUseCase {
    private final OrderRepositoryPort orderRepository;
    @Override @Transactional
    public Order createOrder(Long affiliateId, Long branchId, Double lat, Double lng, List<OrderItemRequest> items) {
        throw new UnsupportedOperationException("TODO Miguel");
    }
    @Override @Transactional
    public Order confirmOrder(Long orderId) {
        throw new UnsupportedOperationException("TODO Miguel: SELECT FOR UPDATE SKIP LOCKED");
    }
}