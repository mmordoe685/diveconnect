package com.diveconnect.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_punto_mapa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FotoPuntoMapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(length = 200)
    private String especieAvistada;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(length = 500)
    private String descripcion;

    @CreationTimestamp
    @Column(name = "fecha_subida", updatable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_mapa_id", nullable = false)
    private PuntoMapa puntoMapa;
}
