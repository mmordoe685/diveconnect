package com.diveconnect.service;

import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Convierte {@link Usuario} a {@link UsuarioResponse}. Extraído para que
 * Search, Seguimiento y otros servicios reutilicen la misma forma de
 * respuesta sin duplicar lógica.
 */
@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final PublicacionRepository publicacionRepository;

    public UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setBiografia(u.getBiografia());
        r.setFotoPerfil(u.getFotoPerfil());
        r.setNivelCertificacion(u.getNivelCertificacion());
        r.setNumeroInmersiones(u.getNumeroInmersiones());
        r.setTipoUsuario(u.getTipoUsuario());
        r.setActivo(u.getActivo());
        r.setFechaRegistro(u.getFechaRegistro());

        r.setNombreEmpresa(u.getNombreEmpresa());
        r.setDescripcionEmpresa(u.getDescripcionEmpresa());
        r.setDireccion(u.getDireccion());
        r.setTelefono(u.getTelefono());
        r.setSitioWeb(u.getSitioWeb());

        r.setNumeroSeguidores(u.getSeguidores() != null ? u.getSeguidores().size() : 0);
        r.setNumeroSiguiendo(u.getSiguiendo() != null ? u.getSiguiendo().size() : 0);
        r.setNumeroPublicaciones(publicacionRepository.countByUsuario(u).intValue());
        return r;
    }
}
