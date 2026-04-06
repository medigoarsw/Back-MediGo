package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.medigo.users.application.dto.LoginRequestDto;
import edu.escuelaing.arsw.medigo.users.application.dto.LoginResponseDto;
import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.config.TestSecurityConfig;
import edu.escuelaing.arsw.medigo.users.infrastructure.config.AuthConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración del AuthController (Capa de Infraestructura - Adapter IN)
 * 
 * Usa @SpringBootTest para cargar el contexto completo de Spring
 * Esto permite que AuthService esté disponible como un bean real
 * @AutoConfigureMockMvc: Proporciona MockMvc para hacer peticiones HTTP
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class, AuthConfig.class})
@ActiveProfiles("test")
@DisplayName("AuthController - Endpoints de Autenticación REST")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 200 con credenciales válidas")
    void testLoginSuccess() throws Exception {
        // ARRANGE - Usuario real del repositorio en memoria
        LoginRequestDto request = new LoginRequestDto("user@medigo.com", "123");
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.user_id").value(2))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.role").value("AFFILIATE"));
    }
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 401 con credenciales inválidas")
    void testLoginUnauthorized() throws Exception {
        // ARRANGE - Usuario que existe pero contraseña incorrecta
        LoginRequestDto request = new LoginRequestDto("user@medigo.com", "wrongpassword");
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /api/auth/login debe retornar 404 cuando usuario no existe")
    void testLoginUserNotFound() throws Exception {
        // ARRANGE - Usuario que no existe en el repositorio
        LoginRequestDto request = new LoginRequestDto("nonexistent@medigo.com", "123");
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/auth/{id} debe retornar usuario por ID")
    void testGetUserById() throws Exception {
        // ARRANGE - Usuario real: ID=2, username=user
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/2")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(2))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.email").value("user@medigo.com"));
    }
    
    @Test
    @DisplayName("GET /api/auth/email/{email} debe retornar usuario por email")
    void testGetUserByEmail() throws Exception {
        // ARRANGE - Usuario real: admin@medigo.com
        
        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/email/admin@medigo.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(1))
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
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
        // ARRANGE - Usuario delivery real
        LoginRequestDto request = new LoginRequestDto("delivery@medigo.com", "123");
        
        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("DELIVERY"));
    }

            @Test
            @DisplayName("POST /api/auth/register debe crear usuario con phone y timestamps")
            void testRegisterSuccessWithPhone() throws Exception {
            String requestJson = """
                {
                  "name": "nuevo",
                  "email": "nuevo@example.com",
                  "password": "Password123",
                  "phone": "+57-322-5555555",
                  "role": "AFFILIATE"
                }
                """;

            mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@example.com"))
                .andExpect(jsonPath("$.phone").value("+57-322-5555555"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
            }
}