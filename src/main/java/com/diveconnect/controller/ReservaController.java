package com.diveconnect.controller;

import com.diveconnect.dto.request.ReservaRequest;
import com.diveconnect.dto.response.CentroBuceoResponse;
import com.diveconnect.dto.response.ReservaResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.CentroBuceoService;
import com.diveconnect.service.ReservaService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;
    private final CentroBuceoService centroBuceoService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(
            @Valid @RequestBody ReservaRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        ReservaResponse reserva = reservaService.crearReserva(usuario.getId(), request);
        return new ResponseEntity<>(reserva, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable Long id) {
        ReservaResponse reserva = reservaService.obtenerReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaResponse>> obtenerMisReservas(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        List<ReservaResponse> reservas = reservaService.obtenerReservasDeUsuario(usuario.getId());
        return ResponseEntity.ok(reservas);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmarReserva(@PathVariable Long id) {
        ReservaResponse reserva = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(reserva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Long id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mi-centro")
    public ResponseEntity<List<ReservaResponse>> reservasMiCentro(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        Optional<CentroBuceoResponse> centro = centroBuceoService.obtenerCentroPorUsuario(usuario.getId());
        if (centro.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(reservaService.obtenerReservasDeCentro(centro.get().getId()));
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<ReservaResponse> completarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.completarReserva(id));
    }
}