error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/entity/OrderEntity.java:_empty_/Column#nullable#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/entity/OrderEntity.java
empty definition using pc, found symbol in pc: _empty_/Column#nullable#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 432
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/orders/infrastructure/entity/OrderEntity.java
text:
```scala
package edu.escuelaing.arsw.medigo.orders.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_number", unique = true, @@nullable = true, length = 50) private String orderNumber;
    @Column(name = "affiliate_id", nullable = false) private Long affiliateId;
    @Column(name = "branch_id", nullable = false) private Long branchId;
    @Column(name = "auction_id") private Long auctionId;
    @Column(name = "final_price", precision = 12, scale = 2) private BigDecimal finalPrice;
    @Column(name = "total_price", precision = 12, scale = 2) private BigDecimal totalPrice;
    @Column(nullable = false) private String status;
    @Column(name = "street", length = 255) private String street;
    @Column(name = "street_number", length = 50) private String streetNumber;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "commune", length = 100) private String commune;
    @Column(name = "address_lat") private Double addressLat;
    @Column(name = "address_lng") private Double addressLng;
    @Column(name = "created_at") private LocalDateTime createdAt;
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Column#nullable#