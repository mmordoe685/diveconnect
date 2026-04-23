package com.diveconnect.repository;

import com.diveconnect.entity.EstadoSolicitud;
import com.diveconnect.entity.SolicitudSeguimiento;
import com.diveconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudSeguimientoRepository extends JpaRepository<SolicitudSeguimiento, Long> {

    @Query("SELECT s FROM SolicitudSeguimiento s " +
           "WHERE s.solicitante = :solicitante AND s.destinatario = :destinatario " +
           "AND s.estado = com.diveconnect.entity.EstadoSolicitud.PENDIENTE")
    Optional<SolicitudSeguimiento> findPendienteEntre(@Param("solicitante") Usuario solicitante,
                                                      @Param("destinatario") Usuario destinatario);

    @Query("SELECT s FROM SolicitudSeguimiento s WHERE s.destinatario = :usuario AND s.estado = :estado " +
           "ORDER BY s.fechaCreacion DESC")
    List<SolicitudSeguimiento> findPorDestinatarioYEstado(@Param("usuario") Usuario usuario,
                                                          @Param("estado") EstadoSolicitud estado);

    @Query("SELECT s FROM SolicitudSeguimiento s WHERE s.solicitante = :usuario AND s.estado = :estado " +
           "ORDER BY s.fechaCreacion DESC")
    List<SolicitudSeguimiento> findPorSolicitanteYEstado(@Param("usuario") Usuario usuario,
                                                         @Param("estado") EstadoSolicitud estado);
}
