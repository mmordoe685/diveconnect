package com.diveconnect.service;

import com.diveconnect.entity.Reserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests del servicio de Stripe sin red.
 * Comprueba que sin secret-key:
 *   - isEnabled() es false
 *   - createCheckoutSession lanza IllegalStateException con mensaje informativo
 *   - getPublishableKey nunca es null (devuelve "" en vez de null)
 */
class StripeServiceTest {

    private StripeService crearServicio(String secretKey, String publishableKey) {
        StripeService svc = new StripeService();
        ReflectionTestUtils.setField(svc, "secretKey",       secretKey);
        ReflectionTestUtils.setField(svc, "publishableKey",  publishableKey);
        ReflectionTestUtils.setField(svc, "currency",        "eur");
        ReflectionTestUtils.setField(svc, "frontendUrl",     "http://localhost:8080");
        return svc;
    }

    @Test
    @DisplayName("isEnabled: false con secret vacío")
    void isEnabled_secretVacio() {
        assertThat(crearServicio("", "pk_test").isEnabled()).isFalse();
    }

    @Test
    @DisplayName("isEnabled: false con secret null")
    void isEnabled_secretNull() {
        assertThat(crearServicio(null, "pk_test").isEnabled()).isFalse();
    }

    @Test
    @DisplayName("isEnabled: true con secret definida")
    void isEnabled_secretDefinida() {
        assertThat(crearServicio("sk_test_xxx", "pk_test_xxx").isEnabled()).isTrue();
    }

    @Test
    @DisplayName("getPublishableKey: nunca devuelve null")
    void getPublishableKey_nuncaNull() {
        assertThat(crearServicio("sk", null).getPublishableKey()).isEqualTo("");
        assertThat(crearServicio("sk", "pk_x").getPublishableKey()).isEqualTo("pk_x");
    }

    @Test
    @DisplayName("createCheckoutSession sin configurar lanza IllegalStateException")
    void createCheckoutSession_sinConfigurar_lanza() {
        StripeService svc = crearServicio("", "");
        Reserva r = new Reserva();
        r.setId(1L);
        r.setPrecioTotal(50.0);
        r.setNumeroPersonas(1);

        assertThatThrownBy(() -> svc.createCheckoutSession(r))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stripe no está configurado");
    }
}
