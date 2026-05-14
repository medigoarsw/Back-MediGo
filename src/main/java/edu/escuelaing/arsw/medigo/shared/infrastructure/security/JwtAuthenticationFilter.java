package edu.escuelaing.arsw.medigo.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que extrae el rol del JWT y lo registra en el
 * SecurityContext para que Spring Security aplique las reglas de autorización.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String FAKE_JWT_PREFIX = "fake-jwt";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            
            try {
                String role = null;
                if (token.startsWith(FAKE_JWT_PREFIX)) {
                    role = extractRoleFromFakeJwt(token);
                } else if (jwtService.isTokenValid(token)) {
                    Claims claims = jwtService.extractAllClaims(token);
                    role = claims.get("role", String.class);
                    if (role == null) {
                        role = claims.get("authorities", String.class);
                    }
                }

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
            } catch (Exception e) {
                // Token inválido - continuar sin autenticación
            }
        }

        chain.doFilter(request, response);
    }

    private String extractRoleFromFakeJwt(String token) {
        String[] parts = token.split("\\.", -1);
        return (parts.length >= 4) ? parts[2] : null;
    }
}
