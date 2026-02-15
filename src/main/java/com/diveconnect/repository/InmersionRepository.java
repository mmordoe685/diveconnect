package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.Inmersion;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InmersionRepository extends JpaRepository<Inmersion, Long> {
    
    // Inmersiones activas de un centro
    List<Inmersion> findByCentroBuceoAndActivaTrue(CentroBuceo centroBuceo);
    
    // Todas las inmersiones activas ordenadas por fecha
    List<Inmersion> findByActivaTrueOrderByFechaInmersionAsc();
    
    // Inmersiones próximas desde una fecha
    @Query("SELECT i FROM Inmersion i WHERE i.activa = true AND i.fechaInmersion >= :fechaInicio")
    List<Inmersion> findInmersionesProximas(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Inmersiones con plazas disponibles
    @Query("SELECT i FROM Inmersion i WHERE i.activa = true AND i.plazasDisponibles > 0 ORDER BY i.fechaInmersion ASC")
    List<Inmersion> findInmersionesDisponibles();
    
    // Buscar por título o ubicación
    @Query("SELECT i FROM Inmersion i WHERE LOWER(i.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.ubicacion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Inmersion> buscarPorTituloOUbicacion(@Param("keyword") String keyword);
}