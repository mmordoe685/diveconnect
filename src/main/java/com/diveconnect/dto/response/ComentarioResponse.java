package com.diveconnect.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComentarioResponse {
    private Long id;
    private String contenido;
    private LocalDateTime fechaComentario;
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioFotoPerfil;
    private Long publicacionId;
}