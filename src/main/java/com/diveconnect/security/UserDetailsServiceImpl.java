package com.diveconnect.security;

import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar por username o email
        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> usuarioRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Crear authorities basados en el tipo de usuario
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        switch (usuario.getTipoUsuario()) {
            case ADMINISTRADOR:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_EMPRESA"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case USUARIO_EMPRESA:
                authorities.add(new SimpleGrantedAuthority("ROLE_EMPRESA"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case USUARIO_COMUN:
            default:
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
        }

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}