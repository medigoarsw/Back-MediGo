package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta de confirmación de pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmOrderResponse {

    @Schema(description = "Número único del pedido", example = "ORD-2024-001234")
    private String orderNumber;

    @Schema(description = "Total del pedido", example = "125.50")
    private BigDecimal totalPrice;

    @Schema(description = "Estado del pedido", example = "PENDING_SHIPPING")
    private String status;

    @Schema(description = "Calle de envío", example = "Calle 10")
    private String street;

    @Schema(description = "Número de calle", example = "50-20")
    private String streetNumber;

    @Schema(description = "Ciudad", example = "Bogotá")
    private String city;

    @Schema(description = "Comuna", example = "Centro")
    private String commune;

    @Schema(description = "Porcentaje de descuento aplicado", example = "10")
    private Integer discountPercentage;

    @Schema(description = "Precio con descuento", example = "112.95")
    private BigDecimal discountedPrice;

    @Schema(description = "Fecha de creación", example = "2024-04-02T15:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Resumen de los items del pedido")
    private List<OrderItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        @Schema(description = "ID del medicamento", example = "1")
        private Long medicationId;

        @Schema(description = "Nombre del medicamento", example = "Paracetamol 500mg")
        private String medicationName;

        @Schema(description = "Cantidad", example = "2")
        private Integer quantity;

        @Schema(description = "Precio unitario", example = "25.00")
        private BigDecimal unitPrice;

        @Schema(description = "Subtotal del item", example = "50.00")
        private BigDecimal subtotal;
    }
}
