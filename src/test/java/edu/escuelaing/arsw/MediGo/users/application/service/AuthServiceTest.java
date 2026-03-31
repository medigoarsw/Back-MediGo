package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración del Servicio AuthService (Capa de Aplicación)
 * 
 * ✅ Mockea el puerto UserRepositoryPort (abstracción del adaptador)
 * ✅ Prueba casos de uso sin tocar BD real
 * ✅ Verifica interacciones con el repositorio
 * 
 * IMPORTANTE: NO mockeamos la BD, mockeamos el PUERTO (abstracción)
 * Esto permite cambiar el adaptador sin cambiar los tests
 */
@DisplayName("AuthService - Casos de Uso de Autenticación")
class AuthServiceTest {
    
    @Mock
    private UserRepositoryPort userRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        // Inicializa los mocks de Mockito
        MockitoAnnotations.openMocks(this);
        // Crea el servicio con el puerto mockeado
        authService = new AuthService(userRepository);
    }
    
    @Test
    @DisplayName("Debe autenticar usuario con credenciales válidas")
    void testAuthenticateSuccess() {
        // ARRANGE - Preparar datos
        User user = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        when(userRepository.findByUsername("student"))
            .thenReturn(Optional.of(user));
        
        // ACT - Ejecutar caso de uso
        User result = authService.authenticate("student", "123");
        
        // ASSERT - Verificar resultado
        assertNotNull(result);
        assertEquals("student", result.getUsername());
        // Verificar que se llamó al puerto
        verify(userRepository).findByUsername("student");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void testAuthenticateUserNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () -> {
            authService.authenticate("nonexistent", "123");
        });
        
        // Verificar que se buscó el usuario
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción con contraseña incorrecta")
    void testAuthenticateInvalidPassword() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        when(userRepository.findByUsername("student"))
            .thenReturn(Optional.of(user));
        
        // ACT & ASSERT
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate("student", "wrongpassword");
        });
        
        // El usuario fue buscado
        verify(userRepository).findByUsername("student");
    }
    
    @Test
    @DisplayName("Debe obtener usuario por email desde el repositorio")
    void testFindByEmail() {
        // ARRANGE
        User user = User.create(2L, "admin", "admin@example.com", "password", Role.ADMIN);
        when(userRepository.findByEmail("admin@example.com"))
            .thenReturn(Optional.of(user));
        
        // ACT
        Optional<User> result = userRepository.findByEmail("admin@example.com");
        
        // ASSERT
        assertTrue(result.isPresent());
        assertEquals("admin@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("admin@example.com");
    }
    
    @Test
    @DisplayName("Debe retornar vacío cuando email no existe")
    void testFindByEmailNotFound() {
        // ARRANGE
        when(userRepository.findByEmail("nonexistent@example.com"))
            .thenReturn(Optional.empty());
        
        // ACT
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");
        
        // ASSERT
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe autenticar múltiples usuarios diferentes")
    void testAuthenticateMultipleUsers() {
        // ARRANGE - Diferentes usuarios con diferentes roles
        User student = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        User admin = User.create(2L, "admin", "admin@example.com", "456", Role.ADMIN);
        User vendor = User.create(3L, "vendor", "vendor@example.com", "789", Role.VENDOR);
        
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("vendor")).thenReturn(Optional.of(vendor));
        
        // ACT & ASSERT
        User studentResult = authService.authenticate("student", "123");
        User adminResult = authService.authenticate("admin", "456");
        User vendorResult = authService.authenticate("vendor", "789");
        
        assertEquals(Role.STUDENT, studentResult.getRole());
        assertEquals(Role.ADMIN, adminResult.getRole());
        assertEquals(Role.VENDOR, vendorResult.getRole());
    }
}
