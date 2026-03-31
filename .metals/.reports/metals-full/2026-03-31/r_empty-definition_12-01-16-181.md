error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/domain/model/User.java:edu/escuelaing/arsw/medigo/users/domain/valueobject/Role#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/domain/model/User.java
empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/valueobject/Role#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 117
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/users/domain/model/User.java
text:
```scala
package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.@@Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio: Usuario
 * 
 * Esta es la entidad de dominio que representa un usuario en el sistema.
 * NO contiene lógica de persistencia, NO sabe nada de BD.
 * 
 * ❌ NO depende de Spring, JPA ni frameworks
 * ✅ SÍ contiene lógica pura de dominio (validaciones, reglas de negocio)
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

    /**
     * Factory method para crear usuarios
     * Recomendado en arquitectura hexagonal
     */
    public static User create(Long id, String username, String email, String password, Role role) {
        return new User(id, email, password, username, role, true);
    }

    /**
     * Valida que las credenciales coincidan
     * Esta lógica está en el dominio porque es una regla de negocio pura
     */
    public boolean credentialsMatch(String providedPassword) {
        if (!this.active) {
            return false;
        }
        // En producción aquí haría bcrypt.matches(providedPassword, this.password)
        return this.password.equals(providedPassword);
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
                '}';
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: edu/escuelaing/arsw/medigo/users/domain/valueobject/Role#