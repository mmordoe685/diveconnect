package com.diveconnect.dto.response;

import lombok.Data;

@Data
public class SeguimientoEstadoResponse {
    /** NO_SIGUE | SOLICITADO | SIGUIENDO | PROPIO */
    private String estado;
    private Long solicitudPendienteId;
}
