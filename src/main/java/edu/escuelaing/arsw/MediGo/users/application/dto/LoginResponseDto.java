package edu.escuelaing.arsw.medigo.users.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de Salida para el login
 * 
 * Representa la respuesta que enviamos al cliente después de un login exitoso
 * 
 * NOTA: En producción, aquí iría el JWT real generado
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación exitosa con token de acceso")
public class LoginResponseDto {
    
    @JsonProperty("access_token")
    @Schema(description = "Token de acceso (JWT)", example = "fake-jwt.1.student.1774978129358")
    private String accessToken;  // En MVP es fake, en producción sería JWT
    
    @JsonProperty("token_type")
    @Schema(description = "Tipo de token", example = "Bearer")
    private String tokenType;    // Usually "Bearer"
    
    @JsonProperty("user_id")
    @Schema(description = "ID del usuario autenticado", example = "1")
    private Long userId;
    
    @Schema(description = "Nombre de usuario", example = "student")
    private String username;
    
    @Schema(description = "Email del usuario", example = "student@medigo.com")
    private String email;
    
    @Schema(description = "Rol del usuario", example = "STUDENT")
    private String role;
    
    @JsonProperty("expires_in")
    @Schema(description = "Tiempo de expiración del token en segundos", example = "3600")
    private Long expiresIn;      // Segundos hasta que expire el token
}
