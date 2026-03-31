error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java:edu/escuelaing/arsw/medigo/users/domain/exception/InvalidCredentialsException#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/exception/InvalidCredentialsException#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 182
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.application.service;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.exception.@@InvalidCredentialsException;
import edu.escuelaing.arsw.medigo.users.domain.exception.UserNotFoundException;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import edu.escuelaing.arsw.medigo.users.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CAPA DE APLICACIÓN: Service que implementa el caso de uso de autenticación
 * 
 * Responsabilidades:
 * ✅ Implementar la lógica del caso de uso (autenticación)
 * ✅ Orquestar entre el dominio y los repositorios
 * ✅ Realizar transacciones
 * ✅ Logging
 * 
 * ❌ NO contiene lógica de persistencia
 * ❌ NO contiene lógica HTTP/REST
 * ❌ NO sabe cómo se llama desde el exterior (REST, GraphQL, etc.)
 * 
 * DEPENDENCIAS:
 * - UserRepositoryPort (inyectada) → abstracc ión para buscar usuarios
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
        log.info("Intentando autenticar usuario: {}", username);
        
        // Buscar usuario en repositorio (abstracto)
        Optional<User> user = userRepository.findByUsername(username);
        
        // Si no existe, lanzo excepción de dominio
        if (user.isEmpty()) {
            log.warn("Usuario no encontrado: {}", username);
            throw UserNotFoundException.byUsername(username);
        }
        
        // Obtengo el usuario
        User foundUser = user.get();
        
        // Valido credenciales usando lógica del dominio
        if (!foundUser.credentialsMatch(password)) {
            log.warn("Credenciales inválidas para usuario: {}", username);
            throw InvalidCredentialsException.withMessage(username);
        }
        
        log.info("Usuario autenticado exitosamente: {}", username);
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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/exception/InvalidCredentialsException#