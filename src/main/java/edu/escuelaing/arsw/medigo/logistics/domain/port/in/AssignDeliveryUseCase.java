package edu.escuelaing.arsw.medigo.logistics.domain.port.in;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
public interface AssignDeliveryUseCase {
    Delivery assignDelivery(Long orderId, Long deliveryPersonId);
    Delivery pickupDelivery(Long deliveryId);
    Delivery completeDelivery(Long deliveryId);
}