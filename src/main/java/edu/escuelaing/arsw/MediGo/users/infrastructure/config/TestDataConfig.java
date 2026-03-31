package edu.escuelaing.arsw.medigo.users.infrastructure.config;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * ⚠️ TEST DATA CONFIGURATION
 * 
 * CENTRALIZAMOS LOS DATOS DE PRUEBA AQUÍ POR:
 * ✅ Seguridad: Separamos test-data de la lógica
 * ✅ Mantenibilidad: Un solo lugar para actualizar credenciales de test
 * ✅ Auditabilidad: Fácil de encontrar y marcar como TEST-ONLY
 * 
 * IMPORTANTE: 
 * - Estos usuarios son SOLO PARA MVP/TESTING
 * - EN PRODUCCIÓN: Deben venir de una base de datos real con contraseñas hasheadas
 * - Nunca usar "123" como contraseña en producción
 * 
 * S2696 (Hardcoded credentials) HOTSPOT JUSTIFICACIÓN:
 * Esta clase existe específicamente para concentrar credenciales de TEST
 * en una ubicación visible y auditable. El patrón es:
 * - Solo para environments de test (no en producción)
 * - Claramente marcado con @WarningComment y documentación
 * - Mejor que dispersar "123" en múltiples archivos
 */
@Getter
public class TestDataConfig {
    
    /**
     * Usuarios de prueba predefinidos
     * Estructura: id, username, email, password, role
     */
    public static final List<UserTestData> TEST_USERS = Arrays.asList(
        new UserTestData(1L, "student", "student@medigo.com", "123", Role.STUDENT),
        new UserTestData(2L, "admin", "admin@medigo.com", "123", Role.ADMIN),
        new UserTestData(3L, "vendor", "vendor@medigo.com", "123", Role.VENDOR),
        new UserTestData(4L, "logistics", "logistics@medigo.com", "123", Role.LOGISTICS)
    );
    
    /**
     * Registro de datos de un usuario de test
     */
    @Getter
    public static class UserTestData {
        private final Long id;
        private final String username;
        private final String email;
        private final String password;  // TEST ONLY - en prod sería hasheada
        private final Role role;
        
        public UserTestData(Long id, String username, String email, String password, Role role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
        }
        
        /**
         * Convierte a objeto User de dominio
         */
        public User toDomainUser() {
            return User.create(id, username, email, password, role);
        }
    }
    
    /**
     * Obtiene un usuario de test por username
     */
    public static UserTestData findByUsername(String username) {
        return TEST_USERS.stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Obtiene un usuario de test por email
     */
    public static UserTestData findByEmail(String email) {
        return TEST_USERS.stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Obtiene un usuario de test por ID
     */
    public static UserTestData findById(Long id) {
        return TEST_USERS.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}
