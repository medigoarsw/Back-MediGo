package edu.escuelaing.arsw.medigo.catalog.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Branch {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
