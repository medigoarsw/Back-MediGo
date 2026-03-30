package edu.escuelaing.arsw.medigo.logistics.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class LocationUpdate {
    private Long deliveryId;
    private Double lat;
    private Double lng;
    private Long timestamp;
}