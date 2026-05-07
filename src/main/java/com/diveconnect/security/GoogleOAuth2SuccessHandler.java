package com.diveconnect.security;

import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            log.error("OAuth2 login sin email - no se puede continuar");
            response.sendRedirect(frontendUrl + "/pages/login.html?oauth_error=no_email");
            return;
        }

        Usuario usuario = upsertUsuario(email, name, picture);

        // Generar JWT como lo hace el flujo normal
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        // Redirigir al frontend con los datos
        String redirectUrl = String.format(
            "%s/pages/oauth-callback.html?token=%s&username=%s&email=%s&tipo=%s",
            frontendUrl,
            URLEncoder.encode(token, StandardCharsets.UTF_8),
            URLEncoder.encode(usuario.getUsername(), StandardCharsets.UTF_8),
            URLEncoder.encode(usuario.getEmail(), StandardCharsets.UTF_8),
            URLEncoder.encode(usuario.getTipoUsuario().name(), StandardCharsets.UTF_8)
        );

        log.info("OAuth2 login OK para {} → redirigiendo al frontend", email);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private Usuario upsertUsuario(String email, String name, String picture) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(email);

        if (existente.isPresent()) {
            Usuario u = existente.get();
            // Actualizar foto si todavía no tiene
            if ((u.getFotoPerfil() == null || u.getFotoPerfil().isBlank()) && picture != null) {
                u.setFotoPerfil(picture);
                usuarioRepository.save(u);
            }
            return u;
        }

        // Crear usuario nuevo
        Usuario nuevo = new Usuario();
        nuevo.setEmail(email);
        nuevo.setUsername(generarUsername(email, name));
        // Password random — el usuario nunca la usará porque siempre entra vía Google
        nuevo.setPassword("{oauth2}" + java.util.UUID.randomUUID());
        nuevo.setFotoPerfil(picture);
        nuevo.setBiografia("Usuario registrado con Google");
        nuevo.setTipoUsuario(TipoUsuario.USUARIO_COMUN);
        nuevo.setActivo(true);
        nuevo.setNumeroInmersiones(0);

        Usuario guardado = usuarioRepository.save(nuevo);
        log.info("Usuario creado vía Google OAuth2: {} ({})", guardado.getUsername(), email);
        return guardado;
    }

    private String generarUsername(String email, String name) {
        String base;
        if (name != null && !name.isBlank()) {
            base = name.toLowerCase()
                      .replaceAll("[^a-z0-9._]", "_")
                      .replaceAll("_+", "_");
        } else {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9._]", "_");
        }

        if (base.length() > 40) base = base.substring(0, 40);
        if (base.length() < 3) base = "user_" + base;

        String candidato = base;
        int sufijo = 1;
        while (usuarioRepository.existsByUsername(candidato)) {
            candidato = base + "_" + sufijo;
            sufijo++;
            if (sufijo > 1000) {
                candidato = base + "_" + System.currentTimeMillis();
                break;
            }
        }
        return candidato;
    }
}
