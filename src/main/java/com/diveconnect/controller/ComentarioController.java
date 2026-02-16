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
@RequestMapping("/api/publicaciones/{publicacionId}/comentarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<ComentarioResponse> crearComentario(
            @PathVariable Long publicacionId,
            @Valid @RequestBody ComentarioRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        ComentarioResponse comentario = comentarioService.crearComentario(publicacionId, usuario.getId(), request);
        return new ResponseEntity<>(comentario, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> obtenerComentarios(@PathVariable Long publicacionId) {
        List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosDePublicacion(publicacionId);
        return ResponseEntity.ok(comentarios);
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable Long publicacionId,
            @PathVariable Long comentarioId,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        comentarioService.eliminarComentario(comentarioId, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}