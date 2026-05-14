package com.diveconnect.controller;

import com.diveconnect.dto.request.ComentarioRequest;
import com.diveconnect.dto.response.ComentarioResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.ComentarioService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioService usuarioService;

    @GetMapping("/api/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> obtenerComentarios(
            @PathVariable Long publicacionId) {
        return ResponseEntity.ok(
            comentarioService.obtenerComentariosPorPublicacion(publicacionId)
        );
    }

    @PostMapping("/api/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<ComentarioResponse> crearComentario(
            @PathVariable Long publicacionId,
            @Valid @RequestBody ComentarioRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario =
            usuarioService.obtenerPerfilPorUsername(username);
        ComentarioResponse comentario =
            comentarioService.crearComentario(usuario.getId(), publicacionId, request);
        return new ResponseEntity<>(comentario, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/comentarios/{id}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario =
            usuarioService.obtenerPerfilPorUsername(username);
        comentarioService.eliminarComentario(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
