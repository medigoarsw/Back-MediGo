package edu.escuelaing.arsw.medigo.users.infrastructure.security;

import edu.escuelaing.arsw.medigo.users.domain.port.out.PasswordBreachCheckerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * Implementación con k-anonymity usando API de Have I Been Pwned.
 */
@Component
@Slf4j
public class HaveIBeenPwnedPasswordChecker implements PasswordBreachCheckerPort {

    private static final String HIBP_RANGE_ENDPOINT = "https://api.pwnedpasswords.com/range/";

    private final HttpClient httpClient;
    private final boolean enabled;

    public HaveIBeenPwnedPasswordChecker(
            @Value("${app.security.password-breach-check-enabled:true}") boolean enabled,
            @Value("${app.security.password-breach-check-timeout-ms:2000}") long timeoutMs) {
        this.enabled = enabled;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(timeoutMs, 500)))
                .build();
    }

    @Override
    public boolean isCompromised(String plainPassword) {
        if (!enabled || plainPassword == null || plainPassword.isBlank()) {
            return false;
        }

        try {
            String sha1 = sha1Hex(plainPassword);
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HIBP_RANGE_ENDPOINT + prefix))
                    .header("User-Agent", "MediGo-Password-Checker")
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.warn("HIBP password check unavailable (status={})", response.statusCode());
                return false;
            }

            return containsSuffix(response.body(), suffix);
        } catch (Exception ex) {
            // Fail-open para no bloquear registro por conectividad externa.
            log.warn("HIBP password check failed, continuing without external validation: {}", ex.getMessage());
            return false;
        }
    }

    private boolean containsSuffix(String body, String suffixToFind) {
        if (body == null || body.isBlank()) {
            return false;
        }

        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length < 2) {
                continue;
            }

            if (suffixToFind.equalsIgnoreCase(parts[0].trim())) {
                return true;
            }
        }

        return false;
    }

    private String sha1Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }
}
