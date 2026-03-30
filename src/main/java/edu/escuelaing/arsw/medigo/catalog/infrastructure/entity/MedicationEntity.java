package edu.escuelaing.arsw.medigo.catalog.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "medications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    private String description;
    private String unit;
}