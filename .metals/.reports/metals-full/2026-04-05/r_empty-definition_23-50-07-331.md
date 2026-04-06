error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java:_empty_/Role#AFFILIATE#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java
empty definition using pc, found symbol in pc: _empty_/Role#AFFILIATE#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3750
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.application.service.AuthService;
import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidInputException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserAlreadyExistsException;
import edu.escuelaing.arsw.medigo.users.application.dto.SignUpRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración del Servicio AuthService (Capa de Aplicación)
 * 
 * Mockea el puerto UserRepositoryPort (abstracción del adaptador)
 * Prueba casos de uso sin tocar BD real
 * Verifica interacciones con el repositorio
 * 
 * IMPORTANTE: NO mockeamos la BD, mockeamos el PUERTO (abstracción)
 * Esto permite cambiar el adaptador sin cambiar los tests
 */
@DisplayName("AuthService - Casos de Uso de Autenticación")
class AuthServiceTest {
    
    @Mock
    private UserRepositoryPort userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        // Inicializa los mocks de Mockito
        MockitoAnnotations.openMocks(this);
        // Crea el servicio con el puerto mockeado
        authService = new AuthService(userRepository, passwordEncoder);
    }
    
    @Test
    @DisplayName("Debe autenticar usuario con credenciales válidas")
    void testAuthenticateSuccess() {
        // ARRANGE - Preparar datos
        User user = User.create(1L, "user", "user@example.com", "123", Role.AFFILIATE);
        when(userRepository.findByEmail("user@example.com"))
            .thenReturn(Optional.of(user));
        
        // ACT - Ejecutar caso de uso
        User result = authService.authenticate("user@example.com", "123");
        
        // ASSERT - Verificar resultado
        assertNotNull(result);
        assertEquals("user", result.getUsername());
        // Verificar que se llamó al puerto
        verify(userRepository).findByEmail("user@example.com");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void testAuthenticateUserNotFound() {
        // ARRANGE
        when(userRepository.findByEmail("nonexistent@example.com"))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () -> {
            authService.authenticate("nonexistent@example.com", "123");
        });
        
        // Verificar que se buscó el usuario
        verify(userRepository).findByEmail("nonexistent@example.com");
    }
    
    @Test
    @DisplayName("Debe lanzar excepción con contraseña incorrecta")
    void testAuthenticateInvalidPassword() {
        // ARRANGE
        User user = User.create(1L, "user", "user@example.com", "123", Role.@@AFFILIATE);
        when(userRepository.findByEmail("user@example.com"))
            .thenReturn(Optional.of(user));
        
        // ACT & ASSERT
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate("user@example.com", "wrongpassword");
        });
        
        // El usuario fue buscado
        verify(userRepository).findByEmail("user@example.com");
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
        User user = User.create(1L, "user", "user@example.com", "123", Role.AFFILIATE);
        User admin = User.create(2L, "admin", "admin@example.com", "456", Role.ADMIN);
        User delivery = User.create(3L, "delivery", "delivery@example.com", "789", Role.DELIVERY);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(userRepository.findByEmail("delivery@example.com")).thenReturn(Optional.of(delivery));
        
        // ACT & ASSERT
        User userResult = authService.authenticate("user@example.com", "123");
        User adminResult = authService.authenticate("admin@example.com", "456");
        User deliveryResult = authService.authenticate("delivery@example.com", "789");
        
        assertEquals(Role.AFFILIATE, userResult.getRole());
        assertEquals(Role.ADMIN, adminResult.getRole());
        assertEquals(Role.DELIVERY, deliveryResult.getRole());
    }

    @Test
    @DisplayName("Debe registrar usuario nuevo satisfactoriamente")
    void testSignUpSuccess() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("newuser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setRole("AFFILIATE");
        
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        
        User savedUser = User.create(1L, "newuser", "newuser@example.com", "encodedPassword", Role.AFFILIATE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // ACT
        User result = authService.signUp(signUpRequest);
        
        // ASSERT
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Debe fallar cuando el username ya existe")
    void testSignUpUsernameTaken() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("existing");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setRole("AFFILIATE");
        
        User existingUser = User.create(1L, "existing", "existing@example.com", "pass", Role.AFFILIATE);
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existingUser));
        
        // ACT & ASSERT
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }

    @Test
    @DisplayName("Debe fallar cuando el email ya existe")
    void testSignUpEmailTaken() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("newuser");
        signUpRequest.setEmail("existing@example.com");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setRole("AFFILIATE");
        
        User existingUser = User.create(1L, "existinguser", "existing@example.com", "pass", Role.AFFILIATE);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        
        // ACT & ASSERT
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }

    @ParameterizedTest
    @DisplayName("SignUp debe fallar con datos inválidos")
    @CsvSource({
        "'', 'newuser@example.com', 'Password123!', 'AFFILIATE', 'username vacío'",
        "'newuser', 'invalid-email', 'Password123!', 'AFFILIATE', 'email inválido'",
        "'newuser', 'newuser@example.com', 'weak', 'AFFILIATE', 'contraseña débil'"
    })
    void testSignUpWithInvalidData(String name, String email, String password, String role, String reason) {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName(name);
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setRole(role);
        
        // ACT & ASSERT
        assertThrows(InvalidInputException.class, () -> {
            authService.signUp(signUpRequest);
        }, "Debe fallar cuando " + reason);
    }
}





```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Role#AFFILIATE#