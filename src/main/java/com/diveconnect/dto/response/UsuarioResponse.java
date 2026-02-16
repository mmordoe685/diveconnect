package com.diveconnect.dto.response;

import com.diveconnect.entity.TipoUsuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioResponse {
    private Long id;
    private String username;
    private String email;
    private String biografia;
    private String fotoPerfil;
    private String nivelCertificacion;
    private Integer numeroInmersiones;
    private TipoUsuario tipoUsuario;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    
    // Para empresas
    private String nombreEmpresa;
    private String descripcionEmpresa;
    private String direccion;
    private String telefono;
    private String sitioWeb;
    
    // Estadísticas
    private Integer numeroSeguidores;
    private Integer numeroSiguiendo;
    private Integer numeroPublicaciones;
}