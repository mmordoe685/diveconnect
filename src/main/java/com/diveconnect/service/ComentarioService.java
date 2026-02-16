package com.diveconnect.service;

import com.diveconnect.dto.request.ComentarioRequest;
import com.diveconnect.dto.response.ComentarioResponse;
import com.diveconnect.entity.Comentario;
import com.diveconnect.entity.Publicacion;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.exception.UnauthorizedException;
import com.diveconnect.repository.ComentarioRepository;
import com.diveconnect.repository.PublicacionRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ComentarioResponse crearComentario(Long publicacionId, Long usuarioId, ComentarioRequest request) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Comentario comentario = new Comentario();
        comentario.setContenido(request.getContenido());
        comentario.setPublicacion(publicacion);
        comentario.setUsuario(usuario);

        Comentario savedComentario = comentarioRepository.save(comentario);
        
        // Actualizar contador de comentarios en la publicación
        publicacion.setNumeroComentarios(publicacion.getNumeroComentarios() + 1);
        publicacionRepository.save(publicacion);

        return convertirAResponse(savedComentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> obtenerComentariosDePublicacion(Long publicacionId) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));
        
        return comentarioRepository.findByPublicacionOrderByFechaComentarioDesc(publicacion).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarComentario(Long id, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        if (!comentario.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este comentario");
        }

        Publicacion publicacion = comentario.getPublicacion();
        publicacion.setNumeroComentarios(Math.max(0, publicacion.getNumeroComentarios() - 1));
        publicacionRepository.save(publicacion);

        comentarioRepository.delete(comentario);
    }

    private ComentarioResponse convertirAResponse(Comentario comentario) {
        ComentarioResponse response = new ComentarioResponse();
        response.setId(comentario.getId());
        response.setContenido(comentario.getContenido());
        response.setFechaComentario(comentario.getFechaComentario());
        response.setUsuarioId(comentario.getUsuario().getId());
        response.setUsuarioUsername(comentario.getUsuario().getUsername());
        response.setUsuarioFotoPerfil(comentario.getUsuario().getFotoPerfil());
        response.setPublicacionId(comentario.getPublicacion().getId());
        return response;
    }
}