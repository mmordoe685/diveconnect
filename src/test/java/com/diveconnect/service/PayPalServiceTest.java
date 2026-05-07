package com.diveconnect.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayPalServiceTest {

    private PayPalService crearServicio(String clientId, String clientSecret, String mode) {
        PayPalService svc = new PayPalService();
        ReflectionTestUtils.setField(svc, "clientId",       clientId);
        ReflectionTestUtils.setField(svc, "clientSecret",   clientSecret);
        ReflectionTestUtils.setField(svc, "mode",           mode);
        ReflectionTestUtils.setField(svc, "baseUrlSandbox", "https://api-m.sandbox.paypal.com");
        ReflectionTestUtils.setField(svc, "baseUrlLive",    "https://api-m.paypal.com");
        ReflectionTestUtils.setField(svc, "currency",       "EUR");
        return svc;
    }

    @Test
    @DisplayName("isConfigured: false si client-id está vacío")
    void noConfigurado_clientIdVacio() {
        PayPalService svc = crearServicio("", "secret", "sandbox");
        assertThat(svc.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured: false si client-secret está vacío")
    void noConfigurado_secretVacio() {
        PayPalService svc = crearServicio("clientId", "", "sandbox");
        assertThat(svc.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured: true cuando ambos están definidos")
    void configurado_cuandoAmbosDefinidos() {
        PayPalService svc = crearServicio("clientId", "secret", "sandbox");
        assertThat(svc.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("fetchAccessToken: si no está configurado lanza PayPalException")
    void fetchAccessToken_sinConfigurar_lanzaExcepcion() {
        PayPalService svc = crearServicio("", "", "sandbox");
        assertThatThrownBy(svc::fetchAccessToken)
                .isInstanceOf(PayPalService.PayPalException.class)
                .hasMessageContaining("no está configurado");
    }

    @Test
    @DisplayName("getBaseUrl: en modo sandbox apunta a api-m.sandbox.paypal.com")
    void getBaseUrl_sandbox() {
        PayPalService svc = crearServicio("c", "s", "sandbox");
        assertThat(svc.getBaseUrl()).isEqualTo("https://api-m.sandbox.paypal.com");
    }

    @Test
    @DisplayName("getBaseUrl: en modo live apunta a api-m.paypal.com")
    void getBaseUrl_live() {
        PayPalService svc = crearServicio("c", "s", "live");
        assertThat(svc.getBaseUrl()).isEqualTo("https://api-m.paypal.com");
    }

    @Test
    @DisplayName("getBaseUrl: cualquier modo distinto a 'live' (incluyendo null) usa sandbox")
    void getBaseUrl_modoDesconocido_usaSandbox() {
        PayPalService svc = crearServicio("c", "s", "foo");
        assertThat(svc.getBaseUrl()).isEqualTo("https://api-m.sandbox.paypal.com");
    }

    @Test
    @DisplayName("getCurrency devuelve la moneda configurada")
    void getCurrency_devuelveLaConfigurada() {
        PayPalService svc = crearServicio("c", "s", "sandbox");
        assertThat(svc.getCurrency()).isEqualTo("EUR");
    }
}
