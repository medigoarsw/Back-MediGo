# 🧪 Ejemplos de Testing

## Test Unitario del Dominio

```java
package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    /**
     * Test de lógica pura del dominio
     * SIN dependencias externas, SIN BD, SIN Spring
     */
    @Test
    void testUserCredentialsMatchSuccess() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "password123", Role.STUDENT);
        
        // ACT
        boolean result = user.credentialsMatch("password123");
        
        // ASSERT
        assertTrue(result, "Las credenciales debe coincidir");
    }
    
    @Test
    void testUserCredentialsMatchFailure() {
        User user = User.create(1L, "student", "student@example.com", "password123", Role.STUDENT);
        
        boolean result = user.credentialsMatch("wrongpassword");
        
        assertFalse(result, "Las credenciales no deben coincidir");
    }
    
    @Test
    void testInactiveUserCannotLogin() {
        User user = new User(1L, "student@example.com", "password123", "student", Role.STUDENT, false);
        
        boolean result = user.credentialsMatch("password123");
        
        assertFalse(result, "Usuario inactivo no puede autenticarse");
    }
    
    @Test
    void testUserRole() {
        User admin = User.create(1L, "admin", "admin@example.com", "password", Role.ADMIN);
        
        assertEquals("ROLE_ADMIN", admin.getAuthority());
    }
}
```

## Test de Integración del Service

```java
package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    
    @Mock
    private UserRepositoryPort userRepository;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userRepository);
    }
    
    /**
     * Test de caso de uso: login exitoso
     */
    @Test
    void testAuthenticateSuccess() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        when(userRepository.findByUsername("student"))
            .thenReturn(Optional.of(user));
        
        // ACT
        User result = authService.authenticate("student", "123");
        
        // ASSERT
        assertNotNull(result);
        assertEquals("student", result.getUsername());
        verify(userRepository).findByUsername("student");
    }
    
    /**
     * Test de caso de uso: usuario no existe
     */
    @Test
    void testAuthenticateUserNotFound() {
        // ARRANGE
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());
        
        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () -> {
            authService.authenticate("nonexistent", "123");
        });
    }
    
    /**
     * Test de caso de uso: contraseña incorrecta
     */
    @Test
    void testAuthenticateInvalidPassword() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        when(userRepository.findByUsername("student"))
            .thenReturn(Optional.of(user));
        
        // ACT & ASSERT
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.authenticate("student", "wrongpassword");
        });
    }
}
```

## Test de Integración del Controller

```java
package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.medigo.users.application.dto.LoginRequestDto;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthUseCase authUseCase;
    
    @Autowired
    private ObjectMapper mapper;
    
    /**
     * Test de endpoint: POST /api/auth/login exitoso
     */
    @Test
    void testLoginSuccess() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("student", "123");
        User user = User.create(1L, "student", "student@example.com", "123", Role.STUDENT);
        
        when(authUseCase.authenticate("student", "123"))
            .thenReturn(user);
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.user_id").value(1))
            .andExpect(jsonPath("$.username").value("student"))
            .andExpect(jsonPath("$.role").value("student"));
    }
    
    /**
     * Test de endpoint: POST /api/auth/login fallido
     */
    @Test
    void testLoginUnauthorized() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("student", "wrong");
        
        when(authUseCase.authenticate("student", "wrong"))
            .thenThrow(new InvalidCredentialsException("Bad credentials"));
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## ✅ Ejecutar los tests

```bash
# Todos los tests
mvn test

# Tests de un módulo específico
mvn test -Dtest=UserTest

# Tests con coverage
mvn jacoco:report

# Tests en modo watch
mvn test -Dtest=AuthServiceTest -Dtest.skip=false
```

---

## 📊 Cobertura de Tests

La arquitectura hexagonal facilita tests a diferentes niveles:

| Capa | Tipo Test | Cómo |
|------|-----------|------|
| **Domain** | Unit | Sin mocks, lógica pura |
| **Application** | Unit | Mock UserRepositoryPort |
| **Infrastructure** | Integration | @WebMvcTest, MockMvc |
| **E2E** | Integration | @SpringBootTest full context |

