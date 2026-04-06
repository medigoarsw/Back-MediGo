error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/SignUpRequestDto.java:_empty_/Getter#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/SignUpRequestDto.java
empty definition using pc, found symbol in pc: _empty_/Getter#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 124
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/SignUpRequestDto.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@@@Getter
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
    
    @NotBlank(message = "El rol es requerido")
    private String role;
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Getter#