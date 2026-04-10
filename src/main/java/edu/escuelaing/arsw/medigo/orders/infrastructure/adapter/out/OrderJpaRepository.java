package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.domain.model.OrderItem;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.orders.infrastructure.entity.OrderEntity;
import edu.escuelaing.arsw.medigo.orders.infrastructure.entity.OrderItemEntity;
import edu.escuelaing.arsw.medigo.orders.infrastructure.persistence.SpringOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Optional<Order> findPendingByAffiliateAndBranch(Long affiliateId, Long branchId) {
        return springRepo.findPendingByAffiliateAndBranch(affiliateId, branchId)
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    // ── Mappers ───────────────────────────────────────────────────

    private OrderEntity toEntity(Order o) {
        LocalDateTime now = LocalDateTime.now();
        
        OrderEntity entity = OrderEntity.builder()
                .id(o.getId())
                .affiliateId(o.getAffiliateId())
                .branchId(o.getBranchId())
                .auctionId(o.getAuctionId())
                .orderNumber(o.getOrderNumber())
                .finalPrice(o.getFinalPrice())
                .totalPrice(o.getTotalPrice() != null ? o.getTotalPrice() : BigDecimal.ZERO)
                .status(o.getStatus() != null ? o.getStatus().name() : "PENDING")
                .street(o.getStreet())
                .streetNumber(o.getStreetNumber())
                .city(o.getCity())
                .commune(o.getCommune())
                .addressLat(o.getAddressLat())
                .addressLng(o.getAddressLng())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt() : now)
                .build();
        
        // Mapear items
        if (o.getItems() != null && !o.getItems().isEmpty()) {
            List<OrderItemEntity> itemEntities = o.getItems().stream()
                    .map(item -> OrderItemEntity.builder()
                            .order(entity)
                            .medicationId(item.getMedicationId())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .build())
                    .toList();
            entity.setItems(itemEntities);
        } else {
            entity.setItems(new ArrayList<>());
        }
        
        return entity;
    }

    private Order toDomain(OrderEntity e) {
        // Mapear items de la BD
        List<OrderItem> items = new ArrayList<>();
        if (e.getItems() != null && !e.getItems().isEmpty()) {
            items = e.getItems().stream()
                    .map(itemEntity -> OrderItem.builder()
                            .orderId(e.getId())
                            .medicationId(itemEntity.getMedicationId())
                            .quantity(itemEntity.getQuantity())
                            .unitPrice(itemEntity.getUnitPrice())
                            .build())
                    .toList();
        }
        
        return Order.builder()
                .id(e.getId())
                .orderNumber(e.getOrderNumber())
                .affiliateId(e.getAffiliateId())
                .branchId(e.getBranchId())
                .auctionId(e.getAuctionId())
                .finalPrice(e.getFinalPrice())
                .totalPrice(e.getTotalPrice())
                .status(Order.OrderStatus.valueOf(e.getStatus()))
                .street(e.getStreet())
                .streetNumber(e.getStreetNumber())
                .city(e.getCity())
                .commune(e.getCommune())
                .addressLat(e.getAddressLat())
                .addressLng(e.getAddressLng())
                .createdAt(e.getCreatedAt())
                .items(new ArrayList<>(items))  // ArrayList mutable para poder modificar
                .build();
    }
    @Override
    public List<Order> findAll() {
        return springRepo.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
