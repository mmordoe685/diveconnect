package com.diveconnect.controller;

import com.diveconnect.dto.request.HistoriaRequest;
import com.diveconnect.dto.response.HistoriaResponse;
import com.diveconnect.service.HistoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistoriaController {

    private final HistoriaService historiaService;

    /** Todas las historias activas (<24h) agrupadas por usuario, para la barra del feed. */
    @GetMapping
    public ResponseEntity<List<HistoriaResponse.GrupoUsuario>> listarAgrupadas() {
        return ResponseEntity.ok(historiaService.listarAgrupadas());
    }

    /** Historias activas de un usuario concreto (para abrir el visor en orden). */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<HistoriaResponse>> listarDeUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(historiaService.listarDeUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<HistoriaResponse> crear(
            @Valid @RequestBody HistoriaRequest request,
            Authentication authentication) {
        HistoriaResponse resp = historiaService.crear(authentication.getName(), request);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        historiaService.eliminar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
