error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/model/Delivery.java:_empty_/Getter#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/model/Delivery.java
empty definition using pc, found symbol in pc: _empty_/Getter#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 112
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/model/Delivery.java
text:
```scala
package edu.escuelaing.arsw.medigo.logistics.domain.model;
import lombok.*;
import java.time.LocalDateTime;
@@@Getter @Builder @AllArgsConstructor
public class Delivery {
    private Long id;
    private Long orderId;
    private Long deliveryPersonId;
    private DeliveryStatus status;
    private LocalDateTime assignedAt;
    public enum DeliveryStatus { ASSIGNED, IN_ROUTE, DELIVERED }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Getter#