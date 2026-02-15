package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface CentroBuceoRepository extends JpaRepository<CentroBuceo, Long> {
    
    // Buscar centro por usuario asociado
    Optional<CentroBuceo> findByUsuario(Usuario usuario);
    
    // Centros activos
    List<CentroBuceo> findByActivoTrue();
    
    // Buscar por ciudad
    List<CentroBuceo> findByCiudad(String ciudad);
    
    // Buscar por país
    List<CentroBuceo> findByPais(String pais);
    
    // Búsqueda por nombre o ciudad
    @Query("SELECT c FROM CentroBuceo c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.ciudad) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CentroBuceo> buscarPorNombreOCiudad(@Param("keyword") String keyword);
    
    // Centros mejor valorados
    @Query("SELECT c FROM CentroBuceo c WHERE c.activo = true ORDER BY c.valoracionPromedio DESC")
    List<CentroBuceo> findTopRated();
}