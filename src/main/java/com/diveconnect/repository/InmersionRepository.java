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
    
    // Inmersiones activos de un centro
    List<Inmersion> findByCentroBuceoAndActivoTrue(CentroBuceo centroBuceo);
    
    // Todas las inmersiones activos ordenadas por fecha
    List<Inmersion> findByActivoTrueOrderByFechaInmersionAsc();
    
    // Inmersiones próximas desde una fecha
    @Query("SELECT i FROM Inmersion i WHERE i.activo = true AND i.fechaInmersion >= :fechaInicio")
    List<Inmersion> findInmersionesProximas(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    // Inmersiones con plazas disponibles
    @Query("SELECT i FROM Inmersion i WHERE i.activo = true AND i.plazasDisponibles > 0 ORDER BY i.fechaInmersion ASC")
    List<Inmersion> findInmersionesDisponibles();
    
    // Buscar por título o ubicación
    @Query("SELECT i FROM Inmersion i WHERE LOWER(i.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.ubicacion) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Inmersion> buscarPorTituloOUbicacion(@Param("keyword") String keyword);

    // Búsqueda avanzada por texto + filtros opcionales (si el parámetro es null, se ignora)
    @Query("SELECT i FROM Inmersion i WHERE i.activo = true " +
           " AND (:keyword IS NULL OR :keyword = '' " +
           "      OR LOWER(i.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "      OR LOWER(i.ubicacion) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "      OR LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           " AND (:profMin IS NULL OR i.profundidadMaxima >= :profMin) " +
           " AND (:profMax IS NULL OR i.profundidadMaxima <= :profMax) " +
           " AND (:precioMax IS NULL OR i.precio <= :precioMax) " +
           " AND (:nivel IS NULL OR :nivel = '' OR LOWER(i.nivelRequerido) LIKE LOWER(CONCAT('%', :nivel, '%')))" +
           " ORDER BY i.fechaInmersion ASC")
    List<Inmersion> buscarAvanzado(@Param("keyword")   String keyword,
                                   @Param("profMin")   Double profMin,
                                   @Param("profMax")   Double profMax,
                                   @Param("precioMax") Double precioMax,
                                   @Param("nivel")     String nivel);

    @Query(value = "SELECT i.id AS id, " +
                   "  (6371 * ACOS( " +
                   "     COS(RADIANS(:lat)) * COS(RADIANS(i.latitud)) * " +
                   "     COS(RADIANS(i.longitud) - RADIANS(:lon)) + " +
                   "     SIN(RADIANS(:lat)) * SIN(RADIANS(i.latitud)) " +
                   "  )) AS distanciaKm " +
                   "FROM inmersiones i " +
                   "WHERE i.activo = true AND i.latitud IS NOT NULL AND i.longitud IS NOT NULL " +
                   "ORDER BY distanciaKm ASC " +
                   "LIMIT :limite",
           nativeQuery = true)
    List<InmersionConDistancia> findMasCercanas(@Param("lat")    double lat,
                                                @Param("lon")    double lon,
                                                @Param("limite") int    limite);

    /** Proyección de {@link #findMasCercanas(double, double, int)}. */
    interface InmersionConDistancia {
        Long   getId();
        Double getDistanciaKm();
    }
}