package edu.escuelaing.arsw.medigo.catalog.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class Medication {
    private Long id;
    private String name;
    private String description;
    private String unit;
}