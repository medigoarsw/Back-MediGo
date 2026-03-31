package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del dominio (User Entity)
 * 
 * ✅ SIN dependencias externas
 * ✅ SIN BD
 * ✅ SIN Spring
 * ✅ Lógica pura del dominio
 * 
 * Está en la capa de dominio porque prueba entidades, value objects y reglas de negocio
 */
class UserTest {
    
    @Test
    void testUserCredentialsMatchSuccess() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "password123", Role.STUDENT);
        
        // ACT
        boolean result = user.credentialsMatch("password123");
        
        // ASSERT
        assertTrue(result, "Las credenciales deben coincidir");
    }
    
    @Test
    void testUserCredentialsMatchFailure() {
        // ARRANGE
        User user = User.create(1L, "student", "student@example.com", "password123", Role.STUDENT);
        
        // ACT
        boolean result = user.credentialsMatch("wrongpassword");
        
        // ASSERT
        assertFalse(result, "Las credenciales no deben coincidir");
    }
    
    @Test
    void testInactiveUserCannotLogin() {
        // ARRANGE
        User user = new User(1L, "student@example.com", "password123", "student", Role.STUDENT, false);
        
        // ACT
        boolean result = user.credentialsMatch("password123");
        
        // ASSERT
        assertFalse(result, "Usuario inactivo no puede autenticarse");
    }
    
    @Test
    void testUserRole() {
        // ARRANGE
        User admin = User.create(1L, "admin", "admin@example.com", "password", Role.ADMIN);
        
        // ACT & ASSERT
        assertEquals("ROLE_ADMIN", admin.getAuthority());
    }
    
    @Test
    void testUserCreationWithFactory() {
        // ARRANGE & ACT
        User user = User.create(2L, "vendor", "vendor@example.com", "securepass", Role.VENDOR);
        
        // ASSERT
        assertNotNull(user);
        assertEquals(2L, user.getId());
        assertEquals("vendor", user.getUsername());
        assertEquals("vendor@example.com", user.getEmail());
        assertEquals(Role.VENDOR, user.getRole());
        assertTrue(user.credentialsMatch("securepass"));
    }
}
