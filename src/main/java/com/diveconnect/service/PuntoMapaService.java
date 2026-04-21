package com.diveconnect.service;

import com.diveconnect.dto.request.PuntoMapaRequest;
import com.diveconnect.dto.response.PuntoMapaResponse;
import com.diveconnect.entity.FotoPuntoMapa;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.PuntoMapa;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.InmersionRepository;
import com.diveconnect.repository.PuntoMapaRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PuntoMapaService {

    private final PuntoMapaRepository puntoMapaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InmersionRepository inmersionRepository;

    @Transactional
    public PuntoMapaResponse crear(Long usuarioId, PuntoMapaRequest req) {
        Usuario autor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (autor.getTipoUsuario() != TipoUsuario.USUARIO_EMPRESA
                && autor.getTipoUsuario() != TipoUsuario.ADMINISTRADOR) {
            throw new BadRequestException("Solo usuarios empresa pueden crear puntos en el mapa");
        }

        PuntoMapa punto = new PuntoMapa();
        punto.setTitulo(req.getTitulo());
        punto.setDescripcion(req.getDescripcion());
        punto.setLatitud(req.getLatitud());
        punto.setLongitud(req.getLongitud());
        punto.setProfundidadMetros(req.getProfundidadMetros());
        punto.setTemperaturaAgua(req.getTemperaturaAgua());
        punto.setPresionBar(req.getPresionBar());
        punto.setCorriente(req.getCorriente());
        punto.setVisibilidadMetros(req.getVisibilidadMetros());
        punto.setEspeciesVistas(req.getEspeciesVistas());
        punto.setFechaObservacion(req.getFechaObservacion());
        punto.setAutor(autor);

        if (req.getInmersionId() != null) {
            Inmersion inm = inmersionRepository.findById(req.getInmersionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));
            punto.setInmersion(inm);
        }

        if (req.getFotos() != null) {
            for (PuntoMapaRequest.FotoInput f : req.getFotos()) {
                if (f.getUrl() == null || f.getUrl().isBlank()) continue;
                FotoPuntoMapa foto = new FotoPuntoMapa();
                foto.setUrl(f.getUrl());
                foto.setEspecieAvistada(f.getEspecieAvistada());
                foto.setFechaHora(f.getFechaHora());
                foto.setDescripcion(f.getDescripcion());
                foto.setPuntoMapa(punto);
                punto.getFotos().add(foto);
            }
        }

        PuntoMapa saved = puntoMapaRepository.save(punto);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PuntoMapaResponse> listarTodos() {
        return puntoMapaRepository.findByActivoTrueOrderByFechaCreacionDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PuntoMapaResponse obtener(Long id) {
        PuntoMapa p = puntoMapaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado"));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PuntoMapaResponse> listarPorAutor(Long autorId) {
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return puntoMapaRepository.findByAutorOrderByFechaCreacionDesc(autor).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        PuntoMapa p = puntoMapaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Punto no encontrado"));
        Usuario user = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean esAdmin = user.getTipoUsuario() == TipoUsuario.ADMINISTRADOR;
        boolean esAutor = p.getAutor() != null && p.getAutor().getId().equals(usuarioId);
        if (!esAdmin && !esAutor) {
            throw new BadRequestException("No tienes permiso para eliminar este punto");
        }
        puntoMapaRepository.delete(p);
    }

    private PuntoMapaResponse toResponse(PuntoMapa p) {
        PuntoMapaResponse r = new PuntoMapaResponse();
        r.setId(p.getId());
        r.setTitulo(p.getTitulo());
        r.setDescripcion(p.getDescripcion());
        r.setLatitud(p.getLatitud());
        r.setLongitud(p.getLongitud());
        r.setProfundidadMetros(p.getProfundidadMetros());
        r.setTemperaturaAgua(p.getTemperaturaAgua());
        r.setPresionBar(p.getPresionBar());
        r.setCorriente(p.getCorriente());
        r.setVisibilidadMetros(p.getVisibilidadMetros());
        r.setEspeciesVistas(p.getEspeciesVistas());
        r.setFechaObservacion(p.getFechaObservacion());
        r.setFechaCreacion(p.getFechaCreacion());

        if (p.getAutor() != null) {
            r.setAutorId(p.getAutor().getId());
            r.setAutorUsername(p.getAutor().getUsername());
            r.setAutorEmpresa(p.getAutor().getNombreEmpresa());
        }
        if (p.getInmersion() != null) {
            r.setInmersionId(p.getInmersion().getId());
            r.setInmersionTitulo(p.getInmersion().getTitulo());
        }

        List<PuntoMapaResponse.FotoMapaDto> fotos = new ArrayList<>();
        if (p.getFotos() != null) {
            for (FotoPuntoMapa f : p.getFotos()) {
                PuntoMapaResponse.FotoMapaDto d = new PuntoMapaResponse.FotoMapaDto();
                d.setId(f.getId());
                d.setUrl(f.getUrl());
                d.setEspecieAvistada(f.getEspecieAvistada());
                d.setFechaHora(f.getFechaHora());
                d.setDescripcion(f.getDescripcion());
                fotos.add(d);
            }
        }
        r.setFotos(fotos);
        return r;
    }
}
