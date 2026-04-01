package edu.escuelaing.arsw.medigo.users.application.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private String message;
}
