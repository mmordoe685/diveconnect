package com.diveconnect.controller;

import com.diveconnect.dto.request.ActualizarPerfilRequest;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.UsuarioService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerPerfilActual(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.obtenerPerfil(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponse> obtenerPerfilPorUsername(@PathVariable String username) {
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @RequestBody ActualizarPerfilRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuarioActual = usuarioService.obtenerPerfilPorUsername(username);
        UsuarioResponse usuarioActualizado = usuarioService.actualizarPerfil(usuarioActual.getId(), request);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PostMapping("/{id}/seguir")
    public ResponseEntity<Void> seguir(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuarioActual = usuarioService.obtenerPerfilPorUsername(username);
        usuarioService.seguirUsuario(usuarioActual.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<Void> dejarDeSeguir(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuarioActual = usuarioService.obtenerPerfilPorUsername(username);
        usuarioService.dejarDeSeguir(usuarioActual.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioResponse>> obtenerSeguidores(@PathVariable Long id) {
        List<UsuarioResponse> seguidores = usuarioService.obtenerSeguidores(id);
        return ResponseEntity.ok(seguidores);
    }

    @GetMapping("/{id}/siguiendo")
    public ResponseEntity<List<UsuarioResponse>> obtenerSiguiendo(@PathVariable Long id) {
        List<UsuarioResponse> siguiendo = usuarioService.obtenerSiguiendo(id);
        return ResponseEntity.ok(siguiendo);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponse>> buscar(@RequestParam String q) {
        List<UsuarioResponse> usuarios = usuarioService.buscarUsuarios(q);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<UsuarioResponse>> obtenerEmpresas() {
        List<UsuarioResponse> empresas = usuarioService.obtenerEmpresas();
        return ResponseEntity.ok(empresas);
    }
}