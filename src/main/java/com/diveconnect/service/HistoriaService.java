package com.diveconnect.service;

import com.diveconnect.dto.request.HistoriaRequest;
import com.diveconnect.dto.response.HistoriaResponse;
import com.diveconnect.entity.Historia;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.exception.UnauthorizedException;
import com.diveconnect.repository.HistoriaRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriaService {

    private final HistoriaRepository historiaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public HistoriaResponse crear(String username, HistoriaRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Historia historia = new Historia();
        historia.setMediaUrl(request.getMediaUrl());
        historia.setMediaType(parseMediaType(request.getMediaType()));
        historia.setTexto(request.getTexto());
        historia.setUsuario(usuario);
        historia.setExpiraEn(LocalDateTime.now().plusHours(24));

        return toResponse(historiaRepository.save(historia));
    }

    @Transactional(readOnly = true)
    public List<HistoriaResponse.GrupoUsuario> listarAgrupadas() {
        List<Historia> activas = historiaRepository.findActivas(LocalDateTime.now());
        // LinkedHashMap conserva orden de inserción → usuario con historia más reciente aparece primero
        Map<Long, HistoriaResponse.GrupoUsuario> grupos = new LinkedHashMap<>();

        for (Historia h : activas) {
            Usuario u = h.getUsuario();
            HistoriaResponse.GrupoUsuario grupo = grupos.computeIfAbsent(u.getId(), k -> {
                HistoriaResponse.GrupoUsuario g = new HistoriaResponse.GrupoUsuario();
                g.setUsuarioId(u.getId());
                g.setUsuarioUsername(u.getUsername());
                g.setUsuarioFotoPerfil(u.getFotoPerfil());
                g.setUsuarioEmpresa(u.getNombreEmpresa());
                g.setHistorias(new ArrayList<>());
                return g;
            });
            grupo.getHistorias().add(toResponse(h));
        }
        return new ArrayList<>(grupos.values());
    }

    @Transactional(readOnly = true)
    public List<HistoriaResponse> listarDeUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return historiaRepository.findActivasDeUsuario(usuario, LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id, String username) {
        Historia historia = historiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia no encontrada"));
        if (!historia.getUsuario().getUsername().equalsIgnoreCase(username)) {
            throw new UnauthorizedException("No puedes borrar historias de otros usuarios");
        }
        historiaRepository.delete(historia);
    }

    /** Barrido cada hora: elimina historias cuya expiración ya ha pasado. */
    @Scheduled(fixedRate = 3_600_000L)
    @Transactional
    public void limpiarExpiradas() {
        historiaRepository.deleteByExpiraEnBefore(LocalDateTime.now());
    }

    private Historia.MediaType parseMediaType(String raw) {
        if (raw == null) return Historia.MediaType.FOTO;
        try { return Historia.MediaType.valueOf(raw.toUpperCase()); }
        catch (IllegalArgumentException ex) { return Historia.MediaType.FOTO; }
    }

    private HistoriaResponse toResponse(Historia h) {
        HistoriaResponse r = new HistoriaResponse();
        r.setId(h.getId());
        r.setMediaUrl(h.getMediaUrl());
        r.setMediaType(h.getMediaType().name());
        r.setTexto(h.getTexto());
        r.setFechaPublicacion(h.getFechaPublicacion());
        r.setExpiraEn(h.getExpiraEn());
        Usuario u = h.getUsuario();
        r.setUsuarioId(u.getId());
        r.setUsuarioUsername(u.getUsername());
        r.setUsuarioFotoPerfil(u.getFotoPerfil());
        r.setUsuarioEmpresa(u.getNombreEmpresa());
        return r;
    }
}
