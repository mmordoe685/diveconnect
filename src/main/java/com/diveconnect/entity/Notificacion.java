package com.diveconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notificación dirigida a un usuario concreto. Puede ser informativa
 * o accionable (el destinatario debe aceptar / rechazar algo, p.ej.
 * una solicitud de seguimiento).
 */
@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notif_destinatario", columnList = "destinatario_id"),
        @Index(name = "idx_notif_leida",        columnList = "leida")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que recibe la notificación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    /** Usuario que originó el evento (puede ser null para eventos de sistema). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emisor_id")
    private Usuario emisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    /**
     * ID de la entidad relacionada (publicación, reserva, solicitud…).
     * El consumidor lo interpreta según `tipo`.
     */
    @Column(name = "entidad_relacionada_id")
    private Long entidadRelacionadaId;

    /** Texto corto ya renderizado, listo para mostrar. */
    @Column(length = 500)
    private String mensaje;

    /** Si es accionable, el cliente muestra botones (aceptar / rechazar). */
    @Column(nullable = false)
    private Boolean accionable = false;

    /** Para notificaciones accionables: si ya fue resuelta. */
    @Column(nullable = false)
    private Boolean resuelta = false;

    @Column(nullable = false)
    private Boolean leida = false;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false, nullable = false)
    private LocalDateTime fechaCreacion;
}
