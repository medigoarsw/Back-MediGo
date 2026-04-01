package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security
 * 
 * Por ahora:
 * - POST /api/auth/login: PERMITIDO (sin autenticación)
 * - GET /api/auth/: PERMITIDO (sin autenticación)
 * - Todo lo demás: requiere autenticación
 * 
 * CSRF está deshabilitado por diseño:
 * Esta es una API REST stateless que usa autenticación por token (JWT).
 * CSRF es un riesgo solo en aplicaciones con sesiones basadas en cookies.
 * Los clientes (móvil, frontend, Postman) están protegidos por la autenticación
 * de token en el header Authorization.
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
            .csrf().disable()  // Seguro: API stateless con autenticación por token
            .authorizeHttpRequests(authz -> authz
                // TODO LOS ENDPOINTS PÚBLICOS (sin autenticación)
                // Se agregará JWT y permisos por endpoint en fase final
                .anyRequest().permitAll()
            )
            .formLogin().disable()  // Desabilitar form login por defecto
            .httpBasic().disable();  // Desabilitar HTTP Basic

        return http.build();
    }
}