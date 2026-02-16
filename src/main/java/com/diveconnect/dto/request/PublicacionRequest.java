package com.diveconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublicacionRequest {
    
    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;
    
    private String imagenUrl;
    private String videoUrl;
    private String lugarInmersion;
    private Double profundidadMaxima;
    private Double temperaturaAgua;
    private Double visibilidad;
    private String especiesVistas;
}