package edu.escuelaing.arsw.medigo.users.infrastructure.adapter.in;
import edu.escuelaing.arsw.medigo.users.domain.port.in.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthUseCase authUseCase;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok().build();
    }
    public record LoginRequest(String email, String password) {}
}