package com.diveconnect.controller;

import com.diveconnect.dto.request.InmersionRequest;
import com.diveconnect.dto.response.InmersionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.diveconnect.service.InmersionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inmersiones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InmersionController {

    private final InmersionService inmersionService;

    @PostMapping("/centro/{centroBuceoId}")
    public ResponseEntity<InmersionResponse> crearInmersion(
            @PathVariable Long centroBuceoId,
            @Valid @RequestBody InmersionRequest request) {
        InmersionResponse inmersion = inmersionService.crearInmersion(centroBuceoId, request);
        return new ResponseEntity<>(inmersion, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InmersionResponse> obtenerInmersion(@PathVariable Long id) {
        InmersionResponse inmersion = inmersionService.obtenerInmersion(id);
        return ResponseEntity.ok(inmersion);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<InmersionResponse>> obtenerDisponibles() {
        List<InmersionResponse> inmersiones = inmersionService.obtenerInmersionesDisponibles();
        return ResponseEntity.ok(inmersiones);
    }

    @GetMapping("/proximas")
    public ResponseEntity<List<InmersionResponse>> obtenerProximas() {
        List<InmersionResponse> inmersiones = inmersionService.obtenerInmersionesProximas();
        return ResponseEntity.ok(inmersiones);
    }

    @GetMapping("/centro/{centroBuceoId}")
    public ResponseEntity<List<InmersionResponse>> obtenerInmersionesDeCentro(@PathVariable Long centroBuceoId) {
        List<InmersionResponse> inmersiones = inmersionService.obtenerInmersionesDeCentro(centroBuceoId);
        return ResponseEntity.ok(inmersiones);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InmersionResponse> actualizarInmersion(
            @PathVariable Long id,
            @Valid @RequestBody InmersionRequest request) {
        InmersionResponse inmersion = inmersionService.actualizarInmersion(id, request);
        return ResponseEntity.ok(inmersion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarInmersion(@PathVariable Long id) {
        inmersionService.cancelarInmersion(id);
        return ResponseEntity.noContent().build();
    }
}