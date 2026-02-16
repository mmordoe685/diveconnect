package com.diveconnect.dto.request;

import lombok.Data;

@Data
public class ActualizarPerfilRequest {
    private String biografia;
    private String fotoPerfil;
    private String nivelCertificacion;
    private Integer numeroInmersiones;
    
    // Para empresas
    private String nombreEmpresa;
    private String descripcionEmpresa;
    private String direccion;
    private String telefono;
    private String sitioWeb;
}