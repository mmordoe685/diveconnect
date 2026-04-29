package com.diveconnect.controller;

import com.diveconnect.entity.EstadoReserva;
import com.diveconnect.entity.Reserva;
import com.diveconnect.entity.TipoNotificacion;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.service.NotificacionService;
import com.diveconnect.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final StripeService stripeService;
    private final ReservaRepository reservaRepository;
    private final NotificacionService notificacionService;

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
            log.error("Stripe createCheckout falló", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "enabled", true,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Verifica/confirma el pago de una reserva. Tres caminos:
     *  1. Stripe configurado + sessionId presente: consulta el estado real en Stripe.
     *  2. Stripe configurado pero sin sessionId: tratado como pago demo TFG (marca PAID).
     *     Esto hace que el flujo "Pagar con tarjeta" del modal funcione aunque el centro
     *     no haya activado Stripe completo. En producción real este caso debería rechazarse.
     *  3. Stripe NO configurado: modo demo TFG — marca PAID + CONFIRMADA.
     *
     * Tras un pago confirmado se notifica al usuario y al centro (mismo comportamiento
     * que el flujo PayPal).
     */
    @PostMapping("/verify/{reservaId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> verificar(@PathVariable Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Idempotencia: si ya está pagada, devolver el estado tal cual sin volver a notificar.
        if ("PAID".equalsIgnoreCase(reserva.getPaymentStatus())) {
            return ResponseEntity.ok(Map.of(
                    "status",        "PAID",
                    "estado",        reserva.getEstado().name(),
                    "alreadyPaid",   true
            ));
        }

        // Camino 1: Stripe activado + sessionId real → consultar Stripe
        if (stripeService.isEnabled() && reserva.getStripeSessionId() != null) {
            try {
                Session session = stripeService.retrieveSession(reserva.getStripeSessionId());
                String payStatus = session.getPaymentStatus(); // paid | unpaid | no_payment_required
                if ("paid".equalsIgnoreCase(payStatus)) {
                    confirmarPago(reserva, session.getPaymentIntent(), "Stripe");
                } else {
                    reserva.setPaymentStatus("UNPAID");
                    reservaRepository.save(reserva);
                }
                return ResponseEntity.ok(Map.of(
                        "status", reserva.getPaymentStatus(),
                        "estado", reserva.getEstado().name(),
                        "demo",   false
                ));
            } catch (StripeException e) {
                log.error("Stripe retrieveSession falló", e);
                return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
            }
        }

        // Caminos 2 y 3: modo demo (TFG) — el modal local validó la tarjeta.
        confirmarPago(reserva, null, "Demo");
        return ResponseEntity.ok(Map.of(
                "status", "PAID",
                "estado", reserva.getEstado().name(),
                "demo",   true
        ));
    }

    /** Marca la reserva como pagada y dispara las dos notificaciones (usuario + centro). */
    private void confirmarPago(Reserva reserva, String paymentIntentId, String pasarela) {
        reserva.setPaymentStatus("PAID");
        if (paymentIntentId != null) reserva.setStripePaymentIntentId(paymentIntentId);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepository.save(reserva);

        // Notificar al usuario
        if (reserva.getUsuario() != null) {
            notificacionService.crear(
                    reserva.getUsuario(), null,
                    TipoNotificacion.RESERVA_CONFIRMADA, reserva.getId(),
                    "Tu reserva ha sido confirmada y pagada con " + pasarela,
                    false);
        }
        // Notificar al centro de buceo
        if (reserva.getCentroBuceo() != null && reserva.getCentroBuceo().getUsuario() != null) {
            notificacionService.crear(
                    reserva.getCentroBuceo().getUsuario(), reserva.getUsuario(),
                    TipoNotificacion.RESERVA_RECIBIDA, reserva.getId(),
                    "Has recibido una nueva reserva confirmada",
                    false);
        }
    }
}
