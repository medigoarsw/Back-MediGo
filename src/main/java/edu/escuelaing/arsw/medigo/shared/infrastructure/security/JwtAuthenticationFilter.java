package edu.escuelaing.arsw.medigo.shared.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Filtro que extrae el rol del JWT (real o fake) y lo registra en el
 * SecurityContext para que Spring Security aplique las reglas de autorización.
 *
 * Soporta dos formatos:
 *   - JWT real del API Gateway: header.payload.signature (base64url)
 *     El payload debe contener el rol en alguno de estos claims:
 *     "role", "roles", "authorities" o "scope".
 *   - JWT fake del backend (desarrollo/pruebas): fake-jwt.{userId}.{ROLE}.{timestamp}
 *
 * Si no se puede extraer el rol, la request continúa sin autenticación y
 * Spring Security aplicará la regla correspondiente (403 para endpoints protegidos).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX   = "Bearer ";
    private static final String FAKE_JWT_PREFIX = "fake-jwt";

    /** Nombres de claim donde el API Gateway puede enviar el rol, en orden de preferencia. */
    private static final String[] ROLE_CLAIM_NAMES = {"role", "roles", "authorities", "scope"};

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            String role  = extractRole(token);

            if (role != null) {
                // Normalizar: quitar prefijo ROLE_ si el claim ya lo incluye
                String normalized = role.toUpperCase().startsWith("ROLE_")
                        ? role.substring(5).toUpperCase()
                        : role.toUpperCase();

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + normalized);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(token, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    // ── Dispatch por formato ──────────────────────────────────────────

    private String extractRole(String token) {
        if (token.startsWith(FAKE_JWT_PREFIX)) {
            return extractRoleFromFakeJwt(token);
        }
        return extractRoleFromRealJwt(token);
    }

    // ── Formato fake: fake-jwt.{userId}.{ROLE}.{timestamp} ───────────

    private String extractRoleFromFakeJwt(String token) {
        String[] parts = token.split("\\.", -1);
        // parts[0]=fake-jwt  parts[1]=userId  parts[2]=ROLE  parts[3]=timestamp
        return (parts.length >= 4) ? parts[2] : null;
    }

    // ── Formato real JWT: header.payload.signature ───────────────────

    private String extractRoleFromRealJwt(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            JsonNode payload = MAPPER.readTree(decoded);

            for (String claimName : ROLE_CLAIM_NAMES) {
                JsonNode node = payload.get(claimName);
                if (node == null) continue;

                if (node.isTextual()) {
                    return node.asText();
                }
                if (node.isArray() && node.size() > 0) {
                    // Tomar el primer elemento del array
                    return node.get(0).asText();
                }
            }
        } catch (Exception e) {
            // Payload inválido o no decodificable — la request continuará sin autenticación
        }
        return null;
    }

    /** Agrega padding '=' que Base64.getUrlDecoder requiere. */
    private String padBase64(String input) {
        int rem = input.length() % 4;
        return rem == 0 ? input : input + "=".repeat(4 - rem);
    }
}
