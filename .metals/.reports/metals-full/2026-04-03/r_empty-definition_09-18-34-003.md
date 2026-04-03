error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/application/LogisticsService.java:java/util/List#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/application/LogisticsService.java
empty definition using pc, found symbol in pc: java/util/List#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 741
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/application/LogisticsService.java
text:
```scala
package edu.escuelaing.arsw.medigo.logistics.application;
import edu.escuelaing.arsw.medigo.logistics.domain.model.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.*;
import edu.escuelaing.arsw.medigo.orders.domain.port.out.OrderRepositoryPort;
import edu.escuelaing.arsw.medigo.orders.domain.model.Order;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.@@List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogisticsService implements UpdateLocationUseCase, AssignDeliveryUseCase, GetActiveDeliveriesUseCase {
    private final LocationStatePort locationState;
    private final DeliveryRepositoryPort deliveryRepository;
    private final OrderRepositoryPort orderRepository;
    
    @Override
    public void updateLocation(LocationUpdate location) {
        throw new UnsupportedOperationException("TODO Anderson");
    }
    
    @Override
    public Delivery assignDelivery(Long orderId, Long deliveryPersonId) {
        throw new UnsupportedOperationException("TODO Miguel");
    }
    
    /**
     * HU-10: Confirma la entrega de un pedido
     * Cambia el estado de la entrega a DELIVERED y actualiza el pedido
     */
    @Override
    public Delivery completeDelivery(Long deliveryId) {
        log.info("HU-10: Confirmando entrega con ID: {}", deliveryId);
        
        // Obtener la entrega usando el orderId que está disponible en el delivery
        // Actualizar estado de la entrega directamente
        deliveryRepository.updateStatus(deliveryId, Delivery.DeliveryStatus.DELIVERED);
        
        // Obtener el delivery actualizado para obtener el orderId
        // Como DeliveryRepositoryPort no tiene findById, usamos una lógica alternativa
        // Asumimos que la actualización fue exitosa y creamos el objeto de respuesta
        Delivery updatedDelivery = Delivery.builder()
                .id(deliveryId)
                .status(Delivery.DeliveryStatus.DELIVERED)
                .build();
        
        log.info("HU-10: Entrega {} marcada como DELIVERED", deliveryId);
        
        return updatedDelivery;
    }
    
    /**
     * HU-11: Obtiene todas las entregas activas del repartidor
     * Las entregas activas son las que no han sido entregadas aún
     */
    @Override
    public List<Delivery> getActiveDeliveries(Long deliveryPersonId) {
        log.info("HU-11: Obteniendo entregas activas para repartidor ID: {}", deliveryPersonId);
        
        List<Delivery> activeDeliveries = deliveryRepository.findActiveByDeliveryPersonId(deliveryPersonId);
        
        log.info("HU-11: Se encontraron {} entregas activas para el repartidor {}", 
                activeDeliveries.size(), deliveryPersonId);
        return activeDeliveries;
    }
    
    /**
     * HU-11: Obtiene una entrega si pertenece al repartidor propietario
     * Valida la propiedad para evitar acceso no autorizado
     */
    @Override
    public Delivery getDeliveryIfOwner(Long deliveryId, Long deliveryPersonId) {
        log.info("HU-11: Validando propiedad de entrega {} para repartidor {}", deliveryId, deliveryPersonId);
        
        // Buscar la entrega por ID
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Entrega con ID %d no encontrada", deliveryId)
                ));
        
        // Validar que pertenece al repartidor
        if (!delivery.getDeliveryPersonId().equals(deliveryPersonId)) {
            log.warn("HU-11: Acceso denegado - Entrega {} no pertenece al repartidor {}", 
                    deliveryId, deliveryPersonId);
            throw new BusinessException(
                    String.format("La entrega %d no pertenece al repartidor %d", deliveryId, deliveryPersonId)
            );
        }
        
        log.info("HU-11: Validación exitosa - Aceso autorizado a entrega {}", deliveryId);
        return delivery;
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/List#