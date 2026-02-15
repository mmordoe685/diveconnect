package com.diveconnect.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

import com.diveconnect.entity.EstadoReserva;

@Data
public class ReservaResponse {
    private Long id;
    private Integer numeroPersonas;
    private EstadoReserva estado;
    private Double precioTotal;
    private String observaciones;
    private LocalDateTime fechaReserva;
    private Long usuarioId;
    private String usuarioUsername;
    private Long inmersionId;
    private String inmersionTitulo;
    private LocalDateTime inmersionFecha;
    private Long centroBuceoId;
    private String centroBuceoNombre;
}