package com.diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El contenido del comentario es obligatorio")
    @Column(nullable = false, length = 500)
    private String contenido;

    @CreationTimestamp
    @Column(name = "fecha_comentario", updatable = false)
    private LocalDateTime fechaComentario;

    // ==============================================
    // RELACIONES
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;
}