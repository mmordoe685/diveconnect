package com.diveconnect.repository;

import com.diveconnect.entity.Historia;
import com.diveconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoriaRepository extends JpaRepository<Historia, Long> {

    @Query("SELECT h FROM Historia h WHERE h.expiraEn > :ahora ORDER BY h.fechaPublicacion DESC")
    List<Historia> findActivas(@Param("ahora") LocalDateTime ahora);

    @Query("SELECT h FROM Historia h WHERE h.usuario = :usuario AND h.expiraEn > :ahora ORDER BY h.fechaPublicacion DESC")
    List<Historia> findActivasDeUsuario(@Param("usuario") Usuario usuario, @Param("ahora") LocalDateTime ahora);

    void deleteByExpiraEnBefore(LocalDateTime fecha);
}
