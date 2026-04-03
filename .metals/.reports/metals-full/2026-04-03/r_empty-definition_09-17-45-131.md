error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/port/out/DeliveryRepositoryPort.java:java/util/Optional#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/port/out/DeliveryRepositoryPort.java
empty definition using pc, found symbol in pc: java/util/Optional#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 172
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/port/out/DeliveryRepositoryPort.java
text:
```scala
package edu.escuelaing.arsw.medigo.logistics.domain.port.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import java.util.List;
import java.util.@@Optional;
public interface DeliveryRepositoryPort {
    Delivery save(Delivery delivery);
    Optional<Delivery> findByOrderId(Long orderId);
    Optional<Delivery> findById(Long id);
    List<Delivery> findActiveByDeliveryPersonId(Long deliveryPersonId);
    void updateStatus(Long deliveryId, Delivery.DeliveryStatus status);
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/Optional#