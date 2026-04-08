package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para crear una sede")
public class SedeRequest {

    @NotBlank(message = "nombre es requerido")
    @Size(max = 100, message = "nombre no puede superar 100 caracteres")
    @Schema(example = "Sede Centro")
    private String nombre;

    @NotBlank(message = "direccion es requerida")
    @Size(max = 200, message = "direccion no puede superar 200 caracteres")
    @Schema(example = "Calle 10 # 5-20")
    private String direccion;

    @NotBlank(message = "especialidad es requerida")
    @Size(max = 100, message = "especialidad no puede superar 100 caracteres")
    @Schema(example = "Medicina General")
    private String especialidad;

    @Pattern(regexp = "^$|^\\+?[0-9()\\-\\s]{7,20}$", message = "telefono con formato invalido")
    @Schema(example = "+57 300 123 4567")
    private String telefono;

    @Min(value = 0, message = "capacidad debe ser mayor o igual a 0")
    @Max(value = 100000, message = "capacidad fuera de rango")
    @Schema(example = "120")
    private Integer capacidad;
}
