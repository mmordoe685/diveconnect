package com.diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inmersiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inmersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "La fecha de inmersión es obligatoria")
    @Column(name = "fecha_inmersion", nullable = false)
    private LocalDateTime fechaInmersion;

    @Column(nullable = false)
    private Integer duracion; // en minutos

    @Column(name = "profundidad_maxima")
    private Double profundidadMaxima;

    @Column(name = "nivel_requerido", length = 50)
    private String nivelRequerido;

    @Column(nullable = false)
    private Double precio;

    @Column(name = "plazas_disponibles")
    private Integer plazasDisponibles;

    @Column(name = "plazas_totales")
    private Integer plazasTotales;

    @Column(length = 200)
    private String ubicacion;

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Column(name = "equipo_incluido", length = 500)
    private String equipoIncluido;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // ==============================================
    // RELACIONES
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_buceo_id", nullable = false)
    private CentroBuceo centroBuceo;

    @OneToMany(mappedBy = "inmersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();
}