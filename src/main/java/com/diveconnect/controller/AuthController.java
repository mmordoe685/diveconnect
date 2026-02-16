package com.diveconnect.controller;

import com.diveconnect.dto.request.LoginRequest;
import com.diveconnect.dto.request.RegistroRequest;
import com.diveconnect.dto.response.AuthResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.security.JwtUtil;
import com.diveconnect.security.UserDetailsServiceImpl;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse usuario = usuarioService.registrarUsuario(request);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Autenticar usuario
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(),
                request.getPassword()
            )
        );

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsernameOrEmail());
        
        // Generar token
        String token = jwtUtil.generateToken(userDetails);
        
        // Obtener información del usuario
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(userDetails.getUsername());
        
        // Crear respuesta
        AuthResponse response = new AuthResponse(token, usuario);
        
        return ResponseEntity.ok(response);
    }
}