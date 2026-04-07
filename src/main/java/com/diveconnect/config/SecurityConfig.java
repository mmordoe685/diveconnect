package com.diveconnect.config;

import com.diveconnect.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // ── Manejadores de error con cuerpo JSON ────────────────────
            .exceptionHandling(ex -> ex
                // 401 — sin autenticación (token ausente o inválido)
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"message\":\"No autorizado. Por favor inicia sesión.\"}");
                })
                // 403 — autenticado pero sin permiso
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"message\":\"Acceso denegado.\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth

                // ── Recursos estáticos (frontend) ────────────────────────
                .requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**",
                    "/pages/**", "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // ── Auth pública ─────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Admin (sólo ADMINISTRADOR) ───────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")

                // ── Endpoints públicos de catálogo ───────────────────────
                .requestMatchers(HttpMethod.GET, "/api/centros-buceo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/inmersiones/disponibles").permitAll()
                // literal antes del patrón genérico /{id}
                .requestMatchers(HttpMethod.GET,  "/api/inmersiones/mis-inmersiones").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/inmersiones/**").permitAll()

                // ── Endpoints de usuario ─────────────────────────────────
                // IMPORTANTE: las rutas LITERALES deben declararse ANTES que
                // el patrón genérico /{id}, de lo contrario Spring Security
                // empareja /perfil como si fuera un {id} y aplica permitAll,
                // dejando Authentication = null en el controlador.
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/perfil").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/usuarios/perfil").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/buscar").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/empresas").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/username/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/{id}").permitAll()

                // ── Todo lo demás requiere JWT válido ────────────────────
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
