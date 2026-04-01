package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios del dominio (User Entity)
 * 
 * SIN dependencias externas
 * SIN BD
 * SIN Spring
 * Lógica pura del dominio
 * 
 * Está en la capa de dominio porque prueba entidades, value objects y reglas de negocio
 */
class UserTest {
    
    @Test
    void testUserCredentialsMatchSuccess() {
        // ARRANGE
        User user = User.create(1L, "user", "user@example.com", "password123", Role.AFFILIATE);
        
        // ACT
        boolean result = user.credentialsMatch("password123");
        
        // ASSERT
        assertTrue(result, "Las credenciales deben coincidir");
    }
    
    @Test
    void testUserCredentialsMatchFailure() {
        // ARRANGE
        User user = User.create(1L, "user", "user@example.com", "password123", Role.AFFILIATE);
        
        // ACT
        boolean result = user.credentialsMatch("wrongpassword");
        
        // ASSERT
        assertFalse(result, "Las credenciales no deben coincidir");
    }
    
    @Test
    void testInactiveUserCannotLogin() {
        // ARRANGE
        User user = new User(1L, "user@example.com", "password123", "user", Role.AFFILIATE, false, LocalDateTime.now());
        
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
        User user = User.create(2L, "delivery", "delivery@example.com", "securepass", Role.DELIVERY);
        
        // ASSERT
        assertNotNull(user);
        assertEquals(2L, user.getId());
        assertEquals("delivery", user.getUsername());
        assertEquals("delivery@example.com", user.getEmail());
        assertEquals(Role.DELIVERY, user.getRole());
        assertTrue(user.credentialsMatch("securepass"));
    }
}
