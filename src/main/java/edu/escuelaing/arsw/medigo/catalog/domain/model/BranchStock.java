package edu.escuelaing.arsw.medigo.catalog.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class BranchStock {
    private Long branchId;
    private Long medicationId;
    private int quantity;

    public Long getBranchId() {
        return branchId;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public int getQuantity() {
        return quantity;
    }
}