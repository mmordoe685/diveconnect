package com.diveconnect.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InmersionRequest {
    
    @NotBlank(message = "El título es obligatorio")
    private String titulo;
    
    private String descripcion;
    
    @NotNull(message = "La fecha de inmersión es obligatoria")
    private LocalDateTime fechaInmersion;
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer duracion; // en minutos
    
    private Double profundidadMaxima;
    private String nivelRequerido;
    
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;
    
    @NotNull(message = "Las plazas totales son obligatorias")
    @Min(value = 1, message = "Debe haber al menos 1 plaza")
    private Integer plazasTotales;
    
    private String ubicacion;
    private Double latitud;
    private Double longitud;
    private String equipoIncluido;
    private String imagenUrl;
}