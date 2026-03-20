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

    @Transactional(readOnly = true)
    public List<ComentarioResponse> obtenerComentariosPorPublicacion(Long publicacionId) {
        return comentarioRepository
            .findByPublicacionIdOrderByFechaComentarioAsc(publicacionId)
            .stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponse crearComentario(
            Long usuarioId, Long publicacionId, ComentarioRequest request) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Publicacion publicacion = publicacionRepository.findById(publicacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        Comentario comentario = new Comentario();
        comentario.setContenido(request.getContenido());
        comentario.setUsuario(usuario);
        comentario.setPublicacion(publicacion);

        // Incrementar contador de comentarios en la publicación
        publicacion.setNumeroComentarios(publicacion.getNumeroComentarios() + 1);
        publicacionRepository.save(publicacion);

        return convertirAResponse(comentarioRepository.save(comentario));
    }

    @Transactional
    public void eliminarComentario(Long comentarioId, Long usuarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));

        if (!comentario.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException(
                "No tienes permiso para eliminar este comentario");
        }

        Publicacion pub = comentario.getPublicacion();
        if (pub.getNumeroComentarios() > 0) {
            pub.setNumeroComentarios(pub.getNumeroComentarios() - 1);
            publicacionRepository.save(pub);
        }
        comentarioRepository.delete(comentario);
    }

    private ComentarioResponse convertirAResponse(Comentario c) {
        ComentarioResponse r = new ComentarioResponse();
        r.setId(c.getId());
        r.setContenido(c.getContenido());
        r.setFechaComentario(c.getFechaComentario());
        r.setUsuarioId(c.getUsuario().getId());
        r.setUsuarioUsername(c.getUsuario().getUsername());
        r.setUsuarioFotoPerfil(c.getUsuario().getFotoPerfil());
        r.setPublicacionId(c.getPublicacion().getId());
        return r;
    }
}