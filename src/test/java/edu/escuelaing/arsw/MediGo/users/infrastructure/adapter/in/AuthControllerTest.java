package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.escuelaing.arsw.medigo.users.application.dto.LoginRequestDto;
import edu.escuelaing.arsw.medigo.users.application.dto.LoginResponseDto;
import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in.AuthController;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración del AuthController (Capa de Infraestructura - Adapter IN)
 * 
 * Prueba los endpoints REST sin cargar todo Spring
 * Mockea el caso de uso (puerto de entrada)
 * Verifica requests/responses HTTP
 * 
 * @WebMvcTest: Carga solo el contexto web necesario (sin BD, sin servicios extras)
 * @AutoConfigureMockMvc(addFilters = false): Deshabilita Spring Security para tests
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController - Endpoints de Autenticación REST")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthUseCase authUseCase;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 200 con credenciales válidas")
    void testLoginSuccess() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("user", "123");
        User user = User.create(1L, "user", "user@example.com", "123", Role.USER);
        
        when(authUseCase.authenticate("user", "123"))
            .thenReturn(user);
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.user_id").value(1))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.role").value("user"));
        
        // Verifica que se llamó al caso de uso
        verify(authUseCase).authenticate("user", "123");
    }
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 401 con credenciales inválidas")
    void testLoginUnauthorized() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("student", "wrongpassword");
        
        when(authUseCase.authenticate("student", "wrongpassword"))
            .thenThrow(new InvalidCredentialsException("Bad credentials"));
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 404 cuando usuario no existe")
    void testLoginUserNotFound() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("nonexistent", "123");
        
        when(authUseCase.authenticate("nonexistent", "123"))
            .thenThrow(new UserNotFoundException("User not found"));
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/auth/{id} debe retornar usuario por ID")
    void testGetUserById() throws Exception {
        // ARRANGE
        User user = User.create(1L, "user", "user@example.com", "123", Role.USER);
        
        when(authUseCase.getUserById(1L))
            .thenReturn(user);
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(1))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.email").value("user@example.com"));
    }
    
    @Test
    @DisplayName("GET /api/auth/email/{email} debe retornar usuario por email")
    void testGetUserByEmail() throws Exception {
        // ARRANGE
        User admin = User.create(2L, "admin", "admin@example.com", "456", Role.ADMIN);
        
        when(authUseCase.getUserByEmail("admin@example.com"))
            .thenReturn(admin);
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/email/admin@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(2))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("admin"));
    }
    
    @Test
    @DisplayName("POST /api/auth/login debe validar que username y password sean requeridos")
    void testLoginMissingCredentials() throws Exception {
        // ARRANGE - Request inválido sin password
        String invalidJson = "{\"username\":\"student\"}";
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/auth/login con repartidor debe retornar rol DELIVERY")
    void testLoginDeliveryRole() throws Exception {
        // ARRANGE
        LoginRequestDto request = new LoginRequestDto("delivery", "789");
        User delivery = User.create(3L, "delivery", "delivery@example.com", "789", Role.DELIVERY);
        
        when(authUseCase.authenticate("delivery", "789"))
            .thenReturn(delivery);
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("delivery"));
    }
}
