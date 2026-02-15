package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.Comentario;
import com.diveconnect.entity.Publicacion;
import com.diveconnect.entity.Usuario;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
    // Comentarios de una publicación
    List<Comentario> findByPublicacionOrderByFechaComentarioDesc(Publicacion publicacion);
    
    // Comentarios de un usuario
    List<Comentario> findByUsuarioOrderByFechaComentarioDesc(Usuario usuario);
    
    // Contar comentarios de una publicación
    Long countByPublicacion(Publicacion publicacion);
    
    // Contar comentarios de un usuario
    Long countByUsuario(Usuario usuario);
}