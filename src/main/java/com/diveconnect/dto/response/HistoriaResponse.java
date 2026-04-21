package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoriaResponse {
    private Long id;
    private String mediaUrl;
    private String mediaType;
    private String texto;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime expiraEn;
    private Long usuarioId;
    private String usuarioUsername;
    private String usuarioFotoPerfil;
    private String usuarioEmpresa;

    /** Agrupación por usuario para la barra de historias del feed. */
    @Data
    public static class GrupoUsuario {
        private Long usuarioId;
        private String usuarioUsername;
        private String usuarioFotoPerfil;
        private String usuarioEmpresa;
        private List<HistoriaResponse> historias;
        private boolean tieneNoVistas = true;
    }
}
