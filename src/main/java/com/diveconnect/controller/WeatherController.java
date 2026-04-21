package com.diveconnect.controller;

import com.diveconnect.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtener(
            @RequestParam double lat,
            @RequestParam double lon) {
        return ResponseEntity.ok(weatherService.obtenerTiempo(lat, lon));
    }
}
