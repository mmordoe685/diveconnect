package com.diveconnect.controller;

import com.diveconnect.dto.request.CentroBuceoRequest;
import com.diveconnect.dto.response.CentroBuceoResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.service.CentroBuceoService;
import com.diveconnect.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros-buceo")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CentroBuceoController {

    private final CentroBuceoService centroBuceoService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<CentroBuceoResponse> crearCentro(
            @Valid @RequestBody CentroBuceoRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        CentroBuceoResponse centro = centroBuceoService.crearCentroBuceo(usuario.getId(), request);
        return new ResponseEntity<>(centro, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroBuceoResponse> obtenerCentro(@PathVariable Long id) {
        CentroBuceoResponse centro = centroBuceoService.obtenerCentro(id);
        return ResponseEntity.ok(centro);
    }

    @GetMapping
    public ResponseEntity<List<CentroBuceoResponse>> obtenerTodos() {
        List<CentroBuceoResponse> centros = centroBuceoService.obtenerTodos();
        return ResponseEntity.ok(centros);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CentroBuceoResponse>> buscar(@RequestParam String q) {
        List<CentroBuceoResponse> centros = centroBuceoService.buscarCentros(q);
        return ResponseEntity.ok(centros);
    }

    @GetMapping("/mi-centro")
    public ResponseEntity<CentroBuceoResponse> obtenerMiCentro(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorUsername(username);
        CentroBuceoResponse centro = centroBuceoService.obtenerCentroDeUsuario(usuario.getId());
        return ResponseEntity.ok(centro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroBuceoResponse> actualizarCentro(
            @PathVariable Long id,
            @Valid @RequestBody CentroBuceoRequest request) {
        CentroBuceoResponse centro = centroBuceoService.actualizarCentro(id, request);
        return ResponseEntity.ok(centro);
    }
}