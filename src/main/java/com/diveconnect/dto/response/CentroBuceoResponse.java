package com.diveconnect.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CentroBuceoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String ciudad;
    private String pais;
    private String telefono;
    private String email;
    private String sitioWeb;
    private String certificaciones;
    private Double latitud;
    private Double longitud;
    private String imagenUrl;
    private Double valoracionPromedio;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private Long usuarioId;
    private String usuarioUsername;
}