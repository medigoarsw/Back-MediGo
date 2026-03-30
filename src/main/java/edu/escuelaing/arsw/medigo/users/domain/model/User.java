package edu.escuelaing.arsw.medigo.users.domain.model;
import lombok.*;
@Getter @Builder @AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String name;
    private UserRole role;
    private boolean active;
    public enum UserRole { ADMIN, AFFILIATE, DELIVERY }
}