package com.diveconnect.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.Publicacion;
import com.diveconnect.entity.Usuario;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    
    // Publicaciones de un usuario (ordenadas por fecha)
    List<Publicacion> findByUsuarioOrderByFechaPublicacionDesc(Usuario usuario);
    
    // Todas las publicaciones (paginadas)
    Page<Publicacion> findAllByOrderByFechaPublicacionDesc(Pageable pageable);
    
    // Feed de publicaciones de usuarios seguidos
    @Query("SELECT p FROM Publicacion p WHERE p.usuario IN :usuarios ORDER BY p.fechaPublicacion DESC")
    Page<Publicacion> findPublicacionesDeSeguidos(@Param("usuarios") List<Usuario> usuarios, Pageable pageable);
    
    // Buscar publicaciones por contenido o lugar
    @Query("SELECT p FROM Publicacion p WHERE LOWER(p.contenido) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.lugarInmersion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Publicacion> buscarPorContenido(@Param("keyword") String keyword);
    
    // Contar publicaciones de un usuario
    Long countByUsuario(Usuario usuario);
}