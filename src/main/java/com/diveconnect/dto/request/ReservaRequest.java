package com.diveconnect.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaRequest {
    
    @NotNull(message = "El ID de la inmersión es obligatorio")
    private Long inmersionId;
    
    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe ser al menos 1 persona")
    private Integer numeroPersonas;
    
    private String observaciones;
}