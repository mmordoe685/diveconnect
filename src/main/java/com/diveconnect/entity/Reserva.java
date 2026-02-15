package com.diveconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_personas", nullable = false)
    private Integer numeroPersonas = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "precio_total", nullable = false)
    private Double precioTotal;

    @Column(length = 500)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_reserva", updatable = false)
    private LocalDateTime fechaReserva;

    @UpdateTimestamp
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    // ==============================================
    // RELACIONES
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmersion_id", nullable = false)
    private Inmersion inmersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_buceo_id", nullable = false)
    private CentroBuceo centroBuceo;
}