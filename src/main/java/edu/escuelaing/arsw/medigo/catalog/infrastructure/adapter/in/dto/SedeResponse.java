package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sede de MediGo")
public class SedeResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String especialidad;
    private String telefono;
    private Integer capacidad;

    @JsonProperty("name")
    public String getName() {
        return nombre;
    }

    @JsonProperty("address")
    public String getAddress() {
        return direccion;
    }

    @JsonProperty("specialization")
    public String getSpecialization() {
        return especialidad;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return telefono;
    }

    @JsonProperty("capacity")
    public Integer getCapacity() {
        return capacidad;
    }
}
