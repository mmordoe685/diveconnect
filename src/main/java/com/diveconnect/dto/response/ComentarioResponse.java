package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComentarioResponse {
    private Long id;
    private String contenido;
    private LocalDateTime fechaComentario;
    
    // Datos del usuario
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioFotoPerfil;
    
    // Datos de la publicación
    private Long publicacionId;
}