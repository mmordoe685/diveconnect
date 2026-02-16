package com.diveconnect.service;

import com.diveconnect.dto.request.CentroBuceoRequest;
import com.diveconnect.dto.response.CentroBuceoResponse;
import com.diveconnect.entity.CentroBuceo;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.CentroBuceoRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CentroBuceoService {

    private final CentroBuceoRepository centroBuceoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CentroBuceoResponse crearCentroBuceo(Long usuarioId, CentroBuceoRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar que el usuario no tenga ya un centro
        if (centroBuceoRepository.findByUsuario(usuario).isPresent()) {
            throw new BadRequestException("Este usuario ya tiene un centro de buceo registrado");
        }

        CentroBuceo centro = new CentroBuceo();
        centro.setNombre(request.getNombre());
        centro.setDescripcion(request.getDescripcion());
        centro.setDireccion(request.getDireccion());
        centro.setCiudad(request.getCiudad());
        centro.setPais(request.getPais());
        centro.setTelefono(request.getTelefono());
        centro.setEmail(request.getEmail());
        centro.setSitioWeb(request.getSitioWeb());
        centro.setCertificaciones(request.getCertificaciones());
        centro.setLatitud(request.getLatitud());
        centro.setLongitud(request.getLongitud());
        centro.setImagenUrl(request.getImagenUrl());
        centro.setUsuario(usuario);

        CentroBuceo savedCentro = centroBuceoRepository.save(centro);
        return convertirAResponse(savedCentro);
    }

    @Transactional(readOnly = true)
    public CentroBuceoResponse obtenerCentro(Long id) {
        CentroBuceo centro = centroBuceoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de buceo no encontrado"));
        return convertirAResponse(centro);
    }

    @Transactional(readOnly = true)
    public CentroBuceoResponse obtenerCentroDeUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        CentroBuceo centro = centroBuceoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ResourceNotFoundException("Este usuario no tiene un centro de buceo"));
        
        return convertirAResponse(centro);
    }

    @Transactional(readOnly = true)
    public List<CentroBuceoResponse> obtenerTodos() {
        return centroBuceoRepository.findByActivoTrue().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CentroBuceoResponse> buscarCentros(String keyword) {
        return centroBuceoRepository.buscarPorNombreOCiudad(keyword).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CentroBuceoResponse actualizarCentro(Long id, CentroBuceoRequest request) {
        CentroBuceo centro = centroBuceoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de buceo no encontrado"));

        centro.setNombre(request.getNombre());
        centro.setDescripcion(request.getDescripcion());
        centro.setDireccion(request.getDireccion());
        centro.setCiudad(request.getCiudad());
        centro.setPais(request.getPais());
        centro.setTelefono(request.getTelefono());
        centro.setEmail(request.getEmail());
        centro.setSitioWeb(request.getSitioWeb());
        centro.setCertificaciones(request.getCertificaciones());
        centro.setLatitud(request.getLatitud());
        centro.setLongitud(request.getLongitud());
        centro.setImagenUrl(request.getImagenUrl());

        CentroBuceo updatedCentro = centroBuceoRepository.save(centro);
        return convertirAResponse(updatedCentro);
    }

    private CentroBuceoResponse convertirAResponse(CentroBuceo centro) {
        CentroBuceoResponse response = new CentroBuceoResponse();
        response.setId(centro.getId());
        response.setNombre(centro.getNombre());
        response.setDescripcion(centro.getDescripcion());
        response.setDireccion(centro.getDireccion());
        response.setCiudad(centro.getCiudad());
        response.setPais(centro.getPais());
        response.setTelefono(centro.getTelefono());
        response.setEmail(centro.getEmail());
        response.setSitioWeb(centro.getSitioWeb());
        response.setCertificaciones(centro.getCertificaciones());
        response.setLatitud(centro.getLatitud());
        response.setLongitud(centro.getLongitud());
        response.setImagenUrl(centro.getImagenUrl());
        response.setValoracionPromedio(centro.getValoracionPromedio());
        response.setActivo(centro.getActivo());
        response.setFechaRegistro(centro.getFechaRegistro());
        
        if (centro.getUsuario() != null) {
            response.setUsuarioId(centro.getUsuario().getId());
            response.setUsuarioUsername(centro.getUsuario().getUsername());
        }
        
        return response;
    }
}