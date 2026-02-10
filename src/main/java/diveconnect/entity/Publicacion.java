package diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "publicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El contenido es obligatorio")
    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "lugar_inmersion", length = 200)
    private String lugarInmersion;

    @Column(name = "profundidad_maxima")
    private Double profundidadMaxima;

    @Column(name = "temperatura_agua")
    private Double temperaturaAgua;

    @Column(name = "visibilidad")
    private Double visibilidad;

    @Column(name = "especies_vistas", length = 500)
    private String especiesVistas;

    @CreationTimestamp
    @Column(name = "fecha_publicacion", updatable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "numero_likes")
    private Integer numeroLikes = 0;

    @Column(name = "numero_comentarios")
    private Integer numeroComentarios = 0;

    // ==============================================
    // RELACIONES
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "likes_publicacion",
        joinColumns = @JoinColumn(name = "publicacion_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> likes = new HashSet<>();
}