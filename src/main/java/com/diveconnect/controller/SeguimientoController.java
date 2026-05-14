package com.diveconnect.controller;

import com.diveconnect.dto.response.SeguimientoEstadoResponse;
import com.diveconnect.service.SeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seguimiento")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    /** Solicitar seguir a un usuario (usuario común → crea solicitud; empresa → follow directo). */
    @PostMapping("/solicitar/{usuarioId}")
    public ResponseEntity<SeguimientoEstadoResponse> solicitar(@PathVariable Long usuarioId,
                                                                Authentication auth) {
        return ResponseEntity.ok(seguimientoService.solicitar(auth.getName(), usuarioId));
    }

    @PostMapping("/aceptar/{solicitudId}")
    public ResponseEntity<SeguimientoEstadoResponse> aceptar(@PathVariable Long solicitudId,
                                                              Authentication auth) {
        return ResponseEntity.ok(seguimientoService.aceptar(auth.getName(), solicitudId));
    }

    @PostMapping("/rechazar/{solicitudId}")
    public ResponseEntity<SeguimientoEstadoResponse> rechazar(@PathVariable Long solicitudId,
                                                               Authentication auth) {
        return ResponseEntity.ok(seguimientoService.rechazar(auth.getName(), solicitudId));
    }

    @DeleteMapping("/dejar/{usuarioId}")
    public ResponseEntity<SeguimientoEstadoResponse> dejarDeSeguir(@PathVariable Long usuarioId,
                                                                    Authentication auth) {
        return ResponseEntity.ok(seguimientoService.dejarDeSeguir(auth.getName(), usuarioId));
    }

    @GetMapping("/estado/{usuarioId}")
    public ResponseEntity<SeguimientoEstadoResponse> estado(@PathVariable Long usuarioId,
                                                             Authentication auth) {
        return ResponseEntity.ok(seguimientoService.estado(auth.getName(), usuarioId));
    }
}
