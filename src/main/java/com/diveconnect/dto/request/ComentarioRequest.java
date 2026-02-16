package com.diveconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioRequest {
    
    @NotBlank(message = "El contenido del comentario es obligatorio")
    private String contenido;
}