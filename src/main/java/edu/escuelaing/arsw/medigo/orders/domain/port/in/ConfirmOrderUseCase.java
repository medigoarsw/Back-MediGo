package edu.escuelaing.arsw.medigo.orders.domain.port.in;

import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto.ConfirmOrderRequest;

public interface ConfirmOrderUseCase {
    
    /**
     * Confirma un pedido existente.
     * @param orderId ID del pedido a confirmar
     * @return pedido confirmado
     */
    Order confirmOrder(Long orderId);
    
    /**
     * Confirma el carrito pendiente del cliente con dirección de envío.
     * @param affiliateId ID del cliente
     * @param branchId ID de la sucursal
     * @param request datos de confirmación con dirección de envío
     * @return pedido confirmado con número de orden generado
     */
    Order confirmPendingOrder(Long affiliateId, Long branchId, ConfirmOrderRequest request);
}