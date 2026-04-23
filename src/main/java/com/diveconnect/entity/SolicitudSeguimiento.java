package com.diveconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Solicitud de seguimiento entre dos usuarios. Cuando alguien pulsa
 * "Seguir" sobre otro usuario con perfil privado se crea una en estado
 * PENDIENTE; el destinatario puede aceptarla (crea la relación en la
 * tabla seguidores) o rechazarla.
 *
 * Unicidad por pareja (solicitante, destinatario) en estado PENDIENTE
 * se garantiza a nivel de servicio (query findPendienteEntre).
 */
@Entity
@Table(name = "solicitudes_seguimiento", indexes = {
        @Index(name = "idx_sol_dest",  columnList = "destinatario_id"),
        @Index(name = "idx_sol_solic", columnList = "solicitante_id"),
        @Index(name = "idx_sol_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSeguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
}
