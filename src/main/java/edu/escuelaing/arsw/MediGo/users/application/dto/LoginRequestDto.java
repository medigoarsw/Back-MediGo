package edu.escuelaing.arsw.medigo.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de Entrada para el login
 * 
 * DTO = Data Transfer Object
 * Representa los datos que el cliente envía en la solicitud de login
 * 
 * IMPORTANTE:
 * - El DTO no debe confundirse con el modelo de dominio
 * - El DTO es solo para transmisión de datos HTTP
 * - El modelo de dominio es para lógica de negocio
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales para autenticar un usuario")
public class LoginRequestDto {
    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    @Schema(description = "Nombre de usuario", example = "user")
    private String username;
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 1, max = 255, message = "Password debe tener entre 1 y 255 caracteres")
    @Schema(description = "Contraseña del usuario", example = "123")
    private String password;
}
