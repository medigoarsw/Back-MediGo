package edu.escuelaing.arsw.medigo.logistics.domain.port.in;

import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import java.util.List;

/**
 * HU-11: Repartidor presiona botón de finalización al entregar
 * 
 * Puerto de entrada que define los casos de uso para obtener entregas activas
 * del repartidor. Permite verificar la propiedad de una entrega.
 */
public interface GetActiveDeliveriesUseCase {
    
    /**
     * Obtiene todas las entregas activas asignadas a un repartidor específico.
     * 
     * Las entregas activas son aquellas con estado IN_ROUTE, ASSIGNED o PENDING_SHIPPING.
     * 
     * @param deliveryPersonId ID del repartidor
     * @return Lista de entregas activas del repartidor
     */
    List<Delivery> getActiveDeliveries(Long deliveryPersonId);
    
    /**
     * Obtiene una entrega específica si pertenece al repartidor indicado.
     * 
     * Valida la propiedad de la entrega para evitar acceso no autorizado.
     * 
     * @param deliveryId ID de la entrega
     * @param deliveryPersonId ID del repartidor (propietario)
     * @return La entrega si el repartidor es el propietario
     * @throws edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException 
     *         si la entrega no existe o no pertenece al repartidor
     */
    Delivery getDeliveryIfOwner(Long deliveryId, Long deliveryPersonId);
}
