package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionOrderPort;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderAuctionAdapter implements AuctionOrderPort {

    private final OrderRepositoryPort orderRepository;

    @Override
    public Long createAuctionOrder(Long auctionId, Long winnerId, Long medicationId,
                                   BigDecimal finalPrice, Long branchId) {
        Order order = Order.builder()
                .affiliateId(winnerId)
                .branchId(branchId)
                .auctionId(auctionId)
                .finalPrice(finalPrice)
                .status(Order.OrderStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order).getId();
    }

    @Override
    public Optional<String> getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId).map(o -> o.getStatus().name());
    }

    @Override
    public void cancelOrder(Long orderId) {
        orderRepository.updateStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    @Override
    public Optional<Long> findOrderIdByAuction(Long auctionId) {
        return orderRepository.findByAuctionId(auctionId).map(Order::getId);
    }

    @Override
    public List<ExpiredAuctionOrder> findExpiredPendingOrders(LocalDateTime cutoff) {
        return orderRepository.findPendingPaymentCreatedBefore(cutoff).stream()
                .filter(o -> o.getAuctionId() != null)
                .map(o -> new ExpiredAuctionOrder(o.getId(), o.getAuctionId(), o.getAffiliateId()))
                .toList();
    }
}
