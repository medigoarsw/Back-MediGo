package edu.escuelaing.arsw.medigo.orders.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Domain - Pruebas unitarias")
class OrderDomainTest {

    @Test
    void calculateTotalPrice_sumsItemsSubtotals() {
        OrderItem item1 = OrderItem.builder().unitPrice(BigDecimal.valueOf(100)).quantity(2).build(); // subtotal 200
        OrderItem item2 = OrderItem.builder().unitPrice(BigDecimal.valueOf(50)).quantity(3).build();  // subtotal 150
        
        Order order = Order.builder()
            .items(List.of(item1, item2))
            .build();
            
        assertThat(order.calculateTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(350));
    }

    @Test
    void calculateTotalPrice_emptyItems() {
        Order empty = Order.builder().items(List.of()).build();
        assertThat(empty.calculateTotalPrice()).isEqualTo(BigDecimal.ZERO);
        
        Order nullItems = Order.builder().items(null).build();
        assertThat(nullItems.calculateTotalPrice()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void orderItem_subtotal() {
        OrderItem item = OrderItem.builder().unitPrice(BigDecimal.valueOf(100)).quantity(5).build();
        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(500));
        
        OrderItem free = OrderItem.builder().unitPrice(BigDecimal.ZERO).quantity(10).build();
        assertThat(free.getSubtotal()).isEqualTo(BigDecimal.ZERO);
    }
}
