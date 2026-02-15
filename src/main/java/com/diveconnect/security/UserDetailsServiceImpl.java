package com.diveconnect.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.UsuarioRepository;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> usuarioRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                new ArrayList<>()
        );
    }
}