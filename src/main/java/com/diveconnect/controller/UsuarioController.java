package com.diveconnect.controller;

import com.diveconnect.dto.request.ActualizarPerfilRequest;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /** GET /api/usuarios/perfil — perfil del usuario autenticado */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfilActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(authentication.getName());
        return ResponseEntity.ok(usuario);
    }

    /** PUT /api/usuarios/perfil — actualizar datos del perfil propio */
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @RequestBody ActualizarPerfilRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioResponse actual = usuarioService.obtenerPerfilPorUsername(authentication.getName());
        return ResponseEntity.ok(usuarioService.actualizarPerfil(actual.getId(), request));
    }

    /** GET /api/usuarios/{id} — perfil público por ID */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    /** GET /api/usuarios/username/{username} — perfil público por username */
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponse> obtenerPerfilPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPorUsername(username));
    }

    /** POST /api/usuarios/{id}/seguir */
    @PostMapping("/{id}/seguir")
    public ResponseEntity<Void> seguir(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioResponse actual = usuarioService.obtenerPerfilPorUsername(authentication.getName());
        usuarioService.seguirUsuario(actual.getId(), id);
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/usuarios/{id}/seguir */
    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<Void> dejarDeSeguir(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UsuarioResponse actual = usuarioService.obtenerPerfilPorUsername(authentication.getName());
        usuarioService.dejarDeSeguir(actual.getId(), id);
        return ResponseEntity.ok().build();
    }

    /** GET /api/usuarios/{id}/seguidores */
    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioResponse>> obtenerSeguidores(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerSeguidores(id));
    }

    /** GET /api/usuarios/{id}/siguiendo */
    @GetMapping("/{id}/siguiendo")
    public ResponseEntity<List<UsuarioResponse>> obtenerSiguiendo(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerSiguiendo(id));
    }

    /** GET /api/usuarios/buscar?q=... */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponse>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(usuarioService.buscarUsuarios(q));
    }

    /** GET /api/usuarios/empresas */
    @GetMapping("/empresas")
    public ResponseEntity<List<UsuarioResponse>> obtenerEmpresas() {
        return ResponseEntity.ok(usuarioService.obtenerEmpresas());
    }
}
