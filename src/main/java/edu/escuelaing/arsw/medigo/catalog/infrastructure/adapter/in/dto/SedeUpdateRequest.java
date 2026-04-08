package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request para actualizar una sede. PUT con semantica parcial")
public class SedeUpdateRequest {

    @Size(max = 100, message = "nombre no puede superar 100 caracteres")
    @Schema(example = "Sede Norte")
    private String nombre;

    @Size(max = 200, message = "direccion no puede superar 200 caracteres")
    @Schema(example = "Cra 7 # 120-45")
    private String direccion;

    @Size(max = 100, message = "especialidad no puede superar 100 caracteres")
    @Schema(example = "Pediatria")
    private String especialidad;

    @Pattern(regexp = "^$|^\\+?[0-9()\\-\\s]{7,20}$", message = "telefono con formato invalido")
    @Schema(example = "+57 310 000 1111")
    private String telefono;

    @Min(value = 0, message = "capacidad debe ser mayor o igual a 0")
    @Max(value = 100000, message = "capacidad fuera de rango")
    @Schema(example = "180")
    private Integer capacidad;
}
