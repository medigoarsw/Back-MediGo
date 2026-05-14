package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class MockUserRepositoryTest {

    private MockUserRepository repository;
    private User testUser;

    @BeforeEach
    void setUp() {
        repository = new MockUserRepository();
        testUser = User.create(4L, "testuser", "test@example.com", "password", Role.AFFILIATE);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        // Admin and operator are pre-populated in constructor
        Optional<User> foundAdmin = repository.findByEmail("admin@medigo.com");
        assertTrue(foundAdmin.isPresent());
        assertEquals("admin@medigo.com", foundAdmin.get().getEmail());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        Optional<User> found = repository.findByEmail("nonexisting@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void findById_ExistingId_ReturnsUser() {
        Optional<User> foundAdmin = repository.findById(1L);
        assertTrue(foundAdmin.isPresent());
        assertEquals(1L, foundAdmin.get().getId());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        Optional<User> found = repository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        Optional<User> foundAdmin = repository.findByUsername("admin");
        assertTrue(foundAdmin.isPresent());
        assertEquals("admin", foundAdmin.get().getUsername());
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        Optional<User> found = repository.findByUsername("NonExisting");
        assertFalse(found.isPresent());
    }

    @Test
    void save_NewUser_SavesAndReturnsUserWithId() {
        User savedUser = repository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());

        Optional<User> found = repository.findById(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals(savedUser.getEmail(), found.get().getEmail());
    }

    @Test
    void save_ExistingUser_UpdatesAndReturnsUser() {
        User savedUser = repository.save(testUser);
        Long id = savedUser.getId();

        // Use a different email to actually test updating the reference, since the map is by email
        // Or actually the mock repo uses put(email, user), so to update we'd update same email
        User updatedUser = User.create(id, "updateduser", "test@example.com", "newpass", Role.AFFILIATE);
        User result = repository.save(updatedUser);

        assertEquals(id, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("updateduser", result.getUsername());

        Optional<User> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("updateduser", found.get().getUsername());
    }
}
