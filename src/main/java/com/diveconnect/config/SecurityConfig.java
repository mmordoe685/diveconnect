package com.diveconnect.config;

import com.diveconnect.security.GoogleOAuth2SuccessHandler;
import com.diveconnect.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;

    @Value("${app.google-oauth-enabled:false}")
    private boolean googleOAuthEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"message\":\"No autorizado. Por favor inicia sesión.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"message\":\"Acceso denegado.\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**",
                    "/pages/**", "/images/**",
                    "/uploads/**",
                    "/favicon.ico",
                    "/robots.txt", "/sitemap.xml"
                ).permitAll()

                .requestMatchers(
                    "/actuator/health", "/actuator/health/**", "/actuator/info"
                ).permitAll()

                .requestMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs", "/v3/api-docs/**",
                    "/swagger-resources/**", "/webjars/**"
                ).permitAll()

                .requestMatchers(HttpMethod.POST, "/api/uploads").authenticated()

                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/centros-buceo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/inmersiones/disponibles").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/inmersiones/mis-inmersiones").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/inmersiones/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/puntos-mapa/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/weather").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/payments/config").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/paypal/config").permitAll()
                .requestMatchers("/api/paypal/**").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/search/**").permitAll()

                .requestMatchers("/api/notificaciones/**").authenticated()
                .requestMatchers("/api/seguimiento/**").authenticated()

                .requestMatchers(HttpMethod.GET,  "/api/usuarios/perfil").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/usuarios/perfil").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/buscar").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/empresas").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/username/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/usuarios/{id}").permitAll()

                .anyRequest().authenticated()
            );

        // OAuth2 Login sólo se habilita si está configurado con credenciales reales
        if (googleOAuthEnabled) {
            http.oauth2Login(oauth2 -> oauth2
                .successHandler(googleOAuth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/pages/login.html?oauth_error=" +
                        java.net.URLEncoder.encode(exception.getMessage(), "UTF-8"));
                })
            );
        }

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
