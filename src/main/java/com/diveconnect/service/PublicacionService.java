package com.diveconnect.service;

import com.diveconnect.dto.request.PublicacionRequest;
import com.diveconnect.dto.response.PublicacionResponse;
import com.diveconnect.entity.Publicacion;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.exception.UnauthorizedException;
import com.diveconnect.repository.PublicacionRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PublicacionResponse crearPublicacion(Long usuarioId, PublicacionRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Publicacion publicacion = new Publicacion();
        publicacion.setContenido(request.getContenido());
        publicacion.setImagenUrl(request.getImagenUrl());
        publicacion.setVideoUrl(request.getVideoUrl());
        publicacion.setLugarInmersion(request.getLugarInmersion());
        publicacion.setProfundidadMaxima(request.getProfundidadMaxima());
        publicacion.setTemperaturaAgua(request.getTemperaturaAgua());
        publicacion.setVisibilidad(request.getVisibilidad());
        publicacion.setEspeciesVistas(request.getEspeciesVistas());
        publicacion.setUsuario(usuario);

        Publicacion savedPublicacion = publicacionRepository.save(publicacion);
        return convertirAResponse(savedPublicacion, usuarioId);
    }

    @Transactional(readOnly = true)
    public PublicacionResponse obtenerPublicacion(Long id, Long usuarioActualId) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        return convertirAResponse(publicacion, usuarioActualId);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerTodasLasPublicaciones(Long usuarioActualId, int pagina, int tamaño) {
        Pageable pageable = PageRequest.of(pagina, tamaño);
        Page<Publicacion> publicaciones = publicacionRepository.findAllByOrderByFechaPublicacionDesc(pageable);
        return publicaciones.stream()
                .map(p -> convertirAResponse(p, usuarioActualId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerPublicacionesDeUsuario(Long usuarioId, Long usuarioActualId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        return publicacionRepository.findByUsuarioOrderByFechaPublicacionDesc(usuario).stream()
                .map(p -> convertirAResponse(p, usuarioActualId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerFeedPersonalizado(Long usuarioId, int pagina, int tamaño) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Usuario> usuariosSeguidos = new ArrayList<>(usuario.getSiguiendo());
        usuariosSeguidos.add(usuario); // Incluir publicaciones propias

        Pageable pageable = PageRequest.of(pagina, tamaño);
        Page<Publicacion> publicaciones = publicacionRepository.findPublicacionesDeSeguidos(usuariosSeguidos, pageable);
        
        return publicaciones.stream()
                .map(p -> convertirAResponse(p, usuarioId))
                .collect(Collectors.toList());
    }

    @Transactional
    public PublicacionResponse actualizarPublicacion(Long id, Long usuarioId, PublicacionRequest request) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (!publicacion.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tienes permiso para editar esta publicación");
        }

        publicacion.setContenido(request.getContenido());
        publicacion.setImagenUrl(request.getImagenUrl());
        publicacion.setVideoUrl(request.getVideoUrl());
        publicacion.setLugarInmersion(request.getLugarInmersion());
        publicacion.setProfundidadMaxima(request.getProfundidadMaxima());
        publicacion.setTemperaturaAgua(request.getTemperaturaAgua());
        publicacion.setVisibilidad(request.getVisibilidad());
        publicacion.setEspeciesVistas(request.getEspeciesVistas());

        Publicacion updatedPublicacion = publicacionRepository.save(publicacion);
        return convertirAResponse(updatedPublicacion, usuarioId);
    }

    @Transactional
    public void eliminarPublicacion(Long id, Long usuarioId) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        if (!publicacion.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta publicación");
        }

        publicacionRepository.delete(publicacion);
    }

    @Transactional
    public void darLike(Long publicacionId, Long usuarioId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (publicacion.getLikes().contains(usuario)) {
            throw new BadRequestException("Ya has dado like a esta publicación");
        }

        publicacion.getLikes().add(usuario);
        publicacion.setNumeroLikes(publicacion.getLikes().size());
        publicacionRepository.save(publicacion);
    }

    @Transactional
    public void quitarLike(Long publicacionId, Long usuarioId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!publicacion.getLikes().contains(usuario)) {
            throw new BadRequestException("No has dado like a esta publicación");
        }

        publicacion.getLikes().remove(usuario);
        publicacion.setNumeroLikes(publicacion.getLikes().size());
        publicacionRepository.save(publicacion);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> buscarPublicaciones(String keyword, Long usuarioActualId) {
        return publicacionRepository.buscarPorContenido(keyword).stream()
                .map(p -> convertirAResponse(p, usuarioActualId))
                .collect(Collectors.toList());
    }

    private PublicacionResponse convertirAResponse(Publicacion publicacion, Long usuarioActualId) {
        PublicacionResponse response = new PublicacionResponse();
        response.setId(publicacion.getId());
        response.setContenido(publicacion.getContenido());
        response.setImagenUrl(publicacion.getImagenUrl());
        response.setVideoUrl(publicacion.getVideoUrl());
        response.setLugarInmersion(publicacion.getLugarInmersion());
        response.setProfundidadMaxima(publicacion.getProfundidadMaxima());
        response.setTemperaturaAgua(publicacion.getTemperaturaAgua());
        response.setVisibilidad(publicacion.getVisibilidad());
        response.setEspeciesVistas(publicacion.getEspeciesVistas());
        response.setFechaPublicacion(publicacion.getFechaPublicacion());
        response.setNumeroLikes(publicacion.getNumeroLikes());
        response.setNumeroComentarios(publicacion.getNumeroComentarios());
        
        // Datos del usuario
        response.setUsuarioId(publicacion.getUsuario().getId());
        response.setUsuarioUsername(publicacion.getUsuario().getUsername());
        response.setUsuarioFotoPerfil(publicacion.getUsuario().getFotoPerfil());
        
        // Verificar si el usuario actual ha dado like
        if (usuarioActualId != null) {
            boolean liked = publicacion.getLikes().stream()
                    .anyMatch(u -> u.getId().equals(usuarioActualId));
            response.setLikedByCurrentUser(liked);
        } else {
            response.setLikedByCurrentUser(false);
        }
        
        return response;
    }
}