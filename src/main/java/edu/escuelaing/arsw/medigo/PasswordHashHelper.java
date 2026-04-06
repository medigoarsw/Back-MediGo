package edu.escuelaing.arsw.medigo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashHelper {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java PasswordHashHelper <password>");
            System.exit(1);
        }
        
        String password = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash BCrypt: " + hash);
    }
}
