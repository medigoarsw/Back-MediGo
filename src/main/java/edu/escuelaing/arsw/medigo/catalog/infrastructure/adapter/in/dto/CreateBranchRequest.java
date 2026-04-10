package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear una nueva sucursal (Centro Médico)")
public class CreateBranchRequest {

    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    @Schema(description = "Nombre de la sucursal", example = "Clínica Central")
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección física", example = "Calle 100 # 15-20")
    private String address;

    @NotNull(message = "La latitud es obligatoria")
    @Min(value = -90, message = "La latitud debe estar entre -90 y 90")
    @Max(value = 90, message = "La latitud debe estar entre -90 y 90")
    @Schema(description = "Latitud para el mapa", example = "4.6097")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    @Min(value = -180, message = "La longitud debe estar entre -180 y 180")
    @Max(value = 180, message = "La longitud debe estar entre -180 y 180")
    @Schema(description = "Longitud para el mapa", example = "-74.0817")
    private Double longitude;
}
