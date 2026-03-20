package com.diveconnect.service;

import com.diveconnect.dto.request.InmersionRequest;
import com.diveconnect.dto.response.InmersionResponse;
import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.CentroBuceoRepository;
import com.diveconnect.repository.InmersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InmersionService {

    private final InmersionRepository inmersionRepository;
    private final CentroBuceoRepository centroBuceoRepository;

    @Transactional
    public InmersionResponse crearInmersion(Long centroBuceoId, InmersionRequest request) {
        CentroBuceo centro = centroBuceoRepository.findById(centroBuceoId)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de buceo no encontrado"));

        Inmersion inmersion = new Inmersion();
        inmersion.setTitulo(request.getTitulo());
        inmersion.setDescripcion(request.getDescripcion());
        inmersion.setFechaInmersion(request.getFechaInmersion());
        inmersion.setDuracion(request.getDuracion());
        inmersion.setProfundidadMaxima(request.getProfundidadMaxima());
        inmersion.setNivelRequerido(request.getNivelRequerido());
        inmersion.setPrecio(request.getPrecio());
        inmersion.setPlazasTotales(request.getPlazasTotales());
        inmersion.setPlazasDisponibles(request.getPlazasTotales());
        inmersion.setUbicacion(request.getUbicacion());
        inmersion.setLatitud(request.getLatitud());
        inmersion.setLongitud(request.getLongitud());
        inmersion.setEquipoIncluido(request.getEquipoIncluido());
        inmersion.setImagenUrl(request.getImagenUrl());
        inmersion.setCentroBuceo(centro);

        return convertirAResponse(inmersionRepository.save(inmersion));
    }

    @Transactional(readOnly = true)
    public InmersionResponse obtenerInmersion(Long id) {
        Inmersion inmersion = inmersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));
        return convertirAResponse(inmersion);
    }

    @Transactional(readOnly = true)
    public List<InmersionResponse> obtenerInmersionesDisponibles() {
        return inmersionRepository.findInmersionesDisponibles().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InmersionResponse> obtenerTodasLasInmersiones() {
        return inmersionRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InmersionResponse> obtenerInmersionesProximas() {
        return inmersionRepository.findInmersionesProximas(LocalDateTime.now()).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InmersionResponse> obtenerInmersionesPorCentro(Long centroBuceoId) {
        CentroBuceo centro = centroBuceoRepository.findById(centroBuceoId)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de buceo no encontrado"));

        return inmersionRepository.findByCentroBuceoAndActivaTrue(centro).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InmersionResponse actualizarInmersion(Long id, InmersionRequest request) {
        Inmersion inmersion = inmersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));

        inmersion.setTitulo(request.getTitulo());
        inmersion.setDescripcion(request.getDescripcion());
        inmersion.setFechaInmersion(request.getFechaInmersion());
        inmersion.setDuracion(request.getDuracion());
        inmersion.setProfundidadMaxima(request.getProfundidadMaxima());
        inmersion.setNivelRequerido(request.getNivelRequerido());
        inmersion.setPrecio(request.getPrecio());
        inmersion.setPlazasTotales(request.getPlazasTotales());
        inmersion.setUbicacion(request.getUbicacion());
        inmersion.setLatitud(request.getLatitud());
        inmersion.setLongitud(request.getLongitud());
        inmersion.setEquipoIncluido(request.getEquipoIncluido());
        inmersion.setImagenUrl(request.getImagenUrl());

        return convertirAResponse(inmersionRepository.save(inmersion));
    }

    @Transactional
    public void cancelarInmersion(Long id) {
        Inmersion inmersion = inmersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));
        inmersion.setActivo(false);
        inmersionRepository.save(inmersion);
    }

    @Transactional
    public void eliminarInmersion(Long id, Long usuarioId) {
        Inmersion inmersion = inmersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inmersión no encontrada"));
        inmersion.setActivo(false);
        inmersionRepository.save(inmersion);
    }

    private InmersionResponse convertirAResponse(Inmersion inmersion) {
        InmersionResponse response = new InmersionResponse();
        response.setId(inmersion.getId());
        response.setTitulo(inmersion.getTitulo());
        response.setDescripcion(inmersion.getDescripcion());
        response.setFechaInmersion(inmersion.getFechaInmersion());
        response.setDuracion(inmersion.getDuracion());
        response.setProfundidadMaxima(inmersion.getProfundidadMaxima());
        response.setNivelRequerido(inmersion.getNivelRequerido());
        response.setPrecio(inmersion.getPrecio());
        response.setPlazasDisponibles(inmersion.getPlazasDisponibles());
        response.setPlazasTotales(inmersion.getPlazasTotales());
        response.setUbicacion(inmersion.getUbicacion());
        response.setLatitud(inmersion.getLatitud());
        response.setLongitud(inmersion.getLongitud());
        response.setEquipoIncluido(inmersion.getEquipoIncluido());
        response.setImagenUrl(inmersion.getImagenUrl());
        response.setActiva(inmersion.getActivo()); // 🔥 importante
        response.setFechaCreacion(inmersion.getFechaCreacion());

        if (inmersion.getCentroBuceo() != null) {
            response.setCentroBuceoId(inmersion.getCentroBuceo().getId());
            response.setCentroBuceoNombre(inmersion.getCentroBuceo().getNombre());
            response.setCentroBuceoCiudad(inmersion.getCentroBuceo().getCiudad());
        }

        return response;
    }
}