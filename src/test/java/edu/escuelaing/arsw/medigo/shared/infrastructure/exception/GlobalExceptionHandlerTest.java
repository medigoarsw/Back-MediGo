package edu.escuelaing.arsw.medigo.shared.infrastructure.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para el manejador global de excepciones
 */
@DisplayName("GlobalExceptionHandler - Manejo de excepciones")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/test/endpoint");
    }

    // ======================== BUSINESS EXCEPTION ========================

    @Test
    @DisplayName("Debería manejar BusinessException correctamente")
    void testHandleBusinessException() {
        // Arrange
        String errorMessage = "Validación de negocio fallida";
        BusinessException exception = new BusinessException(errorMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleBusinessException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    // ======================== RESOURCE NOT FOUND EXCEPTION ========================

    @Test
    @DisplayName("Debería manejar ResourceNotFoundException correctamente")
    void testHandleResourceNotFoundException() {
        // Arrange
        String errorMessage = "Recurso no encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage, "123");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleResourceNotFoundException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
    }

    // ======================== GENERIC EXCEPTION ========================

    @Test
    @DisplayName("Debería manejar excepciones genéricas")
    void testHandleGlobalException() {
        // Arrange
        Exception exception = new Exception("Error inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleGlobalException(exception, mockRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
    }

    // ======================== ERROR RESPONSE STRUCTURE ========================

    @Test
    @DisplayName("ErrorResponse debe incluir timestamp")
    void testErrorResponseIncludesTimestamp() {
        // Arrange
        BusinessException exception = new BusinessException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleBusinessException(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("ErrorResponse debe incluir path del request")
    void testErrorResponseIncludesPath() {
        // Arrange
        BusinessException exception = new BusinessException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleBusinessException(exception, mockRequest);

        // Assert
        assertNotNull(response.getBody().getPath());
        assertTrue(response.getBody().getPath().contains("/test/endpoint"));
    }
}
