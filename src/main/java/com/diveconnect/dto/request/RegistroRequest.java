package com.diveconnect.dto.request;

import com.diveconnect.entity.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroRequest {
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    private TipoUsuario tipoUsuario = TipoUsuario.USUARIO_COMUN;
    
    // Campos adicionales para empresas
    private String nombreEmpresa;
    private String descripcionEmpresa;
    private String direccion;
    private String telefono;
    private String sitioWeb;
}