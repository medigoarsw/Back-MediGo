package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import edu.escuelaing.arsw.medigo.shared.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 *
 * El API Gateway valida la autenticación y reenvía cada petición con el JWT.
 * Este backend es la fuente de verdad para la AUTORIZACIÓN: extrae el rol
 * del token y aplica las reglas por endpoint y método HTTP.
 *
 * Roles del sistema:
 *   ADMIN    – acceso total al módulo de subastas.
 *   AFFILIATE (USUARIO) – puede consultar, unirse y pujar; no puede crear/editar.
 *   DELIVERY – sin permisos sobre subastas (403 en cualquier endpoint).
 *
 * CSRF deshabilitado: API REST stateless con autenticación por token.
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
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Extraer rol del JWT antes de que Spring Security evalúe permisos
            .addFilterBefore(new JwtAuthenticationFilter(),
                             UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz

                // ── Endpoints públicos ──────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                 "/swagger-ui.html").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // ── Subastas: solo ADMIN ────────────────────────────────
                // POST /api/auctions   → crear subasta
                .requestMatchers(HttpMethod.POST, "/api/auctions")
                        .hasRole("ADMIN")
                // PUT  /api/auctions/{id} → editar subasta
                .requestMatchers(HttpMethod.PUT,  "/api/auctions/**")
                        .hasRole("ADMIN")

                // ── Subastas: ADMIN y AFFILIATE (USUARIO) ───────────────
                // GET  /api/auctions/**  → ver detalle, listar, historial de pujas
                .requestMatchers(HttpMethod.GET, "/api/auctions/**")
                        .hasAnyRole("ADMIN", "AFFILIATE")
                // POST /api/auctions/** → unirse y pujar  (/{id}/join, /{id}/bids)
                .requestMatchers(HttpMethod.POST, "/api/auctions/**")
                        .hasAnyRole("ADMIN", "AFFILIATE")

                // ── Resto: requiere autenticación (403 si no hay token) ─
                .anyRequest().authenticated()
            );

        return http.build();
    }
}