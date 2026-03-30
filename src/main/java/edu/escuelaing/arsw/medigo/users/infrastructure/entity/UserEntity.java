package edu.escuelaing.arsw.medigo.users.infrastructure.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String role;
    @Column(nullable = false) private boolean active = true;
}