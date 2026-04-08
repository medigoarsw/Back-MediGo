package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request para crear una sede")
public class SedeRequest extends SedeUpdateRequest {

    @Override
    @NotBlank(message = "nombre es requerido")
    public String getNombre() {
        return super.getNombre();
    }

    @Override
    @NotBlank(message = "direccion es requerida")
    public String getDireccion() {
        return super.getDireccion();
    }

    @Override
    @NotBlank(message = "especialidad es requerida")
    public String getEspecialidad() {
        return super.getEspecialidad();
    }
}
