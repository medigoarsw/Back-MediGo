package edu.escuelaing.arsw.medigo.users.infrastructure.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserEntityTest {

    @Test
    void testUserEntityBuilderAndGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .name("testuser")
                .phone("123456")
                .role("USER")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(1L, entity.getId());
        assertEquals("test@example.com", entity.getEmail());
        assertEquals("hash", entity.getPasswordHash());
        assertEquals("testuser", entity.getName());
        assertEquals("123456", entity.getPhone());
        assertEquals("USER", entity.getRole());
        assertTrue(entity.isActive());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());

        entity.setId(2L);
        assertEquals(2L, entity.getId());
        entity.setEmail("new@example.com");
        assertEquals("new@example.com", entity.getEmail());
        entity.setPasswordHash("newhash");
        assertEquals("newhash", entity.getPasswordHash());
        entity.setName("newuser");
        assertEquals("newuser", entity.getName());
        entity.setPhone("654321");
        assertEquals("654321", entity.getPhone());
        entity.setRole("ADMIN");
        assertEquals("ADMIN", entity.getRole());
        entity.setActive(false);
        assertFalse(entity.isActive());

        LocalDateTime later = now.plusDays(1);
        entity.setCreatedAt(later);
        assertEquals(later, entity.getCreatedAt());
        entity.setUpdatedAt(later);
        assertEquals(later, entity.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        UserEntity entity = new UserEntity();
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());

        entity.onCreate();

        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Test
    void testPreUpdate() {
        UserEntity entity = new UserEntity();
        entity.onCreate();
        LocalDateTime originalUpdate = entity.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}

        entity.onUpdate();

        assertNotNull(entity.getUpdatedAt());
        assertTrue(entity.getUpdatedAt().isAfter(originalUpdate));
    }
}
