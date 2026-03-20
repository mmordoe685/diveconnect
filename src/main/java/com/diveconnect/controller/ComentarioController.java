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

/**
 * Controller para comentarios.
 * Los endpoints están bajo /api/publicaciones/{publicacionId}/comentarios
 * para seguir las convenciones REST de recursos anidados.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioService usuarioService;

    /**
     * GET /api/publicaciones/{publicacionId}/comentarios
     * Devuelve todos los comentarios de una publicación, ordenados por fecha ascendente.
     * Endpoint público — cualquier usuario autenticado puede ver comentarios.
     */
    @GetMapping("/api/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> obtenerComentarios(
            @PathVariable Long publicacionId) {
        return ResponseEntity.ok(
            comentarioService.obtenerComentariosDePublicacion(publicacionId)
        );
    }

    /**
     * POST /api/publicaciones/{publicacionId}/comentarios
     * Crea un comentario en una publicación.
     * Requiere autenticación.
     */
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

    /**
     * DELETE /api/comentarios/{id}
     * Elimina un comentario. Solo puede eliminarlo su autor.
     */
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