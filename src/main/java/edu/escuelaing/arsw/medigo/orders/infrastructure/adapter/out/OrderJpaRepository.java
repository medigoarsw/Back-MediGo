package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.orders.infrastructure.entity.OrderEntity;
import edu.escuelaing.arsw.medigo.orders.infrastructure.persistence.SpringOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderJpaRepository implements OrderRepositoryPort {

    private final SpringOrderJpaRepository springRepo;

    @Override
    public Order save(Order order) {
        return toDomain(springRepo.save(toEntity(order)));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Order> findByAffiliateId(Long affiliateId) {
        return springRepo.findByAffiliateId(affiliateId).stream().map(this::toDomain).toList();
    }

    @Override
    public void updateStatus(Long orderId, Order.OrderStatus status) {
        springRepo.findById(orderId).ifPresent(e -> {
            e.setStatus(status.name());
            springRepo.save(e);
        });
    }

    @Override
    public Optional<Order> findByAuctionId(Long auctionId) {
        return springRepo.findByAuctionId(auctionId).map(this::toDomain);
    }

    @Override
    public List<Order> findPendingPaymentCreatedBefore(LocalDateTime cutoff) {
        return springRepo.findPendingPaymentCreatedBefore(cutoff)
                .stream().map(this::toDomain).toList();
    }

    // ── Mappers ───────────────────────────────────────────────────

    private OrderEntity toEntity(Order o) {
        return OrderEntity.builder()
                .id(o.getId())
                .affiliateId(o.getAffiliateId())
                .branchId(o.getBranchId())
                .auctionId(o.getAuctionId())
                .finalPrice(o.getFinalPrice())
                .status(o.getStatus().name())
                .addressLat(o.getAddressLat())
                .addressLng(o.getAddressLng())
                .createdAt(o.getCreatedAt())
                .build();
    }

    private Order toDomain(OrderEntity e) {
        return Order.builder()
                .id(e.getId())
                .affiliateId(e.getAffiliateId())
                .branchId(e.getBranchId())
                .auctionId(e.getAuctionId())
                .finalPrice(e.getFinalPrice())
                .status(Order.OrderStatus.valueOf(e.getStatus()))
                .addressLat(e.getAddressLat())
                .addressLng(e.getAddressLng())
                .createdAt(e.getCreatedAt())
                .items(Collections.emptyList())
                .build();
    }
}
