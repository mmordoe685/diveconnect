package com.diveconnect.service;

import com.diveconnect.dto.response.SearchResponse;
import com.diveconnect.dto.response.UsuarioResponse;
import com.diveconnect.entity.Inmersion;
import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;
import com.diveconnect.repository.InmersionRepository;
import com.diveconnect.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UsuarioRepository  usuarioRepository;
    private final InmersionRepository inmersionRepository;
    private final UsuarioMapper       usuarioMapper;

    private static final int MAX_USUARIOS    = 20;
    private static final int MAX_INMERSIONES = 30;
    private static final int PROXIMIDAD_N    =  5;

    @Transactional(readOnly = true)
    public SearchResponse buscar(String q,
                                  String tipo,
                                  Double lat, Double lon,
                                  Double profMin, Double profMax,
                                  Double precioMax,
                                  String nivel) {

        String query  = (q == null) ? "" : q.trim();
        String tipoN  = (tipo == null || tipo.isBlank()) ? "todo" : tipo.toLowerCase();

        SearchResponse out = new SearchResponse();
        out.setQuery(query);
        out.setUsuarios(Collections.emptyList());
        out.setEmpresas(Collections.emptyList());
        out.setInmersiones(Collections.emptyList());
        out.setInmersionesPorProximidad(false);

        boolean wantUsuarios    = tipoN.equals("todo") || tipoN.equals("usuarios");
        boolean wantEmpresas    = tipoN.equals("todo") || tipoN.equals("empresas");
        boolean wantInmersiones = tipoN.equals("todo") || tipoN.equals("inmersiones");

        if (wantUsuarios || wantEmpresas) {
            List<Usuario> base = query.isEmpty()
                    ? usuarioRepository.findByActivoTrue()
                    : usuarioRepository.buscarPorNombre(query);

            if (wantUsuarios) {
                out.setUsuarios(base.stream()
                        .filter(u -> u.getTipoUsuario() == TipoUsuario.USUARIO_COMUN)
                        .limit(MAX_USUARIOS)
                        .map(usuarioMapper::toResponse)
                        .collect(Collectors.toList()));
            }
            if (wantEmpresas) {
                List<UsuarioResponse> empresas = base.stream()
                        .filter(u -> u.getTipoUsuario() == TipoUsuario.USUARIO_EMPRESA)
                        .limit(MAX_USUARIOS)
                        .map(usuarioMapper::toResponse)
                        .collect(Collectors.toList());
                // Si la lista de empresas está vacía y no hay query explícita sobre empresas,
                // devolvemos la lista completa de empresas activas para que se vea algo.
                if (empresas.isEmpty() && query.isEmpty() && tipoN.equals("empresas")) {
                    empresas = usuarioRepository.findEmpresasActivas().stream()
                            .limit(MAX_USUARIOS)
                            .map(usuarioMapper::toResponse)
                            .collect(Collectors.toList());
                }
                out.setEmpresas(empresas);
            }
        }

        if (wantInmersiones) {
            List<Inmersion> inmersiones = inmersionRepository.buscarAvanzado(
                    query, profMin, profMax, precioMax, nivel);

            if (inmersiones.size() > MAX_INMERSIONES) {
                inmersiones = inmersiones.subList(0, MAX_INMERSIONES);
            }

            // Si hay coordenadas y la búsqueda textual no devolvió nada,
            // usamos proximidad como fallback útil.
            if (inmersiones.isEmpty() && lat != null && lon != null) {
                List<InmersionRepository.InmersionConDistancia> cercanas =
                        inmersionRepository.findMasCercanas(
                                lat, lon, org.springframework.data.domain.PageRequest.of(0, PROXIMIDAD_N));
                if (!cercanas.isEmpty()) {
                    Map<Long, Double> distancias = cercanas.stream()
                            .collect(Collectors.toMap(
                                    InmersionRepository.InmersionConDistancia::getId,
                                    InmersionRepository.InmersionConDistancia::getDistanciaKm));
                    List<Inmersion> entities = inmersionRepository.findAllById(distancias.keySet());
                    // mantener el orden por distancia asc
                    entities.sort(Comparator.comparingDouble(e ->
                            distancias.getOrDefault(e.getId(), Double.MAX_VALUE)));
                    out.setInmersiones(entities.stream()
                            .map(e -> toBreve(e, distancias.get(e.getId())))
                            .collect(Collectors.toList()));
                    out.setInmersionesPorProximidad(true);
                } else {
                    out.setInmersiones(Collections.emptyList());
                }
            } else {
                // calcular distancia si hay lat/lon
                out.setInmersiones(inmersiones.stream()
                        .map(i -> toBreve(i, calcularDistancia(i, lat, lon)))
                        .collect(Collectors.toList()));
            }
        }

        return out;
    }

    private Double calcularDistancia(Inmersion i, Double lat, Double lon) {
        if (lat == null || lon == null || i.getLatitud() == null || i.getLongitud() == null) return null;
        double latRad1 = Math.toRadians(lat);
        double latRad2 = Math.toRadians(i.getLatitud());
        double dLat    = Math.toRadians(i.getLatitud() - lat);
        double dLon    = Math.toRadians(i.getLongitud() - lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(latRad1) * Math.cos(latRad2)
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    private SearchResponse.InmersionBreve toBreve(Inmersion i, Double distanciaKm) {
        SearchResponse.InmersionBreve b = new SearchResponse.InmersionBreve();
        b.setId(i.getId());
        b.setTitulo(i.getTitulo());
        b.setDescripcion(i.getDescripcion());
        b.setUbicacion(i.getUbicacion());
        b.setLatitud(i.getLatitud());
        b.setLongitud(i.getLongitud());
        b.setProfundidadMaxima(i.getProfundidadMaxima());
        b.setNivelRequerido(i.getNivelRequerido());
        b.setPrecio(i.getPrecio());
        b.setPlazasDisponibles(i.getPlazasDisponibles());
        b.setImagenUrl(i.getImagenUrl());
        if (i.getCentroBuceo() != null) {
            b.setCentroBuceoId(i.getCentroBuceo().getId());
            b.setCentroBuceoNombre(i.getCentroBuceo().getNombre());
        }
        if (distanciaKm != null) {
            b.setDistanciaKm(Math.round(distanciaKm * 10.0) / 10.0);
        }
        return b;
    }
}
