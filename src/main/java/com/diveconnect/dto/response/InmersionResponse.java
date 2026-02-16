package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InmersionResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInmersion;
    private Integer duracion;
    private Double profundidadMaxima;
    private String nivelRequerido;
    private Double precio;
    private Integer plazasDisponibles;
    private Integer plazasTotales;
    private String ubicacion;
    private Double latitud;
    private Double longitud;
    private String equipoIncluido;
    private String imagenUrl;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
    
    // Datos del centro de buceo
    private Long centroBuceoId;
    private String centroBuceoNombre;
    private String centroBuceoCiudad;
}