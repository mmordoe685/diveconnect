package com.diveconnect.service;

import com.diveconnect.dto.request.ReservaRequest;
import com.diveconnect.dto.response.ReservaResponse;
import com.diveconnect.entity.*;
import com.diveconnect.exception.BadRequestException;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.InmersionRepository;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de ReservaService.
 *
 * Cubren los caminos críticos de la creación de reserva:
 *   1. Camino feliz: descuenta plazas y deja la reserva PENDIENTE/UNPAID.
 *   2. Plazas insuficientes: lanza BadRequestException sin tocar BD.
 *   3. Inmersión inexistente: ResourceNotFoundException.
 *   4. Usuario inexistente: ResourceNotFoundException.
 *   5. marcarComoPagada cambia estado a PAID + CONFIRMADA.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository  reservaRepository;
    @Mock private InmersionRepository inmersionRepository;
    @Mock private UsuarioRepository  usuarioRepository;

    @InjectMocks private ReservaService reservaService;

    private Usuario usuario;
    private CentroBuceo centro;
    private Inmersion inmersion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("buceador_test");

        centro = new CentroBuceo();
        centro.setId(10L);
        centro.setNombre("Centro Test");

        inmersion = new Inmersion();
        inmersion.setId(100L);
        inmersion.setTitulo("Inmersión Cabo Test");
        inmersion.setCentroBuceo(centro);
        inmersion.setPrecio(45.0);
        inmersion.setPlazasDisponibles(4);
    }

    @Test
    @DisplayName("crearReserva: camino feliz crea reserva PENDIENTE/UNPAID y descuenta plazas")
    void crearReserva_caminoFeliz() {
        ReservaRequest req = new ReservaRequest();
        req.setInmersionId(100L);
        req.setNumeroPersonas(2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(inmersionRepository.findById(100L)).thenReturn(Optional.of(inmersion));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(999L);
            return r;
        });

        ReservaResponse resp = reservaService.crearReserva(1L, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(999L);
        assertThat(resp.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
        assertThat(resp.getPaymentStatus()).isEqualTo("UNPAID");
        assertThat(resp.getPrecioTotal()).isEqualTo(90.0); // 45 * 2
        assertThat(inmersion.getPlazasDisponibles()).isEqualTo(2); // 4 - 2

        verify(reservaRepository).save(any(Reserva.class));
        verify(inmersionRepository).save(inmersion);
    }

    @Test
    @DisplayName("crearReserva: sin plazas suficientes lanza BadRequest y no graba nada")
    void crearReserva_sinPlazas() {
        ReservaRequest req = new ReservaRequest();
        req.setInmersionId(100L);
        req.setNumeroPersonas(10);  // pide 10, sólo hay 4

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(inmersionRepository.findById(100L)).thenReturn(Optional.of(inmersion));

        assertThatThrownBy(() -> reservaService.crearReserva(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("plazas");

        verify(reservaRepository, never()).save(any());
        assertThat(inmersion.getPlazasDisponibles()).isEqualTo(4); // sin tocar
    }

    @Test
    @DisplayName("crearReserva: inmersión inexistente lanza ResourceNotFoundException")
    void crearReserva_inmersionNoExiste() {
        ReservaRequest req = new ReservaRequest();
        req.setInmersionId(999L);
        req.setNumeroPersonas(1);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(inmersionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(1L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearReserva: usuario inexistente lanza ResourceNotFoundException")
    void crearReserva_usuarioNoExiste() {
        ReservaRequest req = new ReservaRequest();
        req.setInmersionId(100L);
        req.setNumeroPersonas(1);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.crearReserva(999L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inmersionRepository, never()).findById(any());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarComoPagada: cambia estado a PAID + CONFIRMADA")
    void marcarComoPagada_actualizaCorrectamente() {
        Reserva r = new Reserva();
        r.setId(50L);
        r.setUsuario(usuario);
        r.setInmersion(inmersion);
        r.setCentroBuceo(centro);
        r.setNumeroPersonas(1);
        r.setPrecioTotal(45.0);
        r.setEstado(EstadoReserva.PENDIENTE);
        r.setPaymentStatus("UNPAID");

        when(reservaRepository.findById(50L)).thenReturn(Optional.of(r));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse resp = reservaService.marcarComoPagada(50L, "pi_test_xyz");

        assertThat(resp.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        assertThat(resp.getPaymentStatus()).isEqualTo("PAID");
        assertThat(resp.getStripeSessionId()).isNull(); // no se cambia
    }
}
