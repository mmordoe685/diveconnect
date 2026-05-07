package com.diveconnect.controller;

import com.diveconnect.dto.response.CentroBuceoResponse;
import com.diveconnect.dto.response.InmersionResponse;
import com.diveconnect.dto.response.ReservaResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.service.CentroBuceoService;
import com.diveconnect.service.InmersionService;
import com.diveconnect.service.ReservaService;
import com.diveconnect.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final CentroBuceoService centroBuceoService;
    private final InmersionService inmersionService;
    private final ReservaService reservaService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodos();
        List<CentroBuceoResponse> centros = centroBuceoService.obtenerTodosAdmin();
        List<InmersionResponse> inmersiones = inmersionService.obtenerTodasLasInmersiones();
        List<ReservaResponse> reservas = reservaService.obtenerTodasLasReservas();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarios.size());
        stats.put("totalEmpresas", usuarios.stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.USUARIO_EMPRESA).count());
        stats.put("totalCentros", centros.size());
        stats.put("totalInmersiones", inmersiones.size());
        stats.put("totalReservas", reservas.size());
        stats.put("reservasPendientes", reservas.stream()
                .filter(r -> r.getEstado() != null && r.getEstado().name().equals("PENDIENTE")).count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable Long id,
            @RequestParam TipoUsuario rol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rol));
    }

    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/centros")
    public ResponseEntity<List<CentroBuceoResponse>> listarCentros() {
        return ResponseEntity.ok(centroBuceoService.obtenerTodosAdmin());
    }

    @DeleteMapping("/centros/{id}")
    public ResponseEntity<Void> eliminarCentro(@PathVariable Long id) {
        centroBuceoService.eliminarCentro(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inmersiones")
    public ResponseEntity<List<InmersionResponse>> listarInmersiones() {
        return ResponseEntity.ok(inmersionService.obtenerTodasLasInmersiones());
    }

    @DeleteMapping("/inmersiones/{id}")
    public ResponseEntity<Void> eliminarInmersion(@PathVariable Long id) {
        inmersionService.eliminarInmersion(id, null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservas")
    public ResponseEntity<List<ReservaResponse>> listarReservas() {
        return ResponseEntity.ok(reservaService.obtenerTodasLasReservas());
    }

    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }
}
