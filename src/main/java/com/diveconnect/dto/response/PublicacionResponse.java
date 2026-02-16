package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicacionResponse {
    private Long id;
    private String contenido;
    private String imagenUrl;
    private String videoUrl;
    private String lugarInmersion;
    private Double profundidadMaxima;
    private Double temperaturaAgua;
    private Double visibilidad;
    private String especiesVistas;
    private LocalDateTime fechaPublicacion;
    private Integer numeroLikes;
    private Integer numeroComentarios;
    
    // Datos del usuario
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioFotoPerfil;
    
    // Indicadores
    private Boolean likedByCurrentUser;
}