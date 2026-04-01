package edu.escuelaing.arsw.medigo.shared.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO estandarizado para respuestas de error
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private String errorCode;
    private String path;
    private LocalDateTime timestamp;
    private String details;
}
