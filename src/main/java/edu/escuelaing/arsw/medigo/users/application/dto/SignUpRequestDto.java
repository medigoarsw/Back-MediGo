package edu.escuelaing.arsw.medigo.users.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDto {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    private String password;

    @Pattern(regexp = "^$|^\\+\\d{1,3}-\\d{3}-\\d{7}$", message = "El teléfono debe tener formato +57-322-5555555")
    private String phone;
    
    @NotBlank(message = "El rol es requerido")
    private String role;
}
