package com.diveconnect.controller;

import com.diveconnect.dto.response.SearchResponse;
import com.diveconnect.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

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
