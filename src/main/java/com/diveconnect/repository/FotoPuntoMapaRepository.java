package com.diveconnect.repository;

import com.diveconnect.entity.FotoPuntoMapa;
import com.diveconnect.entity.PuntoMapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoPuntoMapaRepository extends JpaRepository<FotoPuntoMapa, Long> {
    List<FotoPuntoMapa> findByPuntoMapaOrderByFechaSubidaDesc(PuntoMapa puntoMapa);
}
