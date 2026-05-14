package com.diveconnect.controller;

import com.diveconnect.entity.EstadoReserva;
import com.diveconnect.entity.Reserva;
import com.diveconnect.entity.TipoNotificacion;
import com.diveconnect.exception.ResourceNotFoundException;
import com.diveconnect.repository.ReservaRepository;
import com.diveconnect.service.NotificacionService;
import com.diveconnect.service.PayPalService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
@Slf4j
public class PayPalController {

    private final PayPalService       paypal;
    private final ReservaRepository   reservaRepository;
    private final NotificacionService notificacionService;

    /** Config para el frontend: si está habilitado + client-id (sólo público) + modo + moneda. */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        return ResponseEntity.ok(Map.of(
                "enabled",   paypal.isConfigured(),
                "clientId",  paypal.isConfigured() ? paypal.getClientId() : "",
                "mode",      paypal.getMode(),
                "currency",  paypal.getCurrency()
        ));
    }

    /** Crea una orden PayPal para una reserva. Devuelve orderId + estado. */
    @PostMapping("/create-order/{reservaId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> crearOrden(@PathVariable Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!paypal.isConfigured()) {
            return ResponseEntity.ok(Map.of(
                    "enabled", false,
                    "message", "PayPal no está configurado en el backend. " +
                               "Define PAYPAL_CLIENT_ID y PAYPAL_CLIENT_SECRET."
            ));
        }

        try {
            JsonNode order = paypal.createOrder(reserva);
            String orderId = order.path("id").asText();
            reserva.setPaypalOrderId(orderId);
            reserva.setPaymentStatus("UNPAID");
            reservaRepository.save(reserva);

            Map<String, Object> out = new HashMap<>();
            out.put("enabled", true);
            out.put("orderId", orderId);
            out.put("status",  order.path("status").asText());
            return ResponseEntity.ok(out);
        } catch (PayPalService.PayPalException e) {
            log.error("PayPal createOrder falló", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "enabled", true,
                    "error",   "No se pudo crear la orden de PayPal"
            ));
        }
    }

    @PostMapping("/capture-order/{reservaId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> capturarOrden(
            @PathVariable Long reservaId,
            @RequestParam("orderId") String orderId) {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!paypal.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "enabled", false,
                    "error",   "PayPal no configurado en backend"
            ));
        }

        try {
            JsonNode captured = paypal.captureOrder(orderId);
            String status = captured.path("status").asText();

            if ("COMPLETED".equalsIgnoreCase(status)) {
                reserva.setPaymentStatus("PAID");
                reserva.setEstado(EstadoReserva.CONFIRMADA);
                reserva.setPaypalOrderId(orderId);
                // El id de captura está en purchase_units[0].payments.captures[0].id
                JsonNode captureId = captured.at("/purchase_units/0/payments/captures/0/id");
                if (captureId != null && !captureId.isMissingNode()) {
                    reserva.setPaypalCaptureId(captureId.asText());
                }
                reservaRepository.save(reserva);

                // Notificar al usuario y al centro
                if (reserva.getUsuario() != null) {
                    notificacionService.crear(
                            reserva.getUsuario(), null,
                            TipoNotificacion.RESERVA_CONFIRMADA, reserva.getId(),
                            "Tu reserva ha sido confirmada y pagada con PayPal",
                            false);
                }
                if (reserva.getCentroBuceo() != null && reserva.getCentroBuceo().getUsuario() != null) {
                    notificacionService.crear(
                            reserva.getCentroBuceo().getUsuario(), reserva.getUsuario(),
                            TipoNotificacion.RESERVA_RECIBIDA, reserva.getId(),
                            "Has recibido una nueva reserva confirmada",
                            false);
                }
            } else {
                reserva.setPaymentStatus("UNPAID");
                reservaRepository.save(reserva);
            }

            return ResponseEntity.ok(Map.of(
                    "status", status,
                    "estado", reserva.getEstado().name(),
                    "paymentStatus", reserva.getPaymentStatus() == null ? "" : reserva.getPaymentStatus()
            ));
        } catch (PayPalService.PayPalException e) {
            log.error("PayPal captureOrder falló", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "No se pudo capturar la orden de PayPal"
            ));
        }
    }
}
