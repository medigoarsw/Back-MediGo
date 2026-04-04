package edu.escuelaing.arsw.medigo.catalog.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class BranchStock {
    private Long branchId;
    private Long medicationId;
    private int quantity;
}