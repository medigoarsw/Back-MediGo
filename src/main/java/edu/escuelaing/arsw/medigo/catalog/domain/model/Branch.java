package edu.escuelaing.arsw.medigo.catalog.domain.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
