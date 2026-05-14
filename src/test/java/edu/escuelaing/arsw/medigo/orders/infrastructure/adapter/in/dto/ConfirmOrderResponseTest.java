package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ConfirmOrderResponseTest {

    @Test
    void testConfirmOrderResponseBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        ConfirmOrderResponse.OrderItemResponse item = ConfirmOrderResponse.OrderItemResponse.builder()
                .medicationId(1L)
                .medicationName("Paracetamol")
                .quantity(5)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("50.00"))
                .build();

        ConfirmOrderResponse response = ConfirmOrderResponse.builder()
                .orderNumber("ORD-123")
                .status("CONFIRMED")
                .totalPrice(new BigDecimal("50.00"))
                .street("Calle 1")
                .streetNumber("23")
                .city("Bogota")
                .commune("Chapinero")
                .discountPercentage(10)
                .discountedPrice(new BigDecimal("45.00"))
                .createdAt(now)
                .items(List.of(item))
                .build();

        assertEquals("ORD-123", response.getOrderNumber());
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(new BigDecimal("50.00"), response.getTotalPrice());
        assertEquals("Calle 1", response.getStreet());
        assertEquals("23", response.getStreetNumber());
        assertEquals("Bogota", response.getCity());
        assertEquals("Chapinero", response.getCommune());
        assertEquals(10, response.getDiscountPercentage());
        assertEquals(new BigDecimal("45.00"), response.getDiscountedPrice());
        assertEquals(now, response.getCreatedAt());

        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());

        ConfirmOrderResponse.OrderItemResponse itemResponse = response.getItems().get(0);
        assertEquals(1L, itemResponse.getMedicationId());
        assertEquals("Paracetamol", itemResponse.getMedicationName());
        assertEquals(5, itemResponse.getQuantity());
        assertEquals(new BigDecimal("10.00"), itemResponse.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), itemResponse.getSubtotal());

        // Test Setters
        response.setOrderNumber("ORD-456");
        assertEquals("ORD-456", response.getOrderNumber());

        response.setStatus("DELIVERED");
        assertEquals("DELIVERED", response.getStatus());

        LocalDateTime later = now.plusDays(1);
        response.setCreatedAt(later);
        assertEquals(later, response.getCreatedAt());

        response.setTotalPrice(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());

        ConfirmOrderResponse.OrderItemResponse item2 = new ConfirmOrderResponse.OrderItemResponse();
        item2.setMedicationId(2L);
        item2.setMedicationName("Ibuprofeno");
        item2.setQuantity(2);
        item2.setUnitPrice(new BigDecimal("25.00"));
        item2.setSubtotal(new BigDecimal("50.00"));

        response.setItems(List.of(item, item2));
        assertEquals(2, response.getItems().size());
    }
}
