package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import edu.escuelaing.arsw.medigo.shared.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de Spring Security con modo conmutable.
 *
 * - Por defecto: seguridad restringida por rol (útil en CI/tests).
 * - Modo abierto: todos los endpoints públicos cuando
 *   app.security.open-all-endpoints=true.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.open-all-endpoints:false}")
    private boolean openAllEndpoints;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://frontmedigo.vercel.app}")
    private String allowedOrigins;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (openAllEndpoints) {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }

        http
            .addFilterBefore(new JwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auctions").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/auctions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/auctions/**").hasAnyRole("ADMIN", "AFFILIATE")
                .requestMatchers(HttpMethod.POST, "/api/auctions/**").hasAnyRole("ADMIN", "AFFILIATE")
                .requestMatchers("/api/sedes/**").hasRole("ADMIN")
                .requestMatchers("/api/logistics/**").authenticated()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Trace-Id"));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Trace-Id"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}