error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/entity/BranchEntity.java:_empty_/Table#name#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/entity/BranchEntity.java
empty definition using pc, found symbol in pc: _empty_/Table#name#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 160
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/entity/BranchEntity.java
text:
```scala
package edu.escuelaing.arsw.medigo.catalog.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(@@name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "phone")
    private String phone;

    @Column(name = "capacity")
    private Integer capacity;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    private Double latitude;

    private Double longitude;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "branch_id")
    private List<BranchStockEntity> stocks;

    @PrePersist
    public void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Table#name#