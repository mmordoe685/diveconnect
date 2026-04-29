package com.diveconnect.service;

import com.diveconnect.dto.response.SeguimientoEstadoResponse;
import com.diveconnect.entity.EstadoSolicitud;
import com.diveconnect.entity.SolicitudSeguimiento;
import com.diveconnect.entity.TipoNotificacion;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.exception.UnauthorizedException;
import com.diveconnect.repository.SolicitudSeguimientoRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Gestiona seguidores con flujo de solicitud:
 *   - Si el destinatario es EMPRESA → follow directo (perfil público comercial).
 *   - Si el destinatario es USUARIO_COMUN → se crea SolicitudSeguimiento PENDIENTE
 *     + Notificación accionable. El destinatario decide aceptar/rechazar.
 */
@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final UsuarioRepository usuarioRepository;
    private final SolicitudSeguimientoRepository solicitudRepository;
    private final NotificacionService notificacionService;

    // ─── Acciones ──────────────────────────────────────────────────────────────

    /**
     * Solicitar seguir a un usuario. Devuelve el estado resultante.
     * Si el destinatario es empresa → queda SIGUIENDO inmediatamente.
     * Si es usuario común → queda SOLICITADO hasta que el otro acepte.
     */
    @Transactional
    public SeguimientoEstadoResponse solicitar(String usernameSolicitante, Long destinatarioId) {
        Usuario solicitante = cargarPorUsername(usernameSolicitante);
        Usuario destinatario = cargarPorId(destinatarioId);

        if (solicitante.getId().equals(destinatario.getId())) {
            throw new BadRequestException("No puedes seguirte a ti mismo");
        }
        if (usuarioRepository.existsSeguimiento(solicitante.getId(), destinatario.getId())) {
            throw new BadRequestException("Ya sigues a este usuario");
        }

        // Empresas: follow inmediato
        if (destinatario.getTipoUsuario() == TipoUsuario.USUARIO_EMPRESA) {
            usuarioRepository.addSeguidor(solicitante.getId(), destinatario.getId());
            notificacionService.crear(
                    destinatario, solicitante,
                    TipoNotificacion.NUEVO_SEGUIDOR, solicitante.getId(),
                    "@" + solicitante.getUsername() + " ha empezado a seguirte",
                    false);
            return estadoResponse("SIGUIENDO", null);
        }

        // Usuarios comunes: solicitud
        Optional<SolicitudSeguimiento> existente =
                solicitudRepository.findPendienteEntre(solicitante, destinatario);
        if (existente.isPresent()) {
            return estadoResponse("SOLICITADO", existente.get().getId());
        }

        SolicitudSeguimiento s = new SolicitudSeguimiento();
        s.setSolicitante(solicitante);
        s.setDestinatario(destinatario);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        s = solicitudRepository.save(s);

        notificacionService.crear(
                destinatario, solicitante,
                TipoNotificacion.SOLICITUD_SEGUIMIENTO, s.getId(),
                "@" + solicitante.getUsername() + " quiere seguirte",
                true);

        return estadoResponse("SOLICITADO", s.getId());
    }

    @Transactional
    public SeguimientoEstadoResponse aceptar(String usernameDestinatario, Long solicitudId) {
        SolicitudSeguimiento s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarDestinatario(s, usernameDestinatario);
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido resuelta");
        }

        // crear follow (solicitante → destinatario)
        usuarioRepository.addSeguidor(s.getSolicitante().getId(), s.getDestinatario().getId());

        s.setEstado(EstadoSolicitud.ACEPTADA);
        s.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(s);

        // Notificar al solicitante
        notificacionService.crear(
                s.getSolicitante(), s.getDestinatario(),
                TipoNotificacion.SEGUIMIENTO_ACEPTADO, s.getId(),
                "@" + s.getDestinatario().getUsername() + " aceptó tu solicitud de seguimiento",
                false);

        // Marcar la notificación accionable original como resuelta
        marcarNotificacionResuelta(s.getDestinatario(), s.getSolicitante(),
                TipoNotificacion.SOLICITUD_SEGUIMIENTO);

        return estadoResponse("ACEPTADA", s.getId());
    }

    @Transactional
    public SeguimientoEstadoResponse rechazar(String usernameDestinatario, Long solicitudId) {
        SolicitudSeguimiento s = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        verificarDestinatario(s, usernameDestinatario);
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido resuelta");
        }

        s.setEstado(EstadoSolicitud.RECHAZADA);
        s.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(s);

        marcarNotificacionResuelta(s.getDestinatario(), s.getSolicitante(),
                TipoNotificacion.SOLICITUD_SEGUIMIENTO);

        return estadoResponse("RECHAZADA", s.getId());
    }

    @Transactional
    public SeguimientoEstadoResponse dejarDeSeguir(String username, Long seguidoId) {
        Usuario seguidor = cargarPorUsername(username);
        Usuario seguido = cargarPorId(seguidoId);
        if (!usuarioRepository.existsSeguimiento(seguidor.getId(), seguido.getId())) {
            throw new BadRequestException("No sigues a este usuario");
        }
        usuarioRepository.removeSeguidor(seguidor.getId(), seguido.getId());
        return estadoResponse("NO_SIGUE", null);
    }

    /** Estado actual de seguimiento entre usernameActual y destinatarioId. */
    @Transactional(readOnly = true)
    public SeguimientoEstadoResponse estado(String usernameActual, Long destinatarioId) {
        Usuario actual = cargarPorUsername(usernameActual);
        Usuario destino = cargarPorId(destinatarioId);
        if (actual.getId().equals(destino.getId())) {
            return estadoResponse("PROPIO", null);
        }
        if (usuarioRepository.existsSeguimiento(actual.getId(), destino.getId())) {
            return estadoResponse("SIGUIENDO", null);
        }
        Optional<SolicitudSeguimiento> pend = solicitudRepository.findPendienteEntre(actual, destino);
        if (pend.isPresent()) {
            return estadoResponse("SOLICITADO", pend.get().getId());
        }
        return estadoResponse("NO_SIGUE", null);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void marcarNotificacionResuelta(Usuario destinatarioNotif, Usuario emisor,
                                             TipoNotificacion tipo) {
        notificacionService.marcarResueltoPorEmisorYTipo(destinatarioNotif, emisor, tipo);
    }

    private Usuario cargarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private Usuario cargarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
    }

    private void verificarDestinatario(SolicitudSeguimiento s, String username) {
        if (!s.getDestinatario().getUsername().equalsIgnoreCase(username)) {
            throw new UnauthorizedException("Esta solicitud no te pertenece");
        }
    }

    private SeguimientoEstadoResponse estadoResponse(String estado, Long solicitudId) {
        SeguimientoEstadoResponse r = new SeguimientoEstadoResponse();
        r.setEstado(estado);
        r.setSolicitudPendienteId(solicitudId);
        return r;
    }
}
