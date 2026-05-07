package com.diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puntos_mapa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoMapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 1500)
    private String descripcion;

    @NotNull
    @Column(nullable = false)
    private Double latitud;

    @NotNull
    @Column(nullable = false)
    private Double longitud;

    @Column(name = "profundidad_metros")
    private Double profundidadMetros;

    @Column(name = "temperatura_agua")
    private Double temperaturaAgua;

    @Column(name = "presion_bar")
    private Double presionBar;

    @Column(name = "corriente", length = 100)
    private String corriente;

    @Column(name = "visibilidad_metros")
    private Double visibilidadMetros;

    // Lista de especies como texto separado por comas
    @Column(name = "especies_vistas", length = 800)
    private String especiesVistas;

    @Column(name = "fecha_observacion")
    private LocalDateTime fechaObservacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inmersion_id")
    private Inmersion inmersion;

    @OneToMany(mappedBy = "puntoMapa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FotoPuntoMapa> fotos = new ArrayList<>();
}
