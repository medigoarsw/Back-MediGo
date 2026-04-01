package edu.escuelaing.arsw.medigo.catalog.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branch_stock", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "medication_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "medication_id", nullable = false)
    private Long medicationId;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", insertable = false, updatable = false)
    private MedicationEntity medication;
}
