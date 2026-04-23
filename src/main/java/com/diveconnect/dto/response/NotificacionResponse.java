package com.diveconnect.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacionResponse {
    private Long id;
    private String tipo;
    private String mensaje;
    private Boolean accionable;
    private Boolean resuelta;
    private Boolean leida;
    private Long entidadRelacionadaId;
    private LocalDateTime fechaCreacion;

    // datos del emisor para renderizar tarjetas
    private Long emisorId;
    private String emisorUsername;
    private String emisorFotoPerfil;
    private String emisorNombreEmpresa;
}
