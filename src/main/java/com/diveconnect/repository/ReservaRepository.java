package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.EstadoReserva;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.Reserva;
import com.diveconnect.entity.Usuario;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Reservas de un usuario
    List<Reserva> findByUsuarioOrderByFechaReservaDesc(Usuario usuario);
    
    // Reservas de un centro
    List<Reserva> findByCentroBuceoOrderByFechaReservaDesc(CentroBuceo centroBuceo);
    
    // Reservas de una inmersión específica
    List<Reserva> findByInmersion(Inmersion inmersion);
    
    // Buscar por estado
    List<Reserva> findByEstado(EstadoReserva estado);
    
    // Reservas de un usuario con estado específico
    List<Reserva> findByUsuarioAndEstado(Usuario usuario, EstadoReserva estado);
    
    // Contar reservas confirmadas de una inmersión
    Long countByInmersionAndEstado(Inmersion inmersion, EstadoReserva estado);
}