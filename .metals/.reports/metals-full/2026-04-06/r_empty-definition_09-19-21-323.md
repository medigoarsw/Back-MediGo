error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/LoginRequestDto.java:lombok/Getter#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/LoginRequestDto.java
empty definition using pc, found symbol in pc: lombok/Getter#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 302
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/LoginRequestDto.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.@@Getter;
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
    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Schema(description = "Email del usuario", example = "student@medigo.com")
    private String email;
    
    @NotBlank(message = "Password es requerido")
    @Size(min = 1, max = 255, message = "Password debe tener entre 1 y 255 caracteres")
    @Schema(description = "Contraseña del usuario", example = "123")
    private String password;
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: lombok/Getter#