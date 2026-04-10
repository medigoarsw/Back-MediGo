package edu.escuelaing.arsw.medigo.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que extrae el rol del JWT (real o fake) y lo registra en el
 * SecurityContext para que Spring Security aplique las reglas de autorización.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX   = "Bearer ";
    private static final String FAKE_JWT_PREFIX = "fake-jwt";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/ws");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            String userId = null;
            String role = null;

            if (token.startsWith(FAKE_JWT_PREFIX)) {
                userId = extractUserIdFromFakeJwt(token);
                role = extractRoleFromFakeJwt(token);
            } else if (jwtService.validateToken(token)) {
                userId = jwtService.extractUserId(token);
                role = jwtService.extractRole(token);
            }

            if (role != null) {
                String normalized = role.toUpperCase().startsWith("ROLE_")
                        ? role.substring(5).toUpperCase()
                        : role.toUpperCase();

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + normalized);
                
                org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class)
                    .info("JWT Filter: Authenticated User ID {} with role {}", userId, authority.getAuthority());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId != null ? userId : token, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class)
                    .warn("JWT Filter: Invalid token or could not extract role: {}", 
                          token.length() > 20 ? token.substring(0, 20) + "..." : token);
            }
        }

        chain.doFilter(request, response);
    }

    private String extractRoleFromFakeJwt(String token) {
        String[] parts = token.split("\\.", -1);
        return (parts.length >= 4) ? parts[2] : null;
    }

    private String extractUserIdFromFakeJwt(String token) {
        String[] parts = token.split("\\.", -1);
        return (parts.length >= 4) ? parts[1] : null;
    }
}
