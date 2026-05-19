package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.users.domain.model.User;
import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import edu.escuelaing.arsw.medigo.users.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JpaUserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository repository;

    @InjectMocks
    private JpaUserRepositoryAdapter adapter;

    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        userEntity = UserEntity.builder()
                .id(1L)
                .name("testuser")
                .email("test@example.com")
                .passwordHash("password123")
                .phone("1234567890")
                .role("AFFILIATE")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        user = User.fromPersistence(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                "1234567890",
                Role.AFFILIATE,
                true,
                now,
                now
        );
    }

    @Test
    void findByEmail_UserFound_ReturnsUser() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        Optional<User> foundUser = adapter.findByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void findByEmail_UserNotFound_ReturnsEmpty() {
        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = adapter.findByEmail("test@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void save_ValidUser_ReturnsSavedUser() {
        when(repository.save(any(UserEntity.class))).thenReturn(userEntity);

        User savedUser = adapter.save(user);

        assertNotNull(savedUser);
        assertEquals(user.getEmail(), savedUser.getEmail());
    }

    @Test
    void findByUsername_UserFound_ReturnsUser() {
        when(repository.findByName("testuser")).thenReturn(Optional.of(userEntity));

        Optional<User> foundUser = adapter.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals(user.getUsername(), foundUser.get().getUsername());
    }

    @Test
    void findByUsername_UserNotFound_ReturnsEmpty() {
        when(repository.findByName("testuser")).thenReturn(Optional.empty());

        Optional<User> foundUser = adapter.findByUsername("testuser");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void findById_UserFound_ReturnsUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(userEntity));

        Optional<User> foundUser = adapter.findById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals(user.getId(), foundUser.get().getId());
    }

    @Test
    void findById_UserNotFound_ReturnsEmpty() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> foundUser = adapter.findById(1L);

        assertFalse(foundUser.isPresent());
    }
}
