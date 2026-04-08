package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.shared.infrastructure.api.ApiEnvelope;
import edu.escuelaing.arsw.medigo.shared.infrastructure.api.TraceIdResolver;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceConflictException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = SedeAdminController.class)
@Slf4j
public class SedeExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiEnvelope<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String traceId = traceId(request);
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("traceId={} error de validacion: {}", traceId, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorEnvelope("Payload invalido", errors, traceId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        String traceId = traceId(request);
        log.warn("traceId={} bad request: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorEnvelope(ex.getMessage(), null, traceId));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        String traceId = traceId(request);
        log.warn("traceId={} not found: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorEnvelope(ex.getMessage(), null, traceId));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiEnvelope<Void>> handleConflict(
            ResourceConflictException ex,
            HttpServletRequest request) {

        String traceId = traceId(request);
        log.warn("traceId={} conflict: {}", traceId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorEnvelope(ex.getMessage(), null, traceId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiEnvelope<Void>> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        String traceId = traceId(request);
        log.error("traceId={} error interno en sedes", traceId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorEnvelope("Error interno procesando sedes", null, traceId));
    }

    private String traceId(HttpServletRequest request) {
        return TraceIdResolver.resolve(request.getHeader("X-Trace-Id"));
    }

    private <T> ApiEnvelope<T> errorEnvelope(String message, T data, String traceId) {
        return ApiEnvelope.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .traceId(traceId)
                .apiVersion("v1")
                .timestamp(Instant.now().toString())
                .build();
    }
}
