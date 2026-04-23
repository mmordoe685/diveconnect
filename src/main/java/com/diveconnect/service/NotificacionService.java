package com.diveconnect.service;

import com.diveconnect.dto.response.NotificacionResponse;
import com.diveconnect.entity.Notificacion;
import com.diveconnect.entity.TipoNotificacion;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.exception.UnauthorizedException;
import com.diveconnect.repository.NotificacionRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository      usuarioRepository;

    // ─── API de emisión (llamada desde otros services) ────────────────────────

    @Transactional
    public Notificacion crear(Usuario destinatario, Usuario emisor, TipoNotificacion tipo,
                              Long entidadRelacionadaId, String mensaje, boolean accionable) {
        if (destinatario == null) return null;
        if (emisor != null && emisor.getId().equals(destinatario.getId())) return null; // no auto-notificarse
        Notificacion n = new Notificacion();
        n.setDestinatario(destinatario);
        n.setEmisor(emisor);
        n.setTipo(tipo);
        n.setEntidadRelacionadaId(entidadRelacionadaId);
        n.setMensaje(mensaje);
        n.setAccionable(accionable);
        n.setResuelta(false);
        n.setLeida(false);
        return notificacionRepository.save(n);
    }

    // ─── API REST ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarDeUsuario(String username) {
        Usuario u = cargarUsuario(username);
        return notificacionRepository.findDeUsuario(u).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        Usuario u = cargarUsuario(username);
        return notificacionRepository.countByDestinatarioAndLeidaFalse(u);
    }

    @Transactional
    public void marcarLeida(Long id, String username) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        verificarPropietario(n, username);
        if (!Boolean.TRUE.equals(n.getLeida())) {
            n.setLeida(true);
            notificacionRepository.save(n);
        }
    }

    @Transactional
    public int marcarTodasLeidas(String username) {
        Usuario u = cargarUsuario(username);
        List<Notificacion> pendientes = notificacionRepository.findDeUsuario(u).stream()
                .filter(n -> !Boolean.TRUE.equals(n.getLeida()))
                .collect(Collectors.toList());
        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
        return pendientes.size();
    }

    @Transactional
    public void eliminar(Long id, String username) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        verificarPropietario(n, username);
        notificacionRepository.delete(n);
    }

    /**
     * Marca como RESUELTA + LEIDA toda notificación accionable pendiente del
     * mismo destinatario y emisor y tipo (para ocultar el botón cuando ya se
     * actuó sobre la solicitud).
     */
    @Transactional
    public void marcarResueltoPorEmisorYTipo(Usuario destinatario, Usuario emisor,
                                             TipoNotificacion tipo) {
        List<Notificacion> pendientes =
                notificacionRepository.findNoResueltasPorEmisorYTipo(destinatario, emisor, tipo);
        for (Notificacion n : pendientes) {
            n.setResuelta(true);
            n.setLeida(true);
        }
        notificacionRepository.saveAll(pendientes);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Usuario cargarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private void verificarPropietario(Notificacion n, String username) {
        if (!n.getDestinatario().getUsername().equalsIgnoreCase(username)) {
            throw new UnauthorizedException("Esta notificación no te pertenece");
        }
    }

    public NotificacionResponse toResponse(Notificacion n) {
        NotificacionResponse r = new NotificacionResponse();
        r.setId(n.getId());
        r.setTipo(n.getTipo() != null ? n.getTipo().name() : null);
        r.setMensaje(n.getMensaje());
        r.setAccionable(n.getAccionable());
        r.setResuelta(n.getResuelta());
        r.setLeida(n.getLeida());
        r.setEntidadRelacionadaId(n.getEntidadRelacionadaId());
        r.setFechaCreacion(n.getFechaCreacion());
        Usuario e = n.getEmisor();
        if (e != null) {
            r.setEmisorId(e.getId());
            r.setEmisorUsername(e.getUsername());
            r.setEmisorFotoPerfil(e.getFotoPerfil());
            r.setEmisorNombreEmpresa(e.getNombreEmpresa());
        }
        return r;
    }
}
