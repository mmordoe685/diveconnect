package com.diveconnect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Historia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "media_url", length = 1500, nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20, nullable = false)
    private MediaType mediaType = MediaType.FOTO;

    @Column(length = 500)
    private String texto;

    @CreationTimestamp
    @Column(name = "fecha_publicacion", updatable = false, nullable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public enum MediaType { FOTO, VIDEO }

    @PrePersist
    private void asignarExpiracion() {
        if (expiraEn == null) {
            expiraEn = LocalDateTime.now().plusHours(24);
        }
    }
}
