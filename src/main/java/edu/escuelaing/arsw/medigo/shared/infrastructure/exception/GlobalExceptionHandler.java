package edu.escuelaing.arsw.medigo.shared.infrastructure.exception;

import edu.escuelaing.arsw.medigo.auction.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Subastas: recurso no encontrado (404) ─────────────────────────

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuctionNotFound(
            AuctionNotFoundException ex, WebRequest request) {
        log.warn("AuctionNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(),
                        "AUCTION_NOT_FOUND", request));
    }

    // ── Subastas: conflicto de estado o regla de negocio (409) ────────

    @ExceptionHandler({
        AuctionAlreadyExistsException.class,
        AuctionNotEditableException.class,
        AuctionClosedException.class,
        InvalidBidException.class,
        UserNotJoinedException.class,
        BidLockNotAcquiredException.class
    })
    public ResponseEntity<ErrorResponse> handleAuctionConflict(
            RuntimeException ex, WebRequest request) {
        log.warn("Auction conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(),
                        "AUCTION_CONFLICT", request));
    }

    // ── Subastas: datos de entrada inválidos (400) ────────────────────

    @ExceptionHandler(InvalidAuctionDatesException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuctionDates(
            InvalidAuctionDatesException ex, WebRequest request) {
        log.warn("InvalidAuctionDatesException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(),
                        "INVALID_AUCTION_DATES", request));
    }

    // ── Negocio genérico (400) ────────────────────────────────────────

    /**
     * Maneja excepciones de negocio (BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {

        log.warn("BusinessException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de recurso no encontrado
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.warn("ResourceNotFoundException: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .errorCode("RESOURCE_NOT_FOUND")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .details("Resource ID: " + ex.getResourceId())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de validación de argumentos (anotaciones @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .details(fieldErrors.toString())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja parámetros de request faltantes (@RequestParam)
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            WebRequest request) {

        log.warn("Missing parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Required parameter is missing: " + ex.getParameterName())
                .errorCode("MISSING_PARAMETER")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .details(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no controladas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .details(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // ── Helper ────────────────────────────────────────────────────────

    private ErrorResponse buildError(HttpStatus status, String message,
                                     String errorCode, WebRequest request) {
        return ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .errorCode(errorCode)
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
