package edu.escuelaing.arsw.medigo.logistics.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Logistics Domain - Pruebas unitarias")
class LogisticsDomainTest {

    @Test
    void testDeliveryModel() {
        Delivery delivery = Delivery.builder()
            .id(1L)
            .orderId(100L)
            .deliveryPersonId(200L)
            .status(Delivery.DeliveryStatus.DELIVERED)
            .assignedAt(LocalDateTime.now())
            .build();
            
        assertThat(delivery.getOrderId()).isEqualTo(100L);
        assertThat(delivery.getDeliveryPersonId()).isEqualTo(200L);
        assertThat(delivery.getStatus()).isEqualTo(Delivery.DeliveryStatus.DELIVERED);
    }

    @Test
    void testLocationUpdateModel() {
        LocationUpdate update = LocationUpdate.builder()
            .deliveryId(1L)
            .lat(4.6097)
            .lng(-74.0817)
            .timestamp(System.currentTimeMillis())
            .build();
            
        assertThat(update.getDeliveryId()).isEqualTo(1L);
        assertThat(update.getLat()).isEqualTo(4.6097);
    }
}
