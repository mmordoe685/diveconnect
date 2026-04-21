package com.diveconnect.repository;

import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.PuntoMapa;
import com.diveconnect.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuntoMapaRepository extends JpaRepository<PuntoMapa, Long> {

    List<PuntoMapa> findByActivoTrueOrderByFechaCreacionDesc();

    List<PuntoMapa> findByAutorOrderByFechaCreacionDesc(Usuario autor);

    List<PuntoMapa> findByInmersion(Inmersion inmersion);
}
