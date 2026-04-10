package edu.escuelaing.arsw.medigo.shared.infrastructure.config;

import edu.escuelaing.arsw.medigo.shared.infrastructure.security.JwtAuthenticationFilter;
import edu.escuelaing.arsw.medigo.shared.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
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
 * 
 * Se usan AntPathRequestMatcher explícitamente para evitar el comportamiento
 * de MvcRequestMatcher (default en Spring Security 6.1+) que puede fallar
 * al resolver patrones wildcard cuando WebSocket está presente en el classpath.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Value("${app.security.open-all-endpoints:false}")
    private boolean openAllEndpoints;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (openAllEndpoints) {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }

        http
            .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                    UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/swagger-ui.html"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/swagger-resources"),
                    new AntPathRequestMatcher("/swagger-resources/**"),
                    new AntPathRequestMatcher("/webjars/**")
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                
                // Endpoints de catálogo - Públicos (modificar si se requiere RBAC)
                .requestMatchers(new AntPathRequestMatcher("/api/medications/**")).permitAll()

                // Subastas: solo ADMIN
                .requestMatchers(new AntPathRequestMatcher("/api/auctions", HttpMethod.POST.name())).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/auctions/**", HttpMethod.PUT.name())).hasRole("ADMIN")

                // Subastas: ADMIN y AFFILIATE
                .requestMatchers(new AntPathRequestMatcher("/api/auctions/**", HttpMethod.GET.name())).hasAnyRole("ADMIN", "AFFILIATE")
                .requestMatchers(new AntPathRequestMatcher("/api/auctions/**", HttpMethod.POST.name())).hasAnyRole("ADMIN", "AFFILIATE")
                
                // Sucursales (Branches)
                .requestMatchers(new AntPathRequestMatcher("/api/branches", HttpMethod.POST.name())).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/branches/**", HttpMethod.PUT.name())).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/branches/**", HttpMethod.DELETE.name())).hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/branches", HttpMethod.GET.name())).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]",
            "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Trace-ID"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Trace-ID"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}