package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.LocationStatePort;
import org.springframework.stereotype.Component;
import java.util.Optional;
@Component
public class RedisLocationAdapter implements LocationStatePort {
    // TODO Anderson: clave delivery:location:{id} TTL 60s
    @Override public void saveLocation(LocationUpdate location) {}
    @Override public Optional<LocationUpdate> getLocation(Long deliveryId) { return Optional.empty(); }
    @Override public void publishLocationUpdate(LocationUpdate location) {}
}