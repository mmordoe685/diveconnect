package diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centros_buceo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CentroBuceo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 50)
    private String pais;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    @Column(length = 500)
    private String certificaciones;

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "valoracion_promedio")
    private Double valoracionPromedio = 0.0;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    // ==============================================
    // RELACIONES
    // ==============================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "centroBuceo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inmersion> inmersiones = new ArrayList<>();

    @OneToMany(mappedBy = "centroBuceo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();
}