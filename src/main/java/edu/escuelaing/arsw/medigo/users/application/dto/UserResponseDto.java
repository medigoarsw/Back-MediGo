package edu.escuelaing.arsw.medigo.users.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO con información del usuario autenticado
 * Útil para devolverlo en respuestas GET /me
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del usuario")
public class UserResponseDto {
    @JsonProperty("user_id")
    @Schema(description = "ID único del usuario", example = "1")
    private Long id;
    
    @Schema(description = "Nombre de usuario", example = "user")
    private String username;
    
    @Schema(description = "Email del usuario", example = "user@medigo.com")
    private String email;
    
    @Schema(description = "Rol del usuario", example = "USER")
    private String role;
    
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private boolean active;

    @Schema(description = "Dirección del usuario", example = "Calle 123 #45-67")
    private String address;
}
