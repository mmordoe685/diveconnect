package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar por username
    Optional<Usuario> findByUsername(String username);
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Verificar si username existe
    Boolean existsByUsername(String username);
    
    // Verificar si email existe
    Boolean existsByEmail(String email);
    
    // Buscar por tipo de usuario
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);
    
    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();
    
    // Consulta personalizada: empresas activas
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario = 'USUARIO_EMPRESA' AND u.activo = true")
    List<Usuario> findEmpresasActivas();
    
    // Búsqueda por palabra clave en nombre o empresa
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.nombreEmpresa) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Usuario> buscarPorNombre(@Param("keyword") String keyword);
}