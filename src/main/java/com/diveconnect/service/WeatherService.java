package com.diveconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WeatherService {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public WeatherService(@Value("${openweather.api-key:}") String apiKey,
                          @Value("${openweather.base-url:https://api.openweathermap.org/data/2.5}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerTiempo(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            return mockResponse(lat, lon);
        }
        try {
            Map<String, Object> raw = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .queryParam("lang", "es")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (raw == null) return mockResponse(lat, lon);

            Map<String, Object> out = new HashMap<>();
            out.put("source", "openweathermap");
            out.put("lat", lat);
            out.put("lon", lon);
            Map<String, Object> main = (Map<String, Object>) raw.get("main");
            Map<String, Object> wind = (Map<String, Object>) raw.get("wind");
            Object weatherArr = raw.get("weather");

            if (main != null) {
                out.put("temperatura", main.get("temp"));
                out.put("sensacionTermica", main.get("feels_like"));
                out.put("presion", main.get("pressure"));
                out.put("humedad", main.get("humidity"));
            }
            if (wind != null) {
                out.put("viento", wind.get("speed"));
                out.put("vientoDireccion", wind.get("deg"));
            }
            if (weatherArr instanceof java.util.List<?> list && !list.isEmpty()) {
                Map<String, Object> w0 = (Map<String, Object>) list.get(0);
                out.put("condicion", w0.get("main"));
                out.put("descripcion", w0.get("description"));
                out.put("icono", w0.get("icon"));
            }
            out.put("ciudad", raw.get("name"));
            return out;
        } catch (Exception e) {
            log.warn("No se pudo obtener el tiempo desde OpenWeatherMap", e);
            Map<String, Object> err = mockResponse(lat, lon);
            err.put("error", "No se pudo obtener el tiempo real; usando datos de demo");
            return err;
        }
    }

    private Map<String, Object> mockResponse(double lat, double lon) {
        Map<String, Object> m = new HashMap<>();
        m.put("source", "mock");
        m.put("note", "Configura OPENWEATHER_API_KEY para datos reales");
        m.put("lat", lat);
        m.put("lon", lon);
        m.put("temperatura", 21.5);
        m.put("sensacionTermica", 22.0);
        m.put("presion", 1014);
        m.put("humedad", 72);
        m.put("viento", 3.2);
        m.put("vientoDireccion", 180);
        m.put("condicion", "Clear");
        m.put("descripcion", "cielo despejado");
        m.put("icono", "01d");
        m.put("ciudad", "Zona costera");
        return m;
    }
}
