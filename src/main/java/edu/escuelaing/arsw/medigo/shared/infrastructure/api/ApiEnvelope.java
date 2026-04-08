package edu.escuelaing.arsw.medigo.shared.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEnvelope<T> {
    private boolean success;
    private String message;
    private T data;
    private String traceId;
    private String apiVersion;
    private String timestamp;
}
