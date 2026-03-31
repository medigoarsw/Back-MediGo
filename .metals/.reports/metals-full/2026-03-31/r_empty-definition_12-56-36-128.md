error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/shared/infrastructure/config/SecurityConfig.java:_empty_/HttpSecurity#csrf#disable#authorizeHttpRequests#formLogin#disable#httpBasic#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/shared/infrastructure/config/SecurityConfig.java
empty definition using pc, found symbol in pc: _empty_/HttpSecurity#csrf#disable#authorizeHttpRequests#formLogin#disable#httpBasic#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2144
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/shared/infrastructure/config/SecurityConfig.java
text:
```scala
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
 * ⚠️ PRODUCCIÓN:
 * - Implementar JWT tokens en AuthController
 * - Crear JwtAuthenticationFilter
 * - Configurar token refresh
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // Temporal: para testing con curl/Postman
            .authorizeHttpRequests(authz -> authz
                // Swagger UI y documentación (públicos)
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Endpoints de autenticación (públicos)
                .requestMatchers(
                    "POST",
                    "/api/auth/login",
                    "/api/auth/register"
                ).permitAll()
                .requestMatchers(
                    "GET",
                    "/api/auth/**"
                ).permitAll()
                // Endpoint raíz
                .requestMatchers("/").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin().disable()  // Desabilitar form login por defecto
            .@@httpBasic().disable();  // Desabilitar HTTP Basic

        return http.build();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/HttpSecurity#csrf#disable#authorizeHttpRequests#formLogin#disable#httpBasic#