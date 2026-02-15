package com.diveconnect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.diveconnect.dto.request.RegistroRequest;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.PublicacionRepository;
import com.diveconnect.repository.UsuarioRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse registrarUsuario(RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya existe");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTipoUsuario(request.getTipoUsuario() != null ? request.getTipoUsuario() : TipoUsuario.USUARIO_COMUN);
        
        if (usuario.getTipoUsuario() == TipoUsuario.USUARIO_EMPRESA) {
            usuario.setNombreEmpresa(request.getNombreEmpresa());
            usuario.setDescripcionEmpresa(request.getDescripcionEmpresa());
            usuario.setDireccion(request.getDireccion());
            usuario.setTelefono(request.getTelefono());
            usuario.setSitioWeb(request.getSitioWeb());
        }

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return convertirAResponse(savedUsuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return convertirAResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfilPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return convertirAResponse(usuario);
    }

    @Transactional
    public void seguirUsuario(Long seguidorId, Long seguidoId) {
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguidor no encontrado"));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a seguir no encontrado"));

        if (seguidorId.equals(seguidoId)) {
            throw new RuntimeException("No puedes seguirte a ti mismo");
        }

        seguidor.getSiguiendo().add(seguido);
        usuarioRepository.save(seguidor);
    }

    @Transactional
    public void dejarDeSeguir(Long seguidorId, Long seguidoId) {
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguidor no encontrado"));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        seguidor.getSiguiendo().remove(seguido);
        usuarioRepository.save(seguidor);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarUsuarios(String keyword) {
        return usuarioRepository.buscarPorNombre(keyword).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setBiografia(usuario.getBiografia());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setNivelCertificacion(usuario.getNivelCertificacion());
        response.setNumeroInmersiones(usuario.getNumeroInmersiones());
        response.setTipoUsuario(usuario.getTipoUsuario());
        response.setActivo(usuario.getActivo());
        response.setFechaRegistro(usuario.getFechaRegistro());
        response.setNombreEmpresa(usuario.getNombreEmpresa());
        response.setDescripcionEmpresa(usuario.getDescripcionEmpresa());
        response.setDireccion(usuario.getDireccion());
        response.setTelefono(usuario.getTelefono());
        response.setSitioWeb(usuario.getSitioWeb());
        response.setNumeroSeguidores(usuario.getSeguidores().size());
        response.setNumeroSiguiendo(usuario.getSiguiendo().size());
        response.setNumeroPublicaciones(publicacionRepository.countByUsuario(usuario).intValue());
        return response;
    }
}