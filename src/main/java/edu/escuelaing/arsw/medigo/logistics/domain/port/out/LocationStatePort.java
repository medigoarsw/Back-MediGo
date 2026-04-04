package edu.escuelaing.arsw.medigo.logistics.domain.port.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
import java.util.Optional;
public interface LocationStatePort {
    void saveLocation(LocationUpdate location);
    Optional<LocationUpdate> getLocation(Long deliveryId);
    void publishLocationUpdate(LocationUpdate location);
}