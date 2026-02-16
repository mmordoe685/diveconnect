package com.diveconnect.controller;

import com.diveconnect.dto.request.PublicacionRequest;
import com.diveconnect.dto.response.PublicacionResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.PublicacionService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<PublicacionResponse> crearPublicacion(
            @Valid @RequestBody PublicacionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        PublicacionResponse publicacion = publicacionService.crearPublicacion(usuario.getId(), request);
        return new ResponseEntity<>(publicacion, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> obtenerPublicacion(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        PublicacionResponse publicacion = publicacionService.obtenerPublicacion(id, usuario.getId());
        return ResponseEntity.ok(publicacion);
    }

    @GetMapping
    public ResponseEntity<List<PublicacionResponse>> obtenerTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        List<PublicacionResponse> publicaciones = publicacionService.obtenerTodasLasPublicaciones(usuario.getId(), page, size);
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PublicacionResponse>> obtenerPublicacionesDeUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        List<PublicacionResponse> publicaciones = publicacionService.obtenerPublicacionesDeUsuario(usuarioId, usuario.getId());
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PublicacionResponse>> obtenerFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        List<PublicacionResponse> publicaciones = publicacionService.obtenerFeedPersonalizado(usuario.getId(), page, size);
        return ResponseEntity.ok(publicaciones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponse> actualizarPublicacion(
            @PathVariable Long id,
            @Valid @RequestBody PublicacionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        PublicacionResponse publicacion = publicacionService.actualizarPublicacion(id, usuario.getId(), request);
        return ResponseEntity.ok(publicacion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPublicacion(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        publicacionService.eliminarPublicacion(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> darLike(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        publicacionService.darLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> quitarLike(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        publicacionService.quitarLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PublicacionResponse>> buscar(
            @RequestParam String q,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        List<PublicacionResponse> publicaciones = publicacionService.buscarPublicaciones(q, usuario.getId());
        return ResponseEntity.ok(publicaciones);
    }
}