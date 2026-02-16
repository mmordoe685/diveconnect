package com.diveconnect.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CentroBuceoRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String descripcion;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    
    private String ciudad;
    private String pais;
    private String telefono;
    
    @Email(message = "Email debe ser válido")
    private String email;
    
    private String sitioWeb;
    private String certificaciones;
    private Double latitud;
    private Double longitud;
    private String imagenUrl;
}