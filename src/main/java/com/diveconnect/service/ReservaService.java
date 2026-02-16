package com.diveconnect.service;

import com.diveconnect.dto.request.ReservaRequest;
import com.diveconnect.dto.response.ReservaResponse;
import com.diveconnect.entity.EstadoReserva;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.Reserva;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.InmersionRepository;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final InmersionRepository inmersionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ReservaResponse crearReserva(Long usuarioId, ReservaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        Inmersion inmersion = inmersionRepository.findById(request.getInmersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));

        // Verificar que hay plazas disponibles
        if (inmersion.getPlazasDisponibles() < request.getNumeroPersonas()) {
            throw new BadRequestException("No hay suficientes plazas disponibles");
        }

        // Calcular precio total
        Double precioTotal = inmersion.getPrecio() * request.getNumeroPersonas();

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setInmersion(inmersion);
        reserva.setCentroBuceo(inmersion.getCentroBuceo());
        reserva.setNumeroPersonas(request.getNumeroPersonas());
        reserva.setPrecioTotal(precioTotal);
        reserva.setObservaciones(request.getObservaciones());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        Reserva savedReserva = reservaRepository.save(reserva);

        // Actualizar plazas disponibles
        inmersion.setPlazasDisponibles(inmersion.getPlazasDisponibles() - request.getNumeroPersonas());
        inmersionRepository.save(inmersion);

        return convertirAResponse(savedReserva);
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        return convertirAResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> obtenerReservasDeUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        return reservaRepository.findByUsuarioOrderByFechaReservaDesc(usuario).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservaResponse confirmarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        Reserva updatedReserva = reservaRepository.save(reserva);
        return convertirAResponse(updatedReserva);
    }

    @Transactional
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        
        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new BadRequestException("No se puede cancelar una reserva ya completada");
        }

        // Liberar plazas
        Inmersion inmersion = reserva.getInmersion();
        inmersion.setPlazasDisponibles(inmersion.getPlazasDisponibles() + reserva.getNumeroPersonas());
        inmersionRepository.save(inmersion);

        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);
    }

    private ReservaResponse convertirAResponse(Reserva reserva) {
        ReservaResponse response = new ReservaResponse();
        response.setId(reserva.getId());
        response.setNumeroPersonas(reserva.getNumeroPersonas());
        response.setEstado(reserva.getEstado());
        response.setPrecioTotal(reserva.getPrecioTotal());
        response.setObservaciones(reserva.getObservaciones());
        response.setFechaReserva(reserva.getFechaReserva());
        response.setUltimaModificacion(reserva.getUltimaModificacion());
        
        if (reserva.getUsuario() != null) {
            response.setUsuarioId(reserva.getUsuario().getId());
            response.setUsuarioUsername(reserva.getUsuario().getUsername());
        }
        
        if (reserva.getInmersion() != null) {
            response.setInmersionId(reserva.getInmersion().getId());
            response.setInmersionTitulo(reserva.getInmersion().getTitulo());
            response.setInmersionFecha(reserva.getInmersion().getFechaInmersion());
        }
        
        if (reserva.getCentroBuceo() != null) {
            response.setCentroBuceoId(reserva.getCentroBuceo().getId());
            response.setCentroBuceoNombre(reserva.getCentroBuceo().getNombre());
        }
        
        return response;
    }
}