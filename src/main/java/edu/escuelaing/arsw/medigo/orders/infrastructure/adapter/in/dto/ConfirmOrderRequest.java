package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para confirmar pedido con dirección de envío.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmOrderRequest {

    @NotBlank(message = "La calle es obligatoria")
    @Schema(description = "Calle de envío", example = "Calle 10")
    private String street;

    @NotBlank(message = "El número es obligatorio")
    @Schema(description = "Número de calle", example = "50-20")
    private String streetNumber;

    @NotBlank(message = "La ciudad es obligatoria")
    @Schema(description = "Ciudad de envío", example = "Bogotá")
    private String city;

    @NotBlank(message = "La comuna es obligatoria")
    @Schema(description = "Comuna/Barrio", example = "Centro")
    private String commune;

    @Schema(description = "Latitud para ubicación (opcional)", example = "4.7110")
    private Double latitude;

    @Schema(description = "Longitud para ubicación (opcional)", example = "-74.0721")
    private Double longitude;
}
