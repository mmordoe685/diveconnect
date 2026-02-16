package com.diveconnect.service;

import com.diveconnect.dto.request.ActualizarPerfilRequest;
import com.diveconnect.dto.request.RegistroRequest;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.PublicacionRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Verificar si el username ya existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("El username '" + request.getUsername() + "' ya está en uso");
        }
        
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email '" + request.getEmail() + "' ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTipoUsuario(request.getTipoUsuario() != null ? request.getTipoUsuario() : TipoUsuario.USUARIO_COMUN);
        
        // Si es empresa, guardar información adicional
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));
        return convertirAResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfilPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' no encontrado"));
        return convertirAResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado"));

        // Actualizar campos generales
        if (request.getBiografia() != null) {
            usuario.setBiografia(request.getBiografia());
        }
        if (request.getFotoPerfil() != null) {
            usuario.setFotoPerfil(request.getFotoPerfil());
        }
        if (request.getNivelCertificacion() != null) {
            usuario.setNivelCertificacion(request.getNivelCertificacion());
        }
        if (request.getNumeroInmersiones() != null) {
            usuario.setNumeroInmersiones(request.getNumeroInmersiones());
        }

        // Actualizar campos de empresa si es aplicable
        if (usuario.getTipoUsuario() == TipoUsuario.USUARIO_EMPRESA) {
            if (request.getNombreEmpresa() != null) {
                usuario.setNombreEmpresa(request.getNombreEmpresa());
            }
            if (request.getDescripcionEmpresa() != null) {
                usuario.setDescripcionEmpresa(request.getDescripcionEmpresa());
            }
            if (request.getDireccion() != null) {
                usuario.setDireccion(request.getDireccion());
            }
            if (request.getTelefono() != null) {
                usuario.setTelefono(request.getTelefono());
            }
            if (request.getSitioWeb() != null) {
                usuario.setSitioWeb(request.getSitioWeb());
            }
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return convertirAResponse(updatedUsuario);
    }

    @Transactional
    public void seguirUsuario(Long seguidorId, Long seguidoId) {
        if (seguidorId.equals(seguidoId)) {
            throw new BadRequestException("No puedes seguirte a ti mismo");
        }

        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado"));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a seguir no encontrado"));

        if (seguidor.getSiguiendo().contains(seguido)) {
            throw new BadRequestException("Ya sigues a este usuario");
        }

        seguidor.getSiguiendo().add(seguido);
        usuarioRepository.save(seguidor);
    }

    @Transactional
    public void dejarDeSeguir(Long seguidorId, Long seguidoId) {
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado"));
        Usuario seguido = usuarioRepository.findById(seguidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!seguidor.getSiguiendo().contains(seguido)) {
            throw new BadRequestException("No sigues a este usuario");
        }

        seguidor.getSiguiendo().remove(seguido);
        usuarioRepository.save(seguidor);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerSeguidores(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        return usuario.getSeguidores().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerSiguiendo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        return usuario.getSiguiendo().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarUsuarios(String keyword) {
        return usuarioRepository.buscarPorNombre(keyword).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerEmpresas() {
        return usuarioRepository.findEmpresasActivas().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll().stream()
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
        
        // Información de empresa
        response.setNombreEmpresa(usuario.getNombreEmpresa());
        response.setDescripcionEmpresa(usuario.getDescripcionEmpresa());
        response.setDireccion(usuario.getDireccion());
        response.setTelefono(usuario.getTelefono());
        response.setSitioWeb(usuario.getSitioWeb());
        
        // Estadísticas
        response.setNumeroSeguidores(usuario.getSeguidores().size());
        response.setNumeroSiguiendo(usuario.getSiguiendo().size());
        response.setNumeroPublicaciones(publicacionRepository.countByUsuario(usuario).intValue());
        
        return response;
    }
}