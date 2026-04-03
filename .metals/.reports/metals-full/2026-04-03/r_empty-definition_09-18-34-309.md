error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/out/DeliveryJpaRepository.java:java/util/List#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/out/DeliveryJpaRepository.java
empty definition using pc, found symbol in pc: java/util/List#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 294
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/out/DeliveryJpaRepository.java
text:
```scala
package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.logistics.domain.model.Delivery;
import edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.@@List;
import java.util.Optional;
@Component
public class DeliveryJpaRepository implements DeliveryRepositoryPort {
    // TODO Miguel: Spring Data JPA
    @Override public Delivery save(Delivery delivery) { return delivery; }
    @Override public Optional<Delivery> findByOrderId(Long orderId) { return Optional.empty(); }
    @Override public Optional<Delivery> findById(Long id) { return Optional.empty(); }
    @Override public List<Delivery> findActiveByDeliveryPersonId(Long deliveryPersonId) { return List.of(); }
    @Override public void updateStatus(Long deliveryId, Delivery.DeliveryStatus status) {}
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/List#