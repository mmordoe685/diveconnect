package com.diveconnect.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private String query;
    private List<UsuarioResponse>   usuarios;
    private List<UsuarioResponse>   empresas;
    private List<InmersionBreve>    inmersiones;
    /**
     * Cuando la búsqueda por "sitio" no devuelve coincidencias textuales y
     * se dispone de coordenadas, el backend completa con las 5 inmersiones
     * más cercanas a ese punto. Se avisa al cliente con este flag.
     */
    private Boolean inmersionesPorProximidad = false;

    @Data
    public static class InmersionBreve {
        private Long id;
        private String titulo;
        private String descripcion;
        private String ubicacion;
        private Double latitud;
        private Double longitud;
        private Double profundidadMaxima;
        private String nivelRequerido;
        private Double precio;
        private Integer plazasDisponibles;
        private String imagenUrl;
        private String centroBuceoNombre;
        private Long   centroBuceoId;
        /** Distancia en km al punto solicitado. Null si no se filtró por proximidad. */
        private Double distanciaKm;
    }
}
