package com.diveconnect.controller;

import com.diveconnect.dto.response.NotificacionResponse;
import com.diveconnect.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(notificacionService.listarDeUsuario(auth.getName()));
    }

    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication auth) {
        long n = notificacionService.contarNoLeidas(auth.getName());
        return ResponseEntity.ok(Map.of("count", n));
    }

    @PostMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Authentication auth) {
        notificacionService.marcarLeida(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasLeidas(Authentication auth) {
        int n = notificacionService.marcarTodasLeidas(auth.getName());
        return ResponseEntity.ok(Map.of("actualizadas", n));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        notificacionService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
