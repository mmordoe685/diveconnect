package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PuntoMapaResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private Double profundidadMetros;
    private Double temperaturaAgua;
    private Double presionBar;
    private String corriente;
    private Double visibilidadMetros;
    private String especiesVistas;
    private LocalDateTime fechaObservacion;
    private LocalDateTime fechaCreacion;

    private Long autorId;
    private String autorUsername;
    private String autorEmpresa; // nombreEmpresa

    private Long inmersionId;
    private String inmersionTitulo;

    private List<FotoMapaDto> fotos;

    @Data
    public static class FotoMapaDto {
        private Long id;
        private String url;
        private String especieAvistada;
        private LocalDateTime fechaHora;
        private String descripcion;
    }
}
