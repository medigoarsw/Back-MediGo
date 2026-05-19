package edu.escuelaing.arsw.medigo.orders.infrastructure.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class OrderEntityTest {

    @Test
    void testOrderEntityBuilderAndGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        OrderItemEntity item = OrderItemEntity.builder()
                .id(1L)
                .medicationId(10L)
                .quantity(5)
                .unitPrice(new BigDecimal("10.00"))
                .build();

        OrderEntity entity = OrderEntity.builder()
                .id(123L)
                .orderNumber("ORD-123")
                .affiliateId(2L)
                .branchId(3L)
                .auctionId(5L)
                .finalPrice(new BigDecimal("45.00"))
                .totalPrice(new BigDecimal("50.00"))
                .status("PENDING")
                .street("Main St")
                .streetNumber("123")
                .city("Springfield")
                .commune("Central")
                .addressLat(4.0)
                .addressLng(-74.0)
                .createdAt(now)
                .deliveredAt(now)
                .items(new ArrayList<>(List.of(item)))
                .build();

        assertEquals(123L, entity.getId());
        assertEquals("ORD-123", entity.getOrderNumber());
        assertEquals(2L, entity.getAffiliateId());
        assertEquals(3L, entity.getBranchId());
        assertEquals(5L, entity.getAuctionId());
        assertEquals(new BigDecimal("45.00"), entity.getFinalPrice());
        assertEquals(new BigDecimal("50.00"), entity.getTotalPrice());
        assertEquals("PENDING", entity.getStatus());
        assertEquals("Main St", entity.getStreet());
        assertEquals("123", entity.getStreetNumber());
        assertEquals("Springfield", entity.getCity());
        assertEquals("Central", entity.getCommune());
        assertEquals(4.0, entity.getAddressLat());
        assertEquals(-74.0, entity.getAddressLng());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getDeliveredAt());
        assertNotNull(entity.getItems());
        assertEquals(1, entity.getItems().size());

        // Items Setters
        OrderItemEntity itemResponse = entity.getItems().get(0);
        assertEquals(1L, itemResponse.getId());
        assertEquals(10L, itemResponse.getMedicationId());
        assertEquals(5, itemResponse.getQuantity());
        assertEquals(new BigDecimal("10.00"), itemResponse.getUnitPrice());

        // Entity Setters
        entity.setId(456L);
        assertEquals(456L, entity.getId());
        entity.setOrderNumber("ORD-456");
        assertEquals("ORD-456", entity.getOrderNumber());
        entity.setAffiliateId(20L);
        assertEquals(20L, entity.getAffiliateId());
        entity.setBranchId(30L);
        assertEquals(30L, entity.getBranchId());
        entity.setAuctionId(50L);
        assertEquals(50L, entity.getAuctionId());
        entity.setFinalPrice(new BigDecimal("80.00"));
        assertEquals(new BigDecimal("80.00"), entity.getFinalPrice());
        entity.setTotalPrice(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), entity.getTotalPrice());
        entity.setStatus("DELIVERED");
        assertEquals("DELIVERED", entity.getStatus());
        entity.setStreet("Elm St");
        assertEquals("Elm St", entity.getStreet());
        entity.setStreetNumber("456");
        assertEquals("456", entity.getStreetNumber());
        entity.setCity("Shelbyville");
        assertEquals("Shelbyville", entity.getCity());
        entity.setCommune("North");
        assertEquals("North", entity.getCommune());
        entity.setAddressLat(5.0);
        assertEquals(5.0, entity.getAddressLat());
        entity.setAddressLng(-75.0);
        assertEquals(-75.0, entity.getAddressLng());
        LocalDateTime later = now.plusDays(1);
        entity.setCreatedAt(later);
        assertEquals(later, entity.getCreatedAt());
        entity.setDeliveredAt(later);
        assertEquals(later, entity.getDeliveredAt());

        // Item setter
        item.setOrder(entity);
        assertEquals(entity, item.getOrder());

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setId(2L);
        item2.setOrder(entity);
        item2.setMedicationId(20L);
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("25.00"));

        entity.setItems(List.of(item, item2));
        assertEquals(2, entity.getItems().size());
        assertEquals(entity, entity.getItems().get(1).getOrder());
    }
}
