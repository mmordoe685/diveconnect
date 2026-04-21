package com.diveconnect.controller;

import com.diveconnect.dto.request.PuntoMapaRequest;
import com.diveconnect.dto.response.PuntoMapaResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.PuntoMapaService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puntos-mapa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PuntoMapaController {

    private final PuntoMapaService puntoMapaService;
    private final UsuarioService usuarioService;

    /** GET público. Cualquiera ve los puntos del mapa. */
    @GetMapping
    public ResponseEntity<List<PuntoMapaResponse>> listar() {
        return ResponseEntity.ok(puntoMapaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuntoMapaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(puntoMapaService.obtener(id));
    }

    @GetMapping("/mis-puntos")
    public ResponseEntity<List<PuntoMapaResponse>> misPuntos(Authentication auth) {
        UsuarioResponse me = usuarioService.obtenerPerfilPorUsername(auth.getName());
        return ResponseEntity.ok(puntoMapaService.listarPorAutor(me.getId()));
    }

    /** POST — sólo empresa/admin. */
    @PostMapping
    public ResponseEntity<PuntoMapaResponse> crear(@Valid @RequestBody PuntoMapaRequest req,
                                                   Authentication auth) {
        UsuarioResponse me = usuarioService.obtenerPerfilPorUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(puntoMapaService.crear(me.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        UsuarioResponse me = usuarioService.obtenerPerfilPorUsername(auth.getName());
        puntoMapaService.eliminar(id, me.getId());
        return ResponseEntity.noContent().build();
    }
}
