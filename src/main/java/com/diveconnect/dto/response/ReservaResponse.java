package com.diveconnect.dto.response;

import com.diveconnect.entity.EstadoReserva;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaResponse {
    private Long id;
    private Integer numeroPersonas;
    private EstadoReserva estado;
    private Double precioTotal;
    private String observaciones;
    private LocalDateTime fechaReserva;
    private LocalDateTime ultimaModificacion;

    // Pago
    private String paymentStatus; // UNPAID, PAID, FAILED
    private String stripeSessionId;
    
    // Datos del usuario
    private Long usuarioId;
    private String usuarioUsername;
    
    // Datos de la inmersión
    private Long inmersionId;
    private String inmersionTitulo;
    private LocalDateTime inmersionFecha;
    
    // Datos del centro
    private Long centroBuceoId;
    private String centroBuceoNombre;
}