error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/entity/DeliveryEntity.java:_empty_/Column#nullable#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/entity/DeliveryEntity.java
empty definition using pc, found symbol in pc: _empty_/Column#nullable#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 393
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/entity/DeliveryEntity.java
text:
```scala
package edu.escuelaing.arsw.medigo.logistics.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "deliveries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", @@nullable = false, unique = true) private Long orderId;
    @Column(name = "delivery_person_id") private Long deliveryPersonId;
    @Column(nullable = false) private String status;
    @Column(name = "assigned_at") private LocalDateTime assignedAt;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Column#nullable#