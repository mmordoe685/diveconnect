package com.diveconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HistoriaRequest {
    @NotBlank(message = "La URL del medio es obligatoria")
    private String mediaUrl;

    private String mediaType = "FOTO"; // FOTO | VIDEO

    private String texto;
}
