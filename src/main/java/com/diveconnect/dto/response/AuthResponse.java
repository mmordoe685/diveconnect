package com.diveconnect.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo = "Bearer";
    private UsuarioResponse usuario;
    
    public AuthResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }
}