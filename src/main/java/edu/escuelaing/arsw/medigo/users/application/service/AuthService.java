package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.exception.*;
import edu.escuelaing.arsw.medigo.users.application.dto.SignUpRequestDto;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
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
 * 1. Controller llama a authenticate(email, password)
 * 2. Service delega a userRepository.findByEmail(email)
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
     * 1. Buscar el usuario por email
     * 2. Verificar que las credenciales coincidan (delegado al modelo de dominio)
     * 3. Si todo OK, devolver el usuario
     * 4. Si falla, lanzar excepción de dominio
     */
    @Override
    public User authenticate(String email, String password) {
        log.debug("Authentication attempt initiated");
        
        // Buscar usuario en repositorio (abstracto)
        Optional<User> user = userRepository.findByEmail(email);
        
        // Si no existe, lanzo excepción de dominio
        if (user.isEmpty()) {
            log.warn("Authentication failed: user not found");
            throw UserNotFoundException.byEmail(email);
        }
        
        // Obtengo el usuario
        User foundUser = user.get();
        
        // Valido credenciales usando PasswordEncoder (para hashes bcrypt)
        // En lugar de user.credentialsMatch() que solo funciona con texto plano
        if (!foundUser.isActive() || !passwordEncoder.matches(password, foundUser.getPassword())) {
            log.warn("Authentication failed: invalid credentials");
            throw InvalidCredentialsException.withMessage(email);
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
    * 1.1 Validar teléfono opcional (si viene, formato +57-322-5555555)
     * 2. Validar email válido (formato)
     * 3. Validar que el username no esté registrado
     * 4. Validar que el email no esté registrado
     * 5. Validar que la contraseña sea fuerte (8+ chars, mayús, minús, dígito)
     * 6. Validar rol (solo AFFILIATE o DELIVERY - ADMIN se crea por admins)
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

        String normalizedPhone = normalizePhone(signUpRequest.getPhone());
        if (normalizedPhone != null && !isValidPhone(normalizedPhone)) {
            log.warn("SignUp failed: invalid phone format");
            throw InvalidInputException.invalidPhone(signUpRequest.getPhone());
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
        
        // VALIDACIÓN 6: Rol válido (solo AFFILIATE o REPARTIDOR)
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(signUpRequest.getRole().toUpperCase());
            if (roleEnum != Role.AFFILIATE && roleEnum != Role.DELIVERY) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            log.warn("SignUp failed: invalid role {}", signUpRequest.getRole());
            throw InvalidInputException.invalidRole(signUpRequest.getRole());
        }
        
        // CIFRADO: Encriptar la contraseña
        String encryptedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        
        // CREACIÓN: Crear usuario con set de fecha de creación
        User newUser = User.create(
            null,
            signUpRequest.getName(),
            signUpRequest.getEmail(),
            encryptedPassword,
            normalizedPhone,
            roleEnum
        );
        
        // PERSISTENCIA: Guardar en repositorio
        User savedUser = userRepository.save(newUser);
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Validar email con expresión regular (segura contra ReDoS)
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Validación simple y segura: debe contener @ y un punto
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return false;
        }
        // Verificar que hay un punto después del @
        return email.lastIndexOf('.') > atIndex;
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
        
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUppercase && hasLowercase && hasDigit;
    }

    /**
     * Valida teléfono internacional con formato: +57-322-5555555
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+\\d{1,3}-\\d{3}-\\d{7}$");
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

