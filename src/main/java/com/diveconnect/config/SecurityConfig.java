package com.diveconnect.config;

import com.diveconnect.security.JwtAuthenticationFilter;
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
            // Desactivar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))

            // Stateless (sin sesión)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Configuración de permisos
            .authorizeHttpRequests(auth -> auth
                // Rutas del frontend
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/pages/**").permitAll()

                // API pública
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/centros-buceo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/inmersiones/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/username/{username}").permitAll()

                // Resto de rutas requiere autenticación
                .anyRequest().authenticated()
            );

        // Filtro JWT antes del filtro de usuario y contraseña
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean para AuthenticationManager (AuthController lo necesita)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Bean para codificación de passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
