package com.diveconnect.controller;

import com.diveconnect.dto.response.SearchResponse;
import com.diveconnect.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    /**
     * Búsqueda universal. Todos los parámetros son opcionales.
     *
     * @param q         palabras clave (aplica a username/empresa/título/descripción/ubicación)
     * @param tipo      "todo" | "usuarios" | "empresas" | "inmersiones" (default "todo")
     * @param lat,lon   coordenadas para distancia y fallback de proximidad
     * @param profMin   profundidad mínima (m) para inmersiones
     * @param profMax   profundidad máxima (m)
     * @param precioMax precio máximo (€)
     * @param nivel     filtro por nivel requerido (ej. "Open Water")
     */
    @GetMapping
    public ResponseEntity<SearchResponse> buscar(
            @RequestParam(value = "q",         required = false) String q,
            @RequestParam(value = "tipo",      required = false) String tipo,
            @RequestParam(value = "lat",       required = false) Double lat,
            @RequestParam(value = "lon",       required = false) Double lon,
            @RequestParam(value = "profMin",   required = false) Double profMin,
            @RequestParam(value = "profMax",   required = false) Double profMax,
            @RequestParam(value = "precioMax", required = false) Double precioMax,
            @RequestParam(value = "nivel",     required = false) String nivel) {
        return ResponseEntity.ok(
                searchService.buscar(q, tipo, lat, lon, profMin, profMax, precioMax, nivel));
    }
}
