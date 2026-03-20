package com.diveconnect.controller;

import com.diveconnect.dto.request.InmersionRequest;
import com.diveconnect.dto.response.InmersionResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.InmersionService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inmersiones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InmersionController {

    private final InmersionService inmersionService;
    private final UsuarioService usuarioService;

    /**
     * GET /api/inmersiones/disponibles
     * Endpoint PÚBLICO — sin autenticación. Devuelve inmersiones activas con plazas.
     * Usado por Inmersiones.html al cargar la página.
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<InmersionResponse>> obtenerDisponibles() {
        return ResponseEntity.ok(inmersionService.obtenerInmersionesDisponibles());
    }

    /**
     * GET /api/inmersiones
     * Lista todas las inmersiones (para administración).
     */
    @GetMapping
    public ResponseEntity<List<InmersionResponse>> obtenerTodas() {
        return ResponseEntity.ok(inmersionService.obtenerTodasLasInmersiones());
    }

    /**
     * GET /api/inmersiones/{id}
     * Detalle de una inmersión específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InmersionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(inmersionService.obtenerInmersion(id));
    }

    /**
     * GET /api/inmersiones/centro/{centroId}
     * Inmersiones de un centro de buceo específico.
     */
    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<InmersionResponse>> obtenerPorCentro(
            @PathVariable Long centroId) {
        return ResponseEntity.ok(
            inmersionService.obtenerInmersionesPorCentro(centroId));
    }

    /**
     * POST /api/inmersiones
     * Crea una nueva inmersión. Solo empresas/admins.
     */
    @PostMapping
    public ResponseEntity<InmersionResponse> crear(
            @Valid @RequestBody InmersionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario =
            usuarioService.obtenerPerfilPorUsername(username);
        return new ResponseEntity<>(
            inmersionService.crearInmersion(usuario.getId(), request),
            HttpStatus.CREATED);
    }

    /**
     * DELETE /api/inmersiones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario =
            usuarioService.obtenerPerfilPorUsername(username);
        inmersionService.eliminarInmersion(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}