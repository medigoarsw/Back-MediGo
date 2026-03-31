error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java:_empty_/User#setId#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java
empty definition using pc, found symbol in pc: _empty_/User#setId#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 6988
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.service;

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
        User user = User.create(1L, "user", "user@example.com", "123", Role.USER);
        when(userRepository.findByUsername("user"))
            .thenReturn(Optional.of(user));
        
        // ACT - Ejecutar caso de uso
        User result = authService.authenticate("user", "123");
        
        // ASSERT - Verificar resultado
        assertNotNull(result);
        assertEquals("user", result.getUsername());
        // Verificar que se llamó al puerto
        verify(userRepository).findByUsername("user");
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
        User user = User.create(1L, "user", "user@example.com", "123", Role.USER);
        when(userRepository.findByUsername("user"))
            .thenReturn(Optional.of(user));
        
        // ACT & ASSERT
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate("user", "wrongpassword");
        });
        
        // El usuario fue buscado
        verify(userRepository).findByUsername("user");
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
        User user = User.create(1L, "user", "user@example.com", "123", Role.USER);
        User admin = User.create(2L, "admin", "admin@example.com", "456", Role.ADMIN);
        User delivery = User.create(3L, "delivery", "delivery@example.com", "789", Role.DELIVERY);
        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("delivery")).thenReturn(Optional.of(delivery));
        
        // ACT & ASSERT
        User userResult = authService.authenticate("user", "123");
        User adminResult = authService.authenticate("admin", "456");
        User deliveryResult = authService.authenticate("delivery", "789");
        
        assertEquals(Role.USER, userResult.getRole());
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
        signUpRequest.setRole("USER");
        
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.@@setId(1L);
            return user;
        });
        
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
        signUpRequest.setRole("USER");
        
        User existingUser = User.create(1L, "existing", "existing@example.com", "pass", Role.USER);
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
        signUpRequest.setRole("USER");
        
        User existingUser = User.create(1L, "existinguser", "existing@example.com", "pass", Role.USER);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        
        // ACT & ASSERT
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }

    @Test
    @DisplayName("Debe fallar con email inválido")
    void testSignUpInvalidEmail() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("newuser");
        signUpRequest.setEmail("invalid-email");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setRole("USER");
        
        // ACT & ASSERT
        assertThrows(InvalidInputException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }

    @Test
    @DisplayName("Debe fallar con contraseña débil")
    void testSignUpWeakPassword() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("newuser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("weak");
        signUpRequest.setRole("USER");
        
        // ACT & ASSERT
        assertThrows(InvalidInputException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }

    @Test
    @DisplayName("Debe fallar cuando username está vacío")
    void testSignUpEmptyUsername() {
        // ARRANGE
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("Password123!");
        signUpRequest.setRole("USER");
        
        // ACT & ASSERT
        assertThrows(InvalidInputException.class, () -> {
            authService.signUp(signUpRequest);
        });
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/User#setId#