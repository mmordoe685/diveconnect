package com.diveconnect.repository;

import com.diveconnect.entity.Notificacion;
import com.diveconnect.entity.TipoNotificacion;
import com.diveconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Query("SELECT n FROM Notificacion n WHERE n.destinatario = :usuario ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findDeUsuario(@Param("usuario") Usuario usuario);

    long countByDestinatarioAndLeidaFalse(Usuario destinatario);

    /** Para evitar duplicar solicitudes repetidas de seguimiento. */
    @Query("SELECT n FROM Notificacion n WHERE n.destinatario = :destinatario " +
           "AND n.emisor = :emisor AND n.tipo = :tipo AND n.resuelta = false")
    List<Notificacion> findNoResueltasPorEmisorYTipo(@Param("destinatario") Usuario destinatario,
                                                     @Param("emisor") Usuario emisor,
                                                     @Param("tipo") TipoNotificacion tipo);
}
