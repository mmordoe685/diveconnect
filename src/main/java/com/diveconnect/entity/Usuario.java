package com.diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @Column(length = 500)
    private String biografia;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "nivel_certificacion", length = 50)
    private String nivelCertificacion;

    @Column(name = "numero_inmersiones")
    private Integer numeroInmersiones = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUsuario tipoUsuario = TipoUsuario.USUARIO_COMUN;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // ==============================================
    // RELACIONES
    // ==============================================

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Publicacion> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    // Usuarios que este usuario sigue
    @ManyToMany
    @JoinTable(
        name = "seguidores",
        joinColumns = @JoinColumn(name = "seguidor_id"),
        inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    private Set<Usuario> siguiendo = new HashSet<>();

    // Usuarios que siguen a este usuario
    @ManyToMany(mappedBy = "siguiendo")
    private Set<Usuario> seguidores = new HashSet<>();

    // ==============================================
    // CAMPOS ADICIONALES PARA EMPRESAS
    // ==============================================

    @Column(name = "nombre_empresa", length = 100)
    private String nombreEmpresa;

    @Column(name = "descripcion_empresa", length = 1000)
    private String descripcionEmpresa;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String sitioWeb;
}