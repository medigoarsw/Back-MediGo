package edu.escuelaing.arsw.medigo.users.domain.model;

import edu.escuelaing.arsw.medigo.users.domain.valueobject.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Domain - Pruebas unitarias")
class UserDomainTest {

    @Test
    void create_setsCorrectValues() {
        User user = User.create(1L, "admin", "admin@medigo.com", "pass", Role.ADMIN);
        
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getEmail()).isEqualTo("admin@medigo.com");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void createWithPhone_setsCorrectValues() {
        User user = User.create(1L, "user", "user@medigo.com", "pass", "123456", Role.AFFILIATE);
        assertThat(user.getPhone()).isEqualTo("123456");
    }

    @Test
    void fromPersistence_reconstructsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.fromPersistence(
            1L, "u", "e", "p", "ph", Role.DELIVERY, false, now, now
        );
        
        assertThat(user.isActive()).isFalse();
        assertThat(user.getPhone()).isEqualTo("ph");
    }

    @Test
    void credentialsMatch_validatesCorrectly() {
        User user = User.create(1L, "u", "e", "pass", Role.ADMIN);
        
        assertThat(user.credentialsMatch("pass")).isTrue();
        assertThat(user.credentialsMatch("wrong")).isFalse();
        
        // Deactivated user
        User inactive = User.fromPersistence(
            1L, "u", "e", "pass", null, Role.ADMIN, false, LocalDateTime.now(), LocalDateTime.now()
        );
        assertThat(inactive.credentialsMatch("pass")).isFalse();
    }

    @Test
    void getAuthority_formatsCorrectly() {
        User user = User.create(1L, "u", "e", "p", Role.ADMIN);
        assertThat(user.getAuthority()).isEqualTo("ROLE_ADMIN");
        
        User driver = User.create(1L, "u", "e", "p", Role.DELIVERY);
        assertThat(driver.getAuthority()).isEqualTo("ROLE_DELIVERY");
    }

    @Test
    void toString_containsKeyFields() {
        User user = User.create(1L, "juan", "e", "p", Role.ADMIN);
        String str = user.toString();
        assertThat(str).contains("juan").contains("ADMIN");
    }
}
