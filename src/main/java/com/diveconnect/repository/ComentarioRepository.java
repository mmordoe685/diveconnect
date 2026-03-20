package com.diveconnect.repository;

import com.diveconnect.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // Usado por ComentarioService.obtenerComentariosPorPublicacion()
    List<Comentario> findByPublicacionIdOrderByFechaComentarioAsc(Long publicacionId);

    // Útil para futuros listados por usuario
    List<Comentario> findByUsuarioId(Long usuarioId);

    // Contador rápido sin cargar objetos completos
    long countByPublicacionId(Long publicacionId);
}