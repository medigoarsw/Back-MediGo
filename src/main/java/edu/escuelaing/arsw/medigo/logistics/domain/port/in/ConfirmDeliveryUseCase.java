package edu.escuelaing.arsw.medigo.logistics.domain.port.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;

/**
 * Puerto de entrada para confirmar/completar la entrega
 * HU-10: Actualización automática a estado "Entregado"
 */
public interface ConfirmDeliveryUseCase {
    
    /**
     * Confirma la entrega de un pedido
     * Cambia el estado del pedido a DELIVERED y registra la fecha/hora de entrega
     * 
     * @param deliveryId ID de la entrega a confirmar
     * @return Delivery con estado actualizado a DELIVERED
     * @throws ResourceNotFoundException si la entrega no existe
     * @throws BusinessException si la entrega no está en estado IN_ROUTE
     */
    Delivery confirmDelivery(Long deliveryId);
}
