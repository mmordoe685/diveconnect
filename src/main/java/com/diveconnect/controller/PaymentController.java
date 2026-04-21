package com.diveconnect.controller;

import com.diveconnect.entity.EstadoReserva;
import com.diveconnect.entity.Reserva;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final StripeService stripeService;
    private final ReservaRepository reservaRepository;

    /** Devuelve publishable key y si Stripe está habilitado. */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        return ResponseEntity.ok(Map.of(
                "enabled", stripeService.isEnabled(),
                "publishableKey", stripeService.getPublishableKey()
        ));
    }

    /** Crea una Checkout Session para una reserva y devuelve su URL. */
    @PostMapping("/checkout/{reservaId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> crearCheckout(@PathVariable Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!stripeService.isEnabled()) {
            return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "message", "Stripe no está configurado. En modo demo, la reserva se marca como pagada."
            ));
        }

        try {
            Session session = stripeService.createCheckoutSession(reserva);
            reserva.setStripeSessionId(session.getId());
            reserva.setPaymentStatus("UNPAID");
            reservaRepository.save(reserva);

            return ResponseEntity.ok(Map.of(
                    "enabled", true,
                    "sessionId", session.getId(),
                    "url", session.getUrl()
            ));
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "enabled", true,
                    "error", e.getMessage()
            ));
        }
    }

    /** Verifica el estado de pago de una sesión (se llama desde la página de éxito). */
    @PostMapping("/verify/{reservaId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> verificar(@PathVariable Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Modo demo: Stripe no configurado -> marcar pagada directamente
        if (!stripeService.isEnabled()) {
            reserva.setPaymentStatus("PAID");
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva);
            return ResponseEntity.ok(Map.of("status", "PAID", "demo", true));
        }

        if (reserva.getStripeSessionId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sin sesión de pago"));
        }

        try {
            Session session = stripeService.retrieveSession(reserva.getStripeSessionId());
            String payStatus = session.getPaymentStatus(); // paid | unpaid | no_payment_required
            if ("paid".equalsIgnoreCase(payStatus)) {
                reserva.setPaymentStatus("PAID");
                reserva.setStripePaymentIntentId(session.getPaymentIntent());
                reserva.setEstado(EstadoReserva.CONFIRMADA);
            } else {
                reserva.setPaymentStatus("UNPAID");
            }
            reservaRepository.save(reserva);
            return ResponseEntity.ok(Map.of("status", reserva.getPaymentStatus(),
                    "estado", reserva.getEstado().name()));
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
