package com.diveconnect.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.diveconnect.dto.request.LoginRequest;
import com.diveconnect.dto.request.RegistroRequest;
import com.diveconnect.dto.response.AuthResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.security.JwtUtil;
import com.diveconnect.security.UserDetailsServiceImpl;
import com.diveconnect.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse usuario = usuarioService.registrarUsuario(request);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsernameOrEmail());
        String token = jwtUtil.generateToken(userDetails);
        
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(userDetails.getUsername());
        
        return ResponseEntity.ok(new AuthResponse(token, usuario));
    }
}