package com.diveconnect.service;

import com.diveconnect.entity.Reserva;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;

/**
 * Cliente ligero de la REST API v2 de PayPal — soporta sandbox y live.
 * No añade dependencias Maven: usa {@code java.net.http.HttpClient}.
 *
 * <p>Modo habilitado solo si {@code paypal.client-id} y
 * {@code paypal.client-secret} están definidos (no vacíos). Si no, el
 * frontend puede usar el flujo cliente-cliente con client-id público
 * "sb" como demo.
 */
@Service
@Slf4j
public class PayPalService {

    @Value("${paypal.client-id:}")     private String clientId;
    @Value("${paypal.client-secret:}") private String clientSecret;
    @Value("${paypal.mode:sandbox}")   private String mode;
    @Value("${paypal.base-url-sandbox}") private String baseUrlSandbox;
    @Value("${paypal.base-url-live}")    private String baseUrlLive;
    @Value("${paypal.currency:EUR}")     private String currency;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
            && clientSecret != null && !clientSecret.isBlank();
    }

    public String getClientId()   { return clientId; }
    public String getMode()       { return mode; }
    public String getCurrency()   { return currency; }

    public String getBaseUrl() {
        return "live".equalsIgnoreCase(mode) ? baseUrlLive : baseUrlSandbox;
    }

    /** Obtiene un access token de PayPal mediante OAuth2 client_credentials. */
    public String fetchAccessToken() throws PayPalException {
        if (!isConfigured()) {
            throw new PayPalException("PayPal no está configurado (falta client-id / secret)");
        }
        String creds = clientId + ":" + clientSecret;
        String basic = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v1/oauth2/token"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new PayPalException("PayPal token error " + resp.statusCode() + ": " + resp.body());
            }
            JsonNode node = mapper.readTree(resp.body());
            return node.path("access_token").asText();
        } catch (PayPalException e) { throw e; }
          catch (Exception e) {
            throw new PayPalException("Error obteniendo token de PayPal: " + e.getMessage(), e);
        }
    }

    /** Crea una orden de pago para una reserva. Devuelve el JSON completo (incluye id + approve URL). */
    public JsonNode createOrder(Reserva reserva) throws PayPalException {
        String token = fetchAccessToken();
        String amount = String.format(Locale.US, "%.2f", reserva.getPrecioTotal());
        String descripcion = reserva.getInmersion() != null
                ? "Reserva: " + reserva.getInmersion().getTitulo()
                : "Reserva #" + reserva.getId();

        String body = """
                {
                  "intent": "CAPTURE",
                  "purchase_units": [
                    {
                      "reference_id": "reserva-%d",
                      "description": %s,
                      "amount": {
                        "currency_code": "%s",
                        "value": "%s"
                      }
                    }
                  ]
                }
                """.formatted(
                    reserva.getId(),
                    mapper.valueToTree(descripcion).toString(),
                    currency,
                    amount);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new PayPalException("PayPal createOrder error " + resp.statusCode() + ": " + resp.body());
            }
            return mapper.readTree(resp.body());
        } catch (PayPalException e) { throw e; }
          catch (Exception e) {
            throw new PayPalException("Error creando orden PayPal: " + e.getMessage(), e);
        }
    }

    /** Captura los fondos de una orden aprobada. Devuelve el JSON completo. */
    public JsonNode captureOrder(String orderId) throws PayPalException {
        String token = fetchAccessToken();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new PayPalException("PayPal captureOrder error " + resp.statusCode() + ": " + resp.body());
            }
            return mapper.readTree(resp.body());
        } catch (PayPalException e) { throw e; }
          catch (Exception e) {
            throw new PayPalException("Error capturando orden PayPal: " + e.getMessage(), e);
        }
    }

    public static class PayPalException extends Exception {
        public PayPalException(String m) { super(m); }
        public PayPalException(String m, Throwable c) { super(m, c); }
    }
}
