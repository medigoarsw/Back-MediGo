package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.users.application.dto.LoginRequestDto;
import edu.escuelaing.arsw.medigo.users.application.dto.LoginResponseDto;
import edu.escuelaing.arsw.medigo.users.application.dto.UserResponseDto;
import edu.escuelaing.arsw.medigo.users.domain.exception.DomainException;
import edu.escuelaing.arsw.medigo.users.domain.exception.InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADAPTADOR DE ENTRADA: Controller REST
 * 
 * Este es un ADAPTADOR hexagonal:
 * - Recibe peticiones HTTP
 * - Convierte requests HTTP → comandos de aplicación
 * - Llama a AuthUseCase (puerto de entrada)
 * - Convierte respuestas de dominio → DTOs HTTP
 * - Maneja errores HTTP
 * 
 * 🔑 IMPORTANTE:
 * - El Controller NO contiene lógica de negocio
 * - Solo traduce entre HTTP y dominio
 * - Si mañana quieres GraphQL, solo creas otro @RestController sin tocar esto
 * 
 * ROUTA API:
 * POST /api/auth/login      → AuthController.login()
 * GET  /api/auth/me         → AuthController.getCurrentUser()
 * GET  /api/auth/{id}       → AuthController.getUserById()
 * GET  /api/auth/email/{email} → AuthController.getUserByEmail()
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API de autenticación y gestión de usuarios")
public class AuthController {
    
    // INYECCIÓN: AuthUseCase (puerto de entrada)
    // En realidad es AuthService que implementa este puerto
    private final AuthUseCase authUseCase;

    /**
     * POST /api/auth/login
     * 
     * Autentica un usuario con username y password
     */
    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuario",
        description = "Autentica un usuario con sus credenciales (username y password)",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            log.debug("Authentication request received");
            
            // PASO 1: Llamar al caso de uso (AuthService)
            User user = authUseCase.authenticate(
                request.getUsername(),
                request.getPassword()
            );
            
            // PASO 2: Convertir a DTO
            LoginResponseDto response = buildLoginResponse(user);
            
            log.info("Authentication successful for user ID: {}", user.getId());
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            log.debug("Authentication failed: user not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidCredentialsException e) {
            log.debug("Authentication failed: invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (DomainException e) {
            log.debug("Authentication failed: domain error");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/auth/me
     * 
     * Obtiene la información del usuario actualmente autenticado
     * (Nota: En futuro con Spring Security real, usarías @AuthenticationPrincipal)
     */
    @GetMapping("/me")
    @Operation(
        summary = "Obtener usuario actual",
        description = "Retorna la información del usuario autenticado actualmente",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<?> getCurrentUser(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @RequestParam(name = "user_id") Long userId) {
        try {
            User user = authUseCase.getUserById(userId);
            return ResponseEntity.ok(toUserResponseDto(user));
        } catch (DomainException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/auth/{id}
     * 
     * Obtiene un usuario por su ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna la información de un usuario específico según su ID",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id) {
        try {
            User user = authUseCase.getUserById(id);
            return ResponseEntity.ok(toUserResponseDto(user));
        } catch (DomainException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET /api/auth/email/{email}
     * 
     * Obtiene un usuario por su email
     */
    @GetMapping("/email/{email}")
    @Operation(
        summary = "Obtener usuario por email",
        description = "Retorna la información de un usuario específico según su email",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<?> getUserByEmail(
            @Parameter(description = "Email del usuario", required = true, example = "student@medigo.com")
            @PathVariable String email) {
        try {
            User user = authUseCase.getUserByEmail(email);
            return ResponseEntity.ok(toUserResponseDto(user));
        } catch (DomainException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ==================== MAPPERS (DTOs) ====================

    /**
     * Convierte User → LoginResponseDto
     * Esta es la respuesta después de un login exitoso
     */
    private LoginResponseDto buildLoginResponse(User user) {
        // En MVP: generamos un fake JWT
        // En producción: aquí haría JwtService.generateToken(user)
        String fakeToken = generateFakeJwt(user);
        
        return LoginResponseDto.builder()
                .accessToken(fakeToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getCode())
                .expiresIn(3600L)  // 1 hora
                .build();
    }

    /**
     * Convierte User → UserResponseDto
     */
    private UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getCode(),
                user.isActive()
        );
    }

    /**
     * ⚠️ FAKE JWT - Solo para MVP
     * 
     * En producción reemplazar con:
     * @Bean
     * public JwtService jwtService() { ... }
     * 
     * y luego: String token = jwtService.generateToken(user);
     */
    private String generateFakeJwt(User user) {
        // Format: "fake-jwt.{userid}.{role}.{timestamp}"
        long timestamp = System.currentTimeMillis();
        return String.format("fake-jwt.%d.%s.%d",
                user.getId(),
                user.getRole().getCode(),
                timestamp);
    }
}