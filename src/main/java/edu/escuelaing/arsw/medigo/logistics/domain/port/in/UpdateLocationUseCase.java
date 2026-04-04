package edu.escuelaing.arsw.medigo.logistics.domain.port.in;
import edu.escuelaing.arsw.medigo.logistics.domain.model.LocationUpdate;
public interface UpdateLocationUseCase {
    void updateLocation(LocationUpdate location);
}