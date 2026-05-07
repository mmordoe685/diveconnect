package com.diveconnect.service;

import com.diveconnect.entity.Reserva;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @Value("${stripe.publishable-key:}")
    private String publishableKey;

    @Value("${stripe.currency:eur}")
    private String currency;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @PostConstruct
    void init() {
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    public boolean isEnabled() {
        return secretKey != null && !secretKey.isBlank();
    }

    public String getPublishableKey() {
        return publishableKey == null ? "" : publishableKey;
    }

    public Session createCheckoutSession(Reserva reserva) throws StripeException {
        if (!isEnabled()) {
            throw new IllegalStateException(
                "Stripe no está configurado. Define STRIPE_SECRET_KEY y STRIPE_PUBLISHABLE_KEY.");
        }

        long amountCents = Math.round(reserva.getPrecioTotal() * 100);
        String tituloInmersion = reserva.getInmersion() != null
                ? reserva.getInmersion().getTitulo()
                : "Inmersión";

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(amountCents)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(tituloInmersion)
                                                .setDescription("DiveConnect - " + reserva.getNumeroPersonas() + " pax")
                                                .build()
                                )
                                .build()
                )
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/pages/reservas.html?payment=success&reserva=" + reserva.getId())
                .setCancelUrl(frontendUrl + "/pages/reservas.html?payment=cancelled&reserva=" + reserva.getId())
                .addLineItem(lineItem)
                .putMetadata("reservaId", String.valueOf(reserva.getId()))
                .build();

        return Session.create(params);
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}
