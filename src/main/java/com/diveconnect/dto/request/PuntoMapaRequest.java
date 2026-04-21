package com.diveconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PuntoMapaRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    private Double profundidadMetros;
    private Double temperaturaAgua;
    private Double presionBar;
    private String corriente;
    private Double visibilidadMetros;
    private String especiesVistas;
    private LocalDateTime fechaObservacion;

    // Opcional: asociar a una inmersión existente
    private Long inmersionId;

    // Fotos iniciales (urls externas o base64)
    private List<FotoInput> fotos;

    @Data
    public static class FotoInput {
        private String url;
        private String especieAvistada;
        private LocalDateTime fechaHora;
        private String descripcion;
    }
}
