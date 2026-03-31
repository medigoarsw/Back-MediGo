error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java:edu/escuelaing/arsw/medigo/users/domain/port/in/AuthUseCase#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/port/in/AuthUseCase#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 316
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.exception.*;
import edu.escuelaing.arsw.medigo.users.application.dto.SignUpRequestDto;
import edu.escuelaing.arsw.medigo.users.domain.port.in.@@AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import edu.escuelaing.arsw.medigo.users.domain.util.PasswordValidator;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CAPA DE APLICACIÓN: Service que implementa el caso de uso de autenticación
 * 
 * Responsabilidades:
 * - Implementar la lógica del caso de uso (autenticación)
 * - Orquestar entre el dominio y los repositorios
 * - Realizar transacciones
 * - Logging
 * 
 * NO contiene lógica de persistencia
 * NO contiene lógica HTTP/REST
 * NO sabe cómo se llama desde el exterior (REST, GraphQL, etc.)
 * 
 * DEPENDENCIAS:
 * - UserRepositoryPort (inyectada) → abstracción para buscar usuarios
 * - No necesita inyectar Controller ni Entity
 * 
 * FLUJO:
 * 1. Controller llama a authenticate(username, password)
 * 2. Service delega a userRepository.findByUsername(username)
 * 3. Service valida credenciales en el modelo de dominio
 * 4. Service devuelve User al Controller
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {
    
    // INYECCIÓN: El repositorio es un puerto de salida (DESACOPLADO)
    // En MVP: InMemoryUserRepository
    // En futuro: JpaUserRepository (SIN cambiar este código)
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Autentica un usuario
     * 
     * LÓGICA DE NEGOCIO:
     * 1. Buscar el usuario por username
     * 2. Verificar que las credenciales coincidan (delegado al modelo de dominio)
     * 3. Si todo OK, devolver el usuario
     * 4. Si fallaExceptionException
     */
    @Override
    public User authenticate(String username, String password) {
        log.debug("Authentication attempt initiated");
        
        // Buscar usuario en repositorio (abstracto)
        Optional<User> user = userRepository.findByUsername(username);
        
        // Si no existe, lanzo excepción de dominio
        if (user.isEmpty()) {
            log.warn("Authentication failed: user not found");
            throw UserNotFoundException.byUsername(username);
        }
        
        // Obtengo el usuario
        User foundUser = user.get();
        
        // Valido credenciales usando lógica del dominio
        if (!foundUser.credentialsMatch(password)) {
            log.warn("Authentication failed: invalid credentials");
            throw InvalidCredentialsException.withMessage(username);
        }
        
        log.info("User authenticated successfully");
        return foundUser;
    }

    /**
     * Obtiene un usuario por ID
     */
    @Override
    public User getUserById(Long userId) {
        log.info("Obteniendo usuario con ID: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isEmpty()) {
            log.warn("Usuario no encontrado con ID: {}", userId);
            throw UserNotFoundException.byId(userId);
        }
        
        return user.get();
    }

    /**
     * Obtiene un usuario por email
     */
    @Override
    public User getUserByEmail(String email) {
        log.info("Obteniendo usuario con email: {}", email);
        
        Optional<User> user = userRepository.findByEmail(email);
        
        if (user.isEmpty()) {
            log.warn("Usuario no encontrado con email: {}", email);
            throw UserNotFoundException.byEmail(email);
        }
        
        return user.get();
    }

    /**
     * Registra un nuevo usuario con validaciones
     * 
     * LÓGICA DE NEGOCIO:
     * 1. Validar campos (username, email, password no vacíos)
     * 2. Validar email válido (formato)
     * 3. Validar que el username no esté registrado
     * 4. Validar que el email no esté registrado
     * 5. Validar que la contraseña sea fuerte
     * 6. Validar rol (solo USUARIO o REPARTIDOR)
     * 7. Encriptar la contraseña
     * 8. Crear el usuario
     * 9. Guardar en el repositorio
     * 10. Devolver el usuario creado
     */
    public User signUp(SignUpRequestDto signUpRequest) {
        log.info("SignUp attempt for email: {}", signUpRequest.getEmail());
        
        // VALIDACIÓN 1: Campos no vacíos
        if (signUpRequest.getName() == null || signUpRequest.getName().strip().isEmpty()) {
            log.warn("SignUp failed: empty username");
            throw InvalidInputException.emptyField("username");
        }
        
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().strip().isEmpty()) {
            log.warn("SignUp failed: empty email");
            throw InvalidInputException.emptyField("email");
        }
        
        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().isEmpty()) {
            log.warn("SignUp failed: empty password");
            throw InvalidInputException.emptyField("password");
        }
        
        // VALIDACIÓN 2: Email válido (formato)
        if (!isValidEmail(signUpRequest.getEmail())) {
            log.warn("SignUp failed: invalid email format");
            throw InvalidInputException.invalidEmail(signUpRequest.getEmail());
        }
        
        // VALIDACIÓN 3: Username no registrado
        if (userRepository.findByUsername(signUpRequest.getName()).isPresent()) {
            log.warn("SignUp failed: username already exists");
            throw UserAlreadyExistsException.usernameExists(signUpRequest.getName());
        }
        
        // VALIDACIÓN 4: Email no registrado
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            log.warn("SignUp failed: email already registered");
            throw UserAlreadyExistsException.emailExists(signUpRequest.getEmail());
        }
        
        // VALIDACIÓN 5: Contraseña fuerte
        if (!isStrongPassword(signUpRequest.getPassword())) {
            log.warn("SignUp failed: weak password");
            throw InvalidInputException.weakPassword();
        }
        
        // VALIDACIÓN 6: Rol válido (solo USUARIO o REPARTIDOR)
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(signUpRequest.getRole().toUpperCase());
            if (roleEnum != Role.USER && roleEnum != Role.DELIVERY) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            log.warn("SignUp failed: invalid role {}", signUpRequest.getRole());
            throw InvalidInputException.weakPassword();
        }
        
        // CIFRADO: Encriptar la contraseña
        String encryptedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        
        // CREACIÓN: Crear usuario con set de fecha de creación
        User newUser = User.create(
            null,
            signUpRequest.getName(),
            signUpRequest.getEmail(),
            encryptedPassword,
            roleEnum
        );
        
        // PERSISTENCIA: Guardar en repositorio
        User savedUser = userRepository.save(newUser);
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Validar email con expresión regular
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
    
    /**
     * Validar contraseña fuerte:
     * - Mínimo 8 caracteres
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     */
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUppercase && hasLowercase && hasDigit;
    }
}


```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/port/in/AuthUseCase#