package edu.escuelaing.arsw.medigo.logistics.application;
import edu.escuelaing.arsw.medigo.logistics.domain.model.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class LogisticsService implements UpdateLocationUseCase, AssignDeliveryUseCase {
    private final LocationStatePort locationState;
    private final DeliveryRepositoryPort deliveryRepository;
    @Override public void updateLocation(LocationUpdate location) { throw new UnsupportedOperationException("TODO Anderson"); }
    @Override public Delivery assignDelivery(Long orderId, Long deliveryPersonId) { throw new UnsupportedOperationException("TODO Miguel"); }
    @Override public Delivery completeDelivery(Long deliveryId) { throw new UnsupportedOperationException("TODO Anderson"); }
}