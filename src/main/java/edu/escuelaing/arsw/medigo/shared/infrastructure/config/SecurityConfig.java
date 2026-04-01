package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de Spring Security
 *
 * CSRF está deshabilitado por diseño:
 * API REST stateless con autenticación por token (JWT).
 *
 * Se usan AntPathRequestMatcher explícitamente para evitar el comportamiento
 * de MvcRequestMatcher (default en Spring Security 6.1+) que puede fallar
 * al resolver patrones wildcard cuando WebSocket está presente en el classpath.
 *
 * PRODUCCIÓN:
 * - Implementar JWT tokens en AuthController
 * - Crear JwtAuthenticationFilter
 * - Configurar token refresh
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("java:S4502")  // CSRF seguro en API stateless con token auth
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
<<<<<<< HEAD
                // TODO LOS ENDPOINTS PÚBLICOS (sin autenticación)
                // Se agregará JWT y permisos por endpoint en fase final
                .anyRequest().permitAll()
            )
            .formLogin().disable()  // Desabilitar form login por defecto
            .httpBasic().disable();  // Desabilitar HTTP Basic
=======
                // Swagger UI y documentación (públicos)
                .requestMatchers(
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/swagger-resources"),
                    new AntPathRequestMatcher("/swagger-resources/**"),
                    new AntPathRequestMatcher("/webjars/**")
                ).permitAll()
                // Endpoints de autenticación (públicos)
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                // Endpoint raíz
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                // Endpoints de subastas (públicos hasta implementar JWT)
                .requestMatchers(new AntPathRequestMatcher("/api/auctions/**")).permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            );
>>>>>>> 1a598441a6d622f352ffdff5ea8ff443872081fe

        return http.build();
    }
}