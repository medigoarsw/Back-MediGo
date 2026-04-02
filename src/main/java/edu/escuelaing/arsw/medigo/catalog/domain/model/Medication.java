package edu.escuelaing.arsw.medigo.catalog.domain.model;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class Medication {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private BigDecimal price;  // Precio del medicamento (HU-07)
}