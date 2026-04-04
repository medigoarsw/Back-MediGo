package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * Modelo de dominio: Usuario
 * 
 * Esta es la entidad de dominio que representa un usuario en el sistema.
 * NO contiene lógica de persistencia, NO sabe nada de BD.
 * 
 * NO depende de Spring, JPA ni frameworks
 * SÍ contiene lógica pura de dominio (validaciones, reglas de negocio)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class User {
    
    private Long id;
    private String email;
    private String password;  // En producción: passwordHash
    private String username;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    /**
     * Factory method para crear usuarios
     * Recomendado en arquitectura hexagonal
     */
    public static User create(Long id, String username, String email, String password, Role role) {
        return new User(id, email, password, username, role, true, LocalDateTime.now());
    }

    /**
     * Valida que las credenciales coincidan
     * Esta lógica está en el dominio porque es una regla de negocio pura
     * 
     * TIMING ATTACK RESISTANCE:
     * Se usa MessageDigest.isEqual() en lugar de String.equals() para evitar
     * ataques de timing que permitirían adivinar contraseñas carácter por carácter.
     * MessageDigest.isEqual() toma siempre el mismo tiempo independientemente
     * de dónde falle la comparación.
     * 
     * En producción: Usaría bcrypt.matches(providedPassword, this.password)
     * que ya es timing-attack-resistant y además hashea las contraseñas.
     */
    public boolean credentialsMatch(String providedPassword) {
        if (!this.active) {
            return false;
        }
        // Usar MessageDigest.isEqual() para comparación timing-attack-resistant
        // Nota: Esto funciona incluso con passwords en texto plano (como en MVP)
        byte[] providedBytes = providedPassword.getBytes();
        byte[] storedBytes = this.password.getBytes();
        return MessageDigest.isEqual(providedBytes, storedBytes);
    }

    /**
     * Obtiene la autoridad de Spring Security
     */
    public String getAuthority() {
        return "ROLE_" + this.role.getCode().toUpperCase();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}